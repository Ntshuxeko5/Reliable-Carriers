package com.reliablecarriers.Reliable.Carriers.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.requests-per-hour:1000}")
    private int requestsPerHour;

    @Value("${app.rate-limit.login-attempts-per-hour:5}")
    private int loginAttemptsPerHour;

    // In-memory storage for rate limiting (in production, use Redis)
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();
    private final Map<String, LoginAttemptInfo> loginAttemptMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIpAddress(request);
        String requestPath = request.getRequestURI();

        // Check if this is a login attempt
        if (isLoginAttempt(requestPath)) {
            if (!checkLoginRateLimit(clientIp)) {
                sendRateLimitResponse(response, "Too many login attempts. Please try again later.");
                return;
            }
        }

        // Check general rate limit
        if (!checkGeneralRateLimit(clientIp)) {
            sendRateLimitResponse(response, "Rate limit exceeded. Please try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkGeneralRateLimit(String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        RateLimitInfo info = rateLimitMap.computeIfAbsent(clientIp, k -> new RateLimitInfo());

        // Clean old entries
        info.cleanOldRequests(now);

        // Check minute limit
        if (info.getRequestsInLastMinute() >= requestsPerMinute) {
            return false;
        }

        // Check hour limit
        if (info.getRequestsInLastHour() >= requestsPerHour) {
            return false;
        }

        // Add current request
        info.addRequest(now);
        return true;
    }

    private boolean checkLoginRateLimit(String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        LoginAttemptInfo info = loginAttemptMap.computeIfAbsent(clientIp, k -> new LoginAttemptInfo());

        // Clean old attempts
        info.cleanOldAttempts(now);

        // Check if too many attempts
        if (info.getAttemptsInLastHour() >= loginAttemptsPerHour) {
            return false;
        }

        // Add current attempt
        info.addAttempt(now);
        return true;
    }

    private boolean isLoginAttempt(String requestPath) {
        return requestPath.contains("/api/auth/login") || 
               requestPath.contains("/api/auth/staff-login") ||
               requestPath.contains("/login");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\",\"retryAfter\":3600}");
    }

    // Inner classes for rate limiting
    private static class RateLimitInfo {
        private final java.util.Queue<LocalDateTime> requests = new java.util.LinkedList<>();

        public void addRequest(LocalDateTime time) {
            requests.offer(time);
        }

        public void cleanOldRequests(LocalDateTime now) {
            while (!requests.isEmpty() && 
                   requests.peek().isBefore(now.minusMinutes(1))) {
                requests.poll();
            }
        }

        public int getRequestsInLastMinute() {
            return (int) requests.stream()
                .filter(time -> time.isAfter(LocalDateTime.now().minusMinutes(1)))
                .count();
        }

        public int getRequestsInLastHour() {
            return (int) requests.stream()
                .filter(time -> time.isAfter(LocalDateTime.now().minusHours(1)))
                .count();
        }
    }

    private static class LoginAttemptInfo {
        private final java.util.Queue<LocalDateTime> attempts = new java.util.LinkedList<>();

        public void addAttempt(LocalDateTime time) {
            attempts.offer(time);
        }

        public void cleanOldAttempts(LocalDateTime now) {
            while (!attempts.isEmpty() && 
                   attempts.peek().isBefore(now.minusHours(1))) {
                attempts.poll();
            }
        }

        public int getAttemptsInLastHour() {
            return (int) attempts.stream()
                .filter(time -> time.isAfter(LocalDateTime.now().minusHours(1)))
                .count();
        }
    }
}

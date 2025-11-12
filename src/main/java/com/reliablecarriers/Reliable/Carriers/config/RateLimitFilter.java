package com.reliablecarriers.Reliable.Carriers.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j
 * Limits API requests to prevent abuse
 */
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    @Value("${app.rate-limit.requests-per-minute:100}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip rate limiting if disabled
        if (!rateLimitEnabled) {
            chain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for static resources and public pages
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/css/") || path.startsWith("/js/") || 
            path.startsWith("/images/") || path.startsWith("/favicon.ico") ||
            path.equals("/") || path.equals("/home") ||
            path.equals("/login") || path.equals("/register") ||
            path.startsWith("/register/") || path.equals("/staff-login") ||
            path.startsWith("/tracking") || path.equals("/about") || 
            path.equals("/contact") || path.startsWith("/services")) {
            chain.doFilter(request, response);
            return;
        }

        // Get client IP
        String clientId = getClientId(httpRequest);

        // Get or create bucket for this client
        Bucket bucket = cache.computeIfAbsent(clientId, this::createNewBucket);

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"Too many requests. Please try again later.\"," +
                "\"status\":429}"
            );
        }
    }

    private Bucket createNewBucket(String clientId) {
        // Configurable rate limit per minute
        // Using Bandwidth.simple (deprecated but functional) - can be updated to Bandwidth.classic in future versions
        return Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))
                .build())
            .build();
    }

    private String getClientId(HttpServletRequest request) {
        // Try to get real IP from proxy headers
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

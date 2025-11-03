package com.reliablecarriers.Reliable.Carriers.config;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filter to authenticate requests using API keys
 * Checks for API key in X-API-Key header or Authorization header
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private ApiKeyService apiKeyService;
    
    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request, 
                                    @org.springframework.lang.NonNull HttpServletResponse response, 
                                    @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Only process API endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/business/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract API key from headers
        String apiKey = extractApiKey(request);
        
        if (apiKey != null) {
            // Validate API key
            User user = apiKeyService.validateApiKey(apiKey);
            
            if (user != null) {
                // Set authentication
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_BUSINESS_API"))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // Invalid API key
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid or expired API key\"}");
                return;
            }
        } else {
            // No API key provided
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"API key required. Use X-API-Key header or Authorization: Bearer <api_key>\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract API key from request headers
     * Checks X-API-Key header first, then Authorization header
     */
    private String extractApiKey(HttpServletRequest request) {
        // Try X-API-Key header first
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }
        
        // Try Authorization header (Bearer token format)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try API-Key header
        apiKey = request.getHeader("API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }
        
        return null;
    }
}


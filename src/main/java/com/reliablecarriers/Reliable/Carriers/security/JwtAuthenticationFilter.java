package com.reliablecarriers.Reliable.Carriers.security;

import com.reliablecarriers.Reliable.Carriers.service.AuditService;
import com.reliablecarriers.Reliable.Carriers.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:reliablecarrierssecretkey}")
    private String jwtSecret;

    private final CustomUserDetailsService userDetailsService;

    private final AuditService auditService;

    public JwtAuthenticationFilter(CustomUserDetailsService userDetailsService, AuditService auditService) {
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                // No Authorization header or not Bearer — continue filter chain
                System.out.println("[JWT DEBUG] No Bearer Authorization header present");
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            // Mask token in logs to avoid leaking secrets
            String masked = jwt.length() > 10 ? jwt.substring(0, 6) + "..." + jwt.substring(jwt.length()-4) : "[short]";
            System.out.println("[JWT DEBUG] Bearer token received (masked): " + masked);

            final String userEmail = extractEmailFromToken(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
                try {
                    if (validateToken(jwt, userDetails)) {
                        System.out.println("[JWT DEBUG] Token validated for user: " + userEmail);
                    } else {
                        System.out.println("[JWT DEBUG] Token validation failed for user: " + userEmail);
                    }
                } catch (Exception ex) {
                    System.out.println("[JWT DEBUG] Token validation threw exception: " + ex.getMessage());
                }

                if (validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Log successful authentication
                    auditService.logAction("API_ACCESS", "AUTH", null, "SUCCESS", 
                        "JWT authentication successful for user: " + userEmail);
                }
            }
        } catch (Exception e) {
            // Log any authentication error (invalid token, parsing error, etc.)
            System.out.println("[JWT DEBUG] Exception in JwtAuthenticationFilter: " + e.getMessage());
            e.printStackTrace();
            auditService.logAction("API_ACCESS", "AUTH", null, "FAILED", "JWT authentication error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractEmailFromToken(String token) {
        try {
            javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean validateToken(String token, UserDetails userDetails) {
        try {
            javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String email = claims.getSubject();
            return email != null && email.equals(userDetails.getUsername()) && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration() != null && 
               claims.getExpiration().before(new java.util.Date());
    }
}

package com.reliablecarriers.Reliable.Carriers.security;

import com.reliablecarriers.Reliable.Carriers.service.AuditService;
import com.reliablecarriers.Reliable.Carriers.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuditService auditService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            final String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            final String userEmail = extractEmailFromToken(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
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
        } catch (SignatureException e) {
            // Log invalid token attempt
            auditService.logAction("API_ACCESS", "AUTH", null, "FAILED", 
                "Invalid JWT token: " + e.getMessage());
        } catch (Exception e) {
            // Log other authentication errors
            auditService.logAction("API_ACCESS", "AUTH", null, "ERROR", 
                "JWT authentication error: " + e.getMessage());
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

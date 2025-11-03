package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request, @org.springframework.lang.NonNull HttpServletResponse response, @org.springframework.lang.NonNull FilterChain chain)
        throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        
        // Check if we have session data and no current authentication (or it's anonymous)
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        boolean needsAuth = (currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken || !currentAuth.isAuthenticated());
        
        if (session != null && needsAuth) {
            String userEmail = (String) session.getAttribute("userEmail");
            String userRole = (String) session.getAttribute("userRole");
            Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
            
            // If we have valid session data, restore authentication
            if (userEmail != null && userRole != null && Boolean.TRUE.equals(isAuthenticated)) {
                // Create authentication token from session
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole))
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        chain.doFilter(request, response);
    }
}

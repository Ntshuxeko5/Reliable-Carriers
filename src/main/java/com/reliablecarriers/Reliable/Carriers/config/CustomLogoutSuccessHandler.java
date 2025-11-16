package com.reliablecarriers.Reliable.Carriers.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String target = "/login?logout=true";
        if (authentication != null && authentication.getAuthorities() != null) {
            boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
            boolean isStaff = hasRole(authentication, "ROLE_STAFF");
            if (isAdmin || isStaff) {
                target = "/staff-login?logout=true";
            }
        }
        response.sendRedirect(target);
    }

    private boolean hasRole(Authentication auth, String role) {
        for (GrantedAuthority ga : auth.getAuthorities()) {
            if (role.equals(ga.getAuthority())) return true;
        }
        return false;
    }
}
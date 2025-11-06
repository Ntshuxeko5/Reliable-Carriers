package com.reliablecarriers.Reliable.Carriers.interceptor;

import com.reliablecarriers.Reliable.Carriers.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PageAccessAuditInterceptor implements HandlerInterceptor {

    @Autowired
    private AuditService auditService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Only log page access (GET requests to HTML pages)
        if ("GET".equalsIgnoreCase(request.getMethod()) && 
            (request.getRequestURI().endsWith(".html") || 
             !request.getRequestURI().startsWith("/api/") ||
             request.getRequestURI().startsWith("/admin/") ||
             request.getRequestURI().startsWith("/customer/") ||
             request.getRequestURI().startsWith("/driver/") ||
             request.getRequestURI().startsWith("/staff/"))) {
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal().toString())) {
                
                try {
                    String pagePath = request.getRequestURI();
                    String queryString = request.getQueryString();
                    String fullPath = queryString != null ? pagePath + "?" + queryString : pagePath;
                    
                    // Log page access with user info from request context
                    auditService.logAction(
                        "PAGE_ACCESS",
                        "PAGE",
                        null,
                        "SUCCESS",
                        null,
                        null,
                        "Page: " + fullPath,
                        request,
                        null
                    );
                } catch (Exception e) {
                    // Don't fail the request if audit logging fails
                    System.err.println("Failed to log page access: " + e.getMessage());
                }
            }
        }
        
        return true;
    }
}


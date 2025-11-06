package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * Custom error handling configuration
 * This controller handles all error responses and maps them to appropriate error pages
 */
@Controller
public class ErrorHandlingConfig implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(ERROR_PATH)
    public String handleError(WebRequest webRequest, Model model) {
        try {
            Map<String, Object> attributes = new DefaultErrorAttributes().getErrorAttributes(
                webRequest, ErrorAttributeOptions.defaults()
            );
            
            Integer status = (Integer) attributes.get("status");
            String message = (String) attributes.get("message");
            String path = (String) attributes.get("path");
            Object timestamp = attributes.get("timestamp");
            
            // User-friendly error messages
            String userMessage = getFriendlyErrorMessage(status, message);
            
            // Add all attributes to model for template rendering
            model.addAttribute("status", status != null ? status : 500);
            model.addAttribute("message", userMessage);
            model.addAttribute("path", path != null ? path : "Unknown");
            model.addAttribute("timestamp", timestamp);
            model.addAttribute("error", true);
            
            // Return appropriate error page based on status
            if (status != null) {
                if (status == HttpStatus.NOT_FOUND.value()) {
                    return "error/404";
                } else if (status == HttpStatus.FORBIDDEN.value()) {
                    return "error/403";
                } else if (status == HttpStatus.UNAUTHORIZED.value()) {
                    return "error/403"; // Use 403 template for unauthorized
                } else if (status == HttpStatus.INTERNAL_SERVER_ERROR.value() || status >= 500) {
                    return "error/500";
                } else if (status >= 400 && status < 500) {
                    // For other 4xx errors, use generic error page
                    return "error/error";
                }
            }
            
            // Default fallback
            return "error/error";
        } catch (Exception e) {
            // If there's an error in error handling, use minimal error page
            model.addAttribute("status", 500);
            model.addAttribute("message", "An error occurred while processing your request.");
            model.addAttribute("error", true);
            return "error/error";
        }
    }
    
    private String getFriendlyErrorMessage(Integer status, String technicalMessage) {
        if (status == null) {
            return "An unexpected error occurred. Please try again later.";
        }
        
        switch (status) {
            case 400:
                return "Invalid request. Please check your input and try again.";
            case 401:
                return "You need to be logged in to access this resource.";
            case 403:
                return "You don't have permission to access this resource.";
            case 404:
                return "The page you're looking for doesn't exist.";
            case 405:
                return "This action is not allowed.";
            case 429:
                return "Too many requests. Please wait a moment and try again.";
            case 500:
                return "We're experiencing technical difficulties. Please try again later.";
            case 503:
                return "Service temporarily unavailable. Please try again later.";
            default:
                return "An error occurred. Please try again or contact support if the problem persists.";
        }
    }
}

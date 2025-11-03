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
 */
@Controller
public class ErrorHandlingConfig implements ErrorController {

    @RequestMapping("/error")
    public String handleError(WebRequest webRequest, Model model) {
        Map<String, Object> attributes = new DefaultErrorAttributes().getErrorAttributes(
            webRequest, ErrorAttributeOptions.defaults()
        );
        
        Integer status = (Integer) attributes.get("status");
        String message = (String) attributes.get("message");
        String path = (String) attributes.get("path");
        
        // User-friendly error messages
        String userMessage = getFriendlyErrorMessage(status, message);
        
        model.addAttribute("status", status);
        model.addAttribute("message", userMessage);
        model.addAttribute("path", path);
        model.addAttribute("error", true);
        
        // Return appropriate error page based on status
        if (status == HttpStatus.NOT_FOUND.value()) {
            return "error/404";
        } else if (status == HttpStatus.FORBIDDEN.value()) {
            return "error/403";
        } else if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return "error/500";
        }
        
        return "error/error";
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

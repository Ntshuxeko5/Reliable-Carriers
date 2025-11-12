package com.reliablecarriers.Reliable.Carriers.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global controller advice for adding common model attributes and handling template errors
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @ModelAttribute
    public void addAttributes(Model model) {
        try {
            // Add common attributes available to all views
            model.addAttribute("appName", "Reliable Carriers");
            model.addAttribute("appVersion", "1.0.0");
            model.addAttribute("appBaseUrl", baseUrl);
            
            // Add app object for nested property access (app.base.url)
            Map<String, Object> app = new HashMap<>();
            Map<String, String> base = new HashMap<>();
            base.put("url", baseUrl);
            app.put("base", base);
            model.addAttribute("app", app);
        } catch (Exception e) {
            // If there's an error adding attributes, log it but don't fail
            logger.warn("Error adding global model attributes: {}", e.getMessage());
        }
    }

    /**
     * Handle Thymeleaf template processing exceptions
     */
    @ExceptionHandler(TemplateProcessingException.class)
    public ModelAndView handleTemplateException(TemplateProcessingException ex) {
        logger.error("Template processing error: {}", ex.getMessage(), ex);
        
        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.addObject("status", 500);
        modelAndView.addObject("message", "We're experiencing technical difficulties. Please try again later.");
        modelAndView.addObject("error", true);
        
        return modelAndView;
    }
}

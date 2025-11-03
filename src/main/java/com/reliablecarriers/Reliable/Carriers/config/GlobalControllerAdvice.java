package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice for adding common model attributes
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addAttributes(Model model) {
        // Add common attributes available to all views
        model.addAttribute("appName", "Reliable Carriers");
        model.addAttribute("appVersion", "1.0.0");
        model.addAttribute("appBaseUrl", "http://localhost:8080"); // Should come from properties
    }
}

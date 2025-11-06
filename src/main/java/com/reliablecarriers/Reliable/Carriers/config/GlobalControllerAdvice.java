package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * Global controller advice for adding common model attributes
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @ModelAttribute
    public void addAttributes(Model model) {
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
    }
}

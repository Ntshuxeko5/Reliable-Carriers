package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * TermsController
 * Handles requests for the Terms and Conditions page.
 *
 * This controller maps the "/terms" route to the Thymeleaf template
 * located at src/main/resources/templates/terms.html
 */
@Controller
public class TermsController {

    @GetMapping("/terms")
    public String showTermsPage() {
        // returns "terms" -> Spring Boot looks for templates/terms.html
        return "terms";
    }

    @GetMapping("/privacy-policy")
    public String showPrivacyPolicyPage() {
        // returns "privacy-policy" -> Spring Boot looks for templates/privacy-policy.html
        return "privacy-policy";
    }
}

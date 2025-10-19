package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageResponse;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerWebController {

    private final CustomerPackageService customerPackageService;
    private final AuthService authService;

    @Autowired
    public CustomerWebController(CustomerPackageService customerPackageService, AuthService authService) {
        this.customerPackageService = customerPackageService;
        this.authService = authService;
    }

    // Main customer dashboard
    @GetMapping
    public String customerDashboard(Model model) {
        try {
            // Get current user information
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userPhone", currentUser.getPhone());
                model.addAttribute("isAuthenticated", true);
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            // If there's an error getting current user, treat as not authenticated
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/dashboard";
    }

    // Package tracking page
    @GetMapping("/track")
    public String trackPackage() {
        return "customer/track";
    }

    @PostMapping("/track")
    public String trackPackageResult(@RequestParam String trackingNumber, Model model) {
        try {
            if (customerPackageService.isValidTrackingNumber(trackingNumber)) {
                CustomerPackageResponse packageInfo = customerPackageService.getPackageByTrackingNumber(trackingNumber);
                model.addAttribute("package", packageInfo);
                model.addAttribute("found", true);
            } else {
                model.addAttribute("found", false);
                model.addAttribute("error", "Invalid tracking number format");
            }
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "Package not found");
        }
        return "customer/track";
    }

    // Quote creation page
    @GetMapping("/quote")
    public String createQuote() {
        return "customer/quote";
    }

    // Package management by email
    @GetMapping("/packages")
    public String managePackages(Model model) {
        // Check if user is authenticated
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, automatically load their packages
            try {
                List<CustomerPackageResponse> packages = customerPackageService.getPackagesByEmail(currentUser.getEmail());
                if (packages != null && !packages.isEmpty()) {
                    model.addAttribute("packages", packages);
                    model.addAttribute("email", currentUser.getEmail());
                    model.addAttribute("found", true);
                    model.addAttribute("isAuthenticated", true);
                } else {
                    model.addAttribute("packages", new ArrayList<>());
                    model.addAttribute("email", currentUser.getEmail());
                    model.addAttribute("found", false);
                    model.addAttribute("isAuthenticated", true);
                }
            } catch (Exception e) {
                model.addAttribute("packages", new ArrayList<>());
                model.addAttribute("email", currentUser.getEmail());
                model.addAttribute("found", false);
                model.addAttribute("isAuthenticated", true);
            }
        } else {
            // User is not logged in
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/packages";
    }

    @PostMapping("/packages")
    public String getPackagesByEmail(@RequestParam String email, Model model) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getPackagesByEmail(email);
            model.addAttribute("packages", packages);
            model.addAttribute("email", email);
            model.addAttribute("found", true);
            
            // Check if the email matches the logged-in user
            User currentUser = authService.getCurrentUser();
            model.addAttribute("isAuthenticated", currentUser != null);
            if (currentUser != null) {
                model.addAttribute("isOwnEmail", currentUser.getEmail().equals(email));
            }
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "No packages found for this email");
            
            User currentUser = authService.getCurrentUser();
            model.addAttribute("isAuthenticated", currentUser != null);
        }
        return "customer/packages";
    }

    // Package history
    @GetMapping("/history")
    public String packageHistory() {
        return "customer/history";
    }

    @PostMapping("/history")
    public String getPackageHistory(@RequestParam String email, 
                                   @RequestParam(defaultValue = "10") int limit, 
                                   Model model) {
        try {
            List<CustomerPackageResponse> packages = customerPackageService.getPackageHistory(email, limit);
            model.addAttribute("packages", packages);
            model.addAttribute("email", email);
            model.addAttribute("found", true);
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "No package history found");
        }
        return "customer/history";
    }

    // Package statistics
    @GetMapping("/statistics")
    public String packageStatistics() {
        return "customer/statistics";
    }

    @PostMapping("/statistics")
    public String getPackageStatistics(@RequestParam String email, Model model) {
        try {
            CustomerPackageService.PackageStatistics stats = customerPackageService.getPackageStatistics(email);
            model.addAttribute("statistics", stats);
            model.addAttribute("email", email);
            model.addAttribute("found", true);
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "No statistics available");
        }
        return "customer/statistics";
    }

    // Store/business package creation
    @GetMapping("/store")
    public String storePackageCreation() {
        return "customer/store";
    }

    // Package cancellation
    @GetMapping("/cancel")
    public String cancelPackage() {
        return "customer/cancel";
    }

    @PostMapping("/cancel")
    public String cancelPackageAction(@RequestParam String trackingNumber, 
                                     @RequestParam String email, 
                                     Model model) {
        try {
            boolean cancelled = customerPackageService.cancelPackage(trackingNumber, email);
            if (cancelled) {
                model.addAttribute("success", true);
                model.addAttribute("message", "Package cancelled successfully");
            } else {
                model.addAttribute("success", false);
                model.addAttribute("error", "Package cannot be cancelled");
            }
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage());
        }
        return "customer/cancel";
    }

    // Pickup request
    @GetMapping("/pickup")
    public String requestPickup() {
        return "customer/pickup";
    }

    @PostMapping("/pickup")
    public String requestPickupAction(@RequestParam String trackingNumber,
                                     @RequestParam String email,
                                     @RequestParam String preferredDate,
                                     @RequestParam(required = false) String notes,
                                     Model model) {
        try {
            customerPackageService.requestPickup(trackingNumber, email, preferredDate, notes);
            model.addAttribute("success", true);
            model.addAttribute("message", "Pickup request submitted successfully");
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage());
        }
        return "customer/pickup";
    }

    // Insurance options
    @GetMapping("/insurance")
    public String insuranceOptions() {
        return "customer/insurance";
    }

    @PostMapping("/insurance")
    public String getInsuranceOptions(@RequestParam String trackingNumber, Model model) {
        try {
            List<CustomerPackageService.InsuranceOption> options = customerPackageService.getInsuranceOptions(trackingNumber);
            model.addAttribute("options", options);
            model.addAttribute("trackingNumber", trackingNumber);
            model.addAttribute("found", true);
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "Package not found");
        }
        return "customer/insurance";
    }

    // Help and support
    @GetMapping("/help")
    public String help() {
        return "customer/help";
    }

    // Contact page
    @GetMapping("/contact")
    public String contact() {
        return "customer/contact";
    }

    // Profile page
    @GetMapping("/profile")
    public String profile() {
        return "customer/profile";
    }
}

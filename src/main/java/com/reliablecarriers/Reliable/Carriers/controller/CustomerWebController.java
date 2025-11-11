package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageResponse;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @GetMapping({"", "/dashboard"})
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
                
                // Check if user is a business
                boolean isBusiness = currentUser.getIsBusiness() != null && currentUser.getIsBusiness();
                model.addAttribute("isBusiness", isBusiness);
                
                // Load user's packages/bookings
                try {
                    List<CustomerPackageResponse> packages = customerPackageService.getPackagesByEmail(currentUser.getEmail());
                    if (packages != null && !packages.isEmpty()) {
                        // Get recent packages (limit to 6 for dashboard)
                        List<CustomerPackageResponse> recentPackages = packages.stream()
                                .limit(6)
                                .collect(Collectors.toList());
                        model.addAttribute("recentPackages", recentPackages);
                        
                        // Calculate statistics
                        long totalPackages = packages.size();
                        long deliveredCount = packages.stream()
                                .filter(p -> p.getStatus() != null && p.getStatus().toString().equalsIgnoreCase("DELIVERED"))
                                .count();
                        long inTransitCount = packages.stream()
                                .filter(p -> {
                                    if (p.getStatus() == null) return false;
                                    String status = p.getStatus().toString();
                                    return "IN_TRANSIT".equalsIgnoreCase(status) || 
                                           "OUT_FOR_DELIVERY".equalsIgnoreCase(status) ||
                                           "PICKED_UP".equalsIgnoreCase(status);
                                })
                                .count();
                        
                        model.addAttribute("totalPackages", totalPackages);
                        model.addAttribute("deliveredCount", deliveredCount);
                        model.addAttribute("inTransitCount", inTransitCount);
                    } else {
                        model.addAttribute("recentPackages", new ArrayList<>());
                        model.addAttribute("totalPackages", 0);
                        model.addAttribute("deliveredCount", 0);
                        model.addAttribute("inTransitCount", 0);
                    }
                } catch (Exception e) {
                    // If there's an error loading packages, set empty lists
                    model.addAttribute("recentPackages", new ArrayList<>());
                    model.addAttribute("totalPackages", 0);
                    model.addAttribute("deliveredCount", 0);
                    model.addAttribute("inTransitCount", 0);
                }
            } else {
                model.addAttribute("isAuthenticated", false);
                model.addAttribute("isBusiness", false);
            }
        } catch (Exception e) {
            // If there's an error getting current user, treat as not authenticated
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("isBusiness", false);
        }
        return "customer/dashboard";
    }

    // Package tracking page
    @GetMapping("/track")
    public String trackPackage() {
        return "customer/track";
    }

    // Package tracking page with tracking number in URL
    @GetMapping("/track/{trackingNumber}")
    public String trackPackageByNumber(@PathVariable String trackingNumber, Model model) {
        try {
            if (customerPackageService.isValidTrackingNumber(trackingNumber)) {
                CustomerPackageResponse packageInfo = customerPackageService.getPackageByTrackingNumber(trackingNumber);
                model.addAttribute("package", packageInfo);
                model.addAttribute("trackingNumber", trackingNumber);
                model.addAttribute("found", true);
            } else {
                model.addAttribute("found", false);
                model.addAttribute("trackingNumber", trackingNumber);
                model.addAttribute("error", "Invalid tracking number format");
            }
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("trackingNumber", trackingNumber);
            model.addAttribute("error", "Package not found");
        }
        return "customer/track";
    }

    @PostMapping("/track")
    public String trackPackageResult(@RequestParam String trackingNumber, Model model) {
        try {
            if (customerPackageService.isValidTrackingNumber(trackingNumber)) {
                CustomerPackageResponse packageInfo = customerPackageService.getPackageByTrackingNumber(trackingNumber);
                model.addAttribute("package", packageInfo);
                model.addAttribute("trackingNumber", trackingNumber);
                model.addAttribute("found", true);
            } else {
                model.addAttribute("found", false);
                model.addAttribute("trackingNumber", trackingNumber);
                model.addAttribute("error", "Invalid tracking number format");
            }
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("trackingNumber", trackingNumber);
            model.addAttribute("error", "Package not found");
        }
        return "customer/track";
    }

    // Business API Management page
    @GetMapping("/api-keys")
    public String apiKeysManagement(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("isBusiness", currentUser.getIsBusiness() != null && currentUser.getIsBusiness());
                model.addAttribute("isVerified", currentUser.getBusinessVerificationStatus() != null && 
                    currentUser.getBusinessVerificationStatus().isVerified());
            }
        } catch (Exception e) {
            model.addAttribute("isBusiness", false);
        }
        return "customer/api-keys";
    }

    // Quote creation page
    @GetMapping("/quote")
    public String createQuote(Model model) {
        try {
            // Check if user is authenticated via session or SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                
                // Try to get user
                try {
                    User currentUser = authService.getCurrentUser();
                    if (currentUser != null) {
                        model.addAttribute("isAuthenticated", true);
                        model.addAttribute("userName", currentUser.getFirstName());
                        model.addAttribute("userEmail", currentUser.getEmail());
                        return "customer/quote-logged-in";
                    }
                } catch (Exception e) {
                    // User might not exist, continue to public page
                }
            }
            
            // Also check session for user info (fallback for session-based auth)
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                String userEmail = (String) session.getAttribute("userEmail");
                Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
                if (userEmail != null && Boolean.TRUE.equals(isAuthenticated)) {
                    try {
                        User currentUser = authService.getCurrentUser();
                        if (currentUser != null) {
                            model.addAttribute("isAuthenticated", true);
                            model.addAttribute("userName", currentUser.getFirstName());
                            model.addAttribute("userEmail", currentUser.getEmail());
                            return "customer/quote-logged-in";
                        }
                    } catch (Exception e) {
                        // Continue to public page
                    }
                }
            }
        } catch (Exception e) {
            // If authentication fails, continue with default behavior
        }
        
        // Default to public quote page
        model.addAttribute("isAuthenticated", false);
        return "customer/quote";
    }

    // Quote logged-in page (alias for /quote when authenticated)
    @GetMapping("/quote-logged-in")
    public String createQuoteLoggedIn(Model model) {
        // Redirect to /quote which will show logged-in version if authenticated
        return createQuote(model);
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
                    model.addAttribute("userName", currentUser.getFirstName());
                    model.addAttribute("found", true);
                    model.addAttribute("isAuthenticated", true);
                } else {
                    model.addAttribute("packages", new ArrayList<>());
                    model.addAttribute("email", currentUser.getEmail());
                    model.addAttribute("userName", currentUser.getFirstName());
                    model.addAttribute("found", false);
                    model.addAttribute("isAuthenticated", true);
                }
            } catch (Exception e) {
                model.addAttribute("packages", new ArrayList<>());
                model.addAttribute("email", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
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

    // Analytics page
    @GetMapping("/analytics")
    public String analytics(Model model) {
        try {
            // Get current user information
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userName", currentUser.getFirstName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("user", currentUser);
                model.addAttribute("customerTier", currentUser.getCustomerTier());
                return "customer/analytics";
            }
        } catch (Exception e) {
            // If authentication fails, redirect to login
        }
        
        // Redirect to login if not authenticated
        return "redirect:/login";
    }

    // Profile page
    @GetMapping("/profile")
    public String profile(Model model) {
        try {
            // Get current user information
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userName", currentUser.getFirstName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("user", currentUser);
                return "customer/profile";
            }
        } catch (Exception e) {
            // If authentication fails, redirect to login
        }
        
        // Redirect to login if not authenticated
        return "redirect:/login";
    }
}

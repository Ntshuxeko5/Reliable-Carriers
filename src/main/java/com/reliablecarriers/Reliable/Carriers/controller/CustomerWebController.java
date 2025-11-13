package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageResponse;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.CustomerPackageService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
public class CustomerWebController {

    private final CustomerPackageService customerPackageService;
    private final AuthService authService;

    public CustomerWebController(CustomerPackageService customerPackageService, AuthService authService) {
        this.customerPackageService = customerPackageService;
        this.authService = authService;
    }

    // Helper method to create navbar links for business pages
    private List<Map<String, Object>> createBusinessNavLinks(String activePage) {
        List<Map<String, Object>> links = new ArrayList<>();
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("label", "Dashboard");
        dashboard.put("url", "/business/dashboard");
        dashboard.put("active", "dashboard".equals(activePage));
        links.add(dashboard);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("label", "Analytics");
        analytics.put("url", "/business/analytics");
        analytics.put("active", "analytics".equals(activePage));
        links.add(analytics);
        
        Map<String, Object> invoices = new HashMap<>();
        invoices.put("label", "Invoices");
        invoices.put("url", "/customer/invoices");
        invoices.put("active", "invoices".equals(activePage));
        links.add(invoices);
        
        Map<String, Object> webhooks = new HashMap<>();
        webhooks.put("label", "Webhooks");
        webhooks.put("url", "/customer/webhooks");
        webhooks.put("active", "webhooks".equals(activePage));
        links.add(webhooks);
        
        Map<String, Object> apiKeys = new HashMap<>();
        apiKeys.put("label", "API Keys");
        apiKeys.put("url", "/customer/api-keys");
        apiKeys.put("active", "api-keys".equals(activePage));
        links.add(apiKeys);
        
        Map<String, Object> support = new HashMap<>();
        support.put("label", "Support");
        support.put("url", "/customer/support");
        support.put("active", "support".equals(activePage));
        links.add(support);
        
        Map<String, Object> loginHistory = new HashMap<>();
        loginHistory.put("label", "Login History");
        loginHistory.put("url", "/customer/login-history");
        loginHistory.put("active", "login-history".equals(activePage));
        links.add(loginHistory);
        
        Map<String, Object> logout = new HashMap<>();
        logout.put("label", "Logout");
        logout.put("url", "/logout");
        logout.put("active", false);
        logout.put("id", "logoutBtn");
        links.add(logout);
        
        return links;
    }

    // Helper method to create navbar links for customer pages
    private List<Map<String, Object>> createCustomerNavLinks(String activePage) {
        List<Map<String, Object>> links = new ArrayList<>();
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("label", "Dashboard");
        dashboard.put("url", "/customer/dashboard");
        dashboard.put("active", "dashboard".equals(activePage));
        links.add(dashboard);
        
        Map<String, Object> packages = new HashMap<>();
        packages.put("label", "My Packages");
        packages.put("url", "/customer/packages");
        packages.put("active", "packages".equals(activePage));
        links.add(packages);
        
        Map<String, Object> quote = new HashMap<>();
        quote.put("label", "Get Quote");
        // Check if user is authenticated - if so, link to quote-logged-in
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                quote.put("url", "/customer/quote-logged-in");
            } else {
                quote.put("url", "/customer/quote");
            }
        } catch (Exception e) {
            // If error, default to public quote page
            quote.put("url", "/customer/quote");
        }
        quote.put("active", "quote".equals(activePage));
        links.add(quote);
        
        Map<String, Object> movingServices = new HashMap<>();
        movingServices.put("label", "Moving Services");
        // Check if user is authenticated - if so, link to customer version
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                movingServices.put("url", "/customer/moving-services");
            } else {
                movingServices.put("url", "/moving-services");
            }
        } catch (Exception e) {
            // If error, default to public moving services page
            movingServices.put("url", "/moving-services");
        }
        movingServices.put("active", "moving-services".equals(activePage));
        links.add(movingServices);
        
        // Add business-specific links only for business users
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getIsBusiness() != null && currentUser.getIsBusiness()) {
                // Business users see business dashboard link
                Map<String, Object> businessDashboard = new HashMap<>();
                businessDashboard.put("label", "Business");
                businessDashboard.put("url", "/business/dashboard");
                businessDashboard.put("active", false);
                links.add(businessDashboard);
            }
        } catch (Exception e) {
            // Ignore errors
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("label", "Profile");
        profile.put("url", "/customer/profile");
        profile.put("active", "profile".equals(activePage));
        links.add(profile);
        
        Map<String, Object> logout = new HashMap<>();
        logout.put("label", "Logout");
        logout.put("url", "/logout");
        logout.put("active", false);
        logout.put("id", "logoutBtn");
        links.add(logout);
        
        return links;
    }

    // Main customer dashboard
    @GetMapping({"", "/dashboard"})
    public String customerDashboard(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("dashboard"));
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
                    String userEmail = currentUser.getEmail();
                    if (userEmail == null || userEmail.trim().isEmpty()) {
                        throw new IllegalArgumentException("User email is missing");
                    }
                    List<CustomerPackageResponse> packages = customerPackageService.getPackagesByEmail(userEmail);
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
    public String trackPackage(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("track"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/track";
    }

    // Package tracking page with tracking number in URL
    @GetMapping("/track/{trackingNumber}")
    public String trackPackageByNumber(@PathVariable String trackingNumber, Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("track"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
            
            if (customerPackageService.isValidTrackingNumber(trackingNumber)) {
                try {
                    CustomerPackageResponse packageInfo = customerPackageService.getPackageByTrackingNumber(trackingNumber);
                    model.addAttribute("package", packageInfo);
                    model.addAttribute("trackingNumber", trackingNumber);
                    model.addAttribute("found", true);
                } catch (Exception e) {
                    model.addAttribute("found", false);
                    model.addAttribute("trackingNumber", trackingNumber);
                    model.addAttribute("error", "Package not found");
                }
            } else {
                model.addAttribute("found", false);
                model.addAttribute("trackingNumber", trackingNumber);
                model.addAttribute("error", "Invalid tracking number format");
            }
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("trackingNumber", trackingNumber);
            model.addAttribute("error", "Package not found");
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/track";
    }

    @PostMapping("/track")
    public String trackPackageResult(@RequestParam String trackingNumber, Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("track"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
            
            if (customerPackageService.isValidTrackingNumber(trackingNumber)) {
                try {
                    CustomerPackageResponse packageInfo = customerPackageService.getPackageByTrackingNumber(trackingNumber);
                    model.addAttribute("package", packageInfo);
                    model.addAttribute("trackingNumber", trackingNumber);
                    model.addAttribute("found", true);
                } catch (Exception e) {
                    model.addAttribute("found", false);
                    model.addAttribute("trackingNumber", trackingNumber);
                    model.addAttribute("error", "Package not found");
                }
            } else {
                model.addAttribute("found", false);
                model.addAttribute("trackingNumber", trackingNumber);
                model.addAttribute("error", "Invalid tracking number format");
            }
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("trackingNumber", trackingNumber);
            model.addAttribute("error", "Package not found");
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/track";
    }

    // Business API Management page
    @GetMapping("/api-keys")
    public String apiKeysManagement(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("api-keys"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("isBusiness", currentUser.getIsBusiness() != null && currentUser.getIsBusiness());
                model.addAttribute("isVerified", currentUser.getBusinessVerificationStatus() != null && 
                    currentUser.getBusinessVerificationStatus().toString().equals("APPROVED"));
            } else {
                model.addAttribute("isBusiness", false);
                model.addAttribute("isVerified", false);
            }
        } catch (Exception e) {
            model.addAttribute("isBusiness", false);
            model.addAttribute("isVerified", false);
        }
        return "business/api-keys";
    }

    // Quote creation page
    @GetMapping("/quote")
    public String createQuote(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("quote"));
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
            try {
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
                // RequestContextHolder not available, continue to public page
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
        model.addAttribute("navLinks", createCustomerNavLinks("quote"));
        // Redirect to /quote which will show logged-in version if authenticated
        return createQuote(model);
    }

    // Moving services page - routes to logged-in version if authenticated
    @GetMapping("/moving-services")
    public String movingServices(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("moving-services"));
        try {
            // Check if user is authenticated
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                
                try {
                    User currentUser = authService.getCurrentUser();
                    if (currentUser != null) {
                        model.addAttribute("isAuthenticated", true);
                        model.addAttribute("userName", currentUser.getFirstName());
                        model.addAttribute("userEmail", currentUser.getEmail());
                        model.addAttribute("userPhone", currentUser.getPhone());
                        model.addAttribute("userId", currentUser.getId());
                        return "customer/moving-services-logged-in";
                    }
                } catch (Exception e) {
                    // User might not exist, continue to public page
                }
            }
        } catch (Exception e) {
            // If authentication fails, continue with default behavior
        }
        
        // Default to public moving services page
        model.addAttribute("isAuthenticated", false);
        return "redirect:/moving-services";
    }

    // Package management by email
    @GetMapping("/packages")
    public String managePackages(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("packages"));
        // Check if user is authenticated
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, automatically load their packages
            try {
                String userEmail = currentUser.getEmail();
                if (userEmail == null || userEmail.trim().isEmpty()) {
                    throw new IllegalArgumentException("User email is missing");
                }
                List<CustomerPackageResponse> packages = customerPackageService.getPackagesByEmail(userEmail);
                if (packages != null && !packages.isEmpty()) {
                    model.addAttribute("packages", packages);
                    model.addAttribute("email", userEmail);
                    model.addAttribute("userName", currentUser.getFirstName());
                    model.addAttribute("found", true);
                    model.addAttribute("isAuthenticated", true);
                } else {
                    model.addAttribute("packages", new ArrayList<>());
                    model.addAttribute("email", userEmail);
                    model.addAttribute("userName", currentUser.getFirstName());
                    model.addAttribute("found", false);
                    model.addAttribute("isAuthenticated", true);
                }
            } catch (Exception e) {
                // Log error but don't fail the page
                model.addAttribute("packages", new ArrayList<>());
                String userEmail = currentUser.getEmail() != null ? currentUser.getEmail() : "";
                model.addAttribute("email", userEmail);
                model.addAttribute("userName", currentUser.getFirstName());
                model.addAttribute("found", false);
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("error", "Unable to load packages. Please try again.");
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
            model.addAttribute("navLinks", createCustomerNavLinks("packages"));
            List<CustomerPackageResponse> packages = customerPackageService.getPackagesByEmail(email);
            if (packages == null) {
                packages = new ArrayList<>();
            }
            model.addAttribute("packages", packages);
            model.addAttribute("email", email);
            model.addAttribute("found", !packages.isEmpty());
            
            // Check if the email matches the logged-in user
            User currentUser = authService.getCurrentUser();
            model.addAttribute("isAuthenticated", currentUser != null);
            if (currentUser != null) {
                model.addAttribute("isOwnEmail", currentUser.getEmail() != null && currentUser.getEmail().equals(email));
                model.addAttribute("userName", currentUser.getFirstName());
            }
        } catch (Exception e) {
            model.addAttribute("packages", new ArrayList<>());
            model.addAttribute("found", false);
            model.addAttribute("error", "Unable to load packages. Please try again.");
            
            User currentUser = authService.getCurrentUser();
            model.addAttribute("isAuthenticated", currentUser != null);
            if (currentUser != null) {
                model.addAttribute("userName", currentUser.getFirstName());
            }
        }
        return "customer/packages";
    }

    // Package history
    @GetMapping("/history")
    public String packageHistory(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("history"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/history";
    }

    @PostMapping("/history")
    public String getPackageHistory(@RequestParam String email, 
                                   @RequestParam(defaultValue = "10") int limit, 
                                   Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("history"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
            
            List<CustomerPackageResponse> packages = customerPackageService.getPackageHistory(email, limit);
            if (packages == null) {
                packages = new ArrayList<>();
            }
            model.addAttribute("packages", packages);
            model.addAttribute("email", email);
            model.addAttribute("found", !packages.isEmpty());
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "No package history found");
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/history";
    }

    // Package statistics
    @GetMapping("/statistics")
    public String packageStatistics(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("statistics"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/statistics";
    }

    @PostMapping("/statistics")
    public String getPackageStatistics(@RequestParam String email, Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("statistics"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
            
            CustomerPackageService.PackageStatistics stats = customerPackageService.getPackageStatistics(email);
            if (stats != null) {
                model.addAttribute("statistics", stats);
                model.addAttribute("found", true);
            } else {
                model.addAttribute("found", false);
                model.addAttribute("error", "No statistics available");
            }
            model.addAttribute("email", email);
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "No statistics available");
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/statistics";
    }

    // Store/business package creation
    @GetMapping("/store")
    public String storePackageCreation(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("store"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/store";
    }

    //Track Package
   @GetMapping("/tier-aware-track")
public String showTierAwareTrackPage(Model model) {
    model.addAttribute("navLinks", createCustomerNavLinks());
    
    try {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
        } else {
            model.addAttribute("isAuthenticated", false);
        }
    } catch (Exception e) {
        model.addAttribute("isAuthenticated", false);
    }

    // 👇 this must match your file structure
    return "customer/tier-aware-track";
}


    // Package cancellation
    @GetMapping("/cancel")
    public String cancelPackage(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("cancel"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/cancel";
    }

    @PostMapping("/cancel")
    public String cancelPackageAction(@RequestParam String trackingNumber, 
                                     @RequestParam String email, 
                                     Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("cancel"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
            
            try {
                boolean cancelled = customerPackageService.cancelPackage(trackingNumber, email);
                if (cancelled) {
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Package cancelled successfully");
                } else {
                    model.addAttribute("success", false);
                    model.addAttribute("error", "Package cannot be cancelled");
                }
            } catch (IllegalArgumentException e) {
                model.addAttribute("success", false);
                model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Invalid cancellation request");
            } catch (RuntimeException e) {
                model.addAttribute("success", false);
                model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Package not found");
            }
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Failed to cancel package");
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/cancel";
    }

    // Payment history page
    @GetMapping("/payments")
    public String paymentHistory(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("payments"));
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
            }
        } catch (Exception e) {
            // User not authenticated or error getting user
        }
        return "customer/payments";
    }

    // Business invoices page
    @GetMapping("/invoices")
    public String invoices(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("invoices"));
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("isBusiness", currentUser.getIsBusiness() != null && currentUser.getIsBusiness());
            } else {
                return "redirect:/login";
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "business/invoices";
    }

    // Webhook management page
    @GetMapping("/webhooks")
    public String webhooks(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("webhooks"));
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("isBusiness", currentUser.getIsBusiness() != null && currentUser.getIsBusiness());
                if (currentUser.getIsBusiness() == null || !currentUser.getIsBusiness()) {
                    return "redirect:/customer/dashboard";
                }
            } else {
                return "redirect:/login";
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "business/webhooks";
    }

    // Pickup request
    @GetMapping("/pickup")
    public String requestPickup(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("pickup"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/pickup";
    }

    @PostMapping("/pickup")
    public String requestPickupAction(@RequestParam String trackingNumber,
                                     @RequestParam String email,
                                     @RequestParam String preferredDate,
                                     @RequestParam(required = false) String notes,
                                     Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("pickup"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
            
            try {
                customerPackageService.requestPickup(trackingNumber, email, preferredDate, notes);
                model.addAttribute("success", true);
                model.addAttribute("message", "Pickup request submitted successfully");
            } catch (IllegalArgumentException e) {
                model.addAttribute("success", false);
                model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Invalid pickup request");
            } catch (RuntimeException e) {
                model.addAttribute("success", false);
                model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Package not found");
            }
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Failed to submit pickup request");
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/pickup";
    }

    // Insurance options
    @GetMapping("/insurance")
    public String insuranceOptions(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("insurance"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/insurance";
    }

    @PostMapping("/insurance")
    public String getInsuranceOptions(@RequestParam String trackingNumber, Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("insurance"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
            
            List<CustomerPackageService.InsuranceOption> options = customerPackageService.getInsuranceOptions(trackingNumber);
            if (options == null) {
                options = new ArrayList<>();
            }
            model.addAttribute("options", options);
            model.addAttribute("trackingNumber", trackingNumber);
            model.addAttribute("found", !options.isEmpty());
        } catch (Exception e) {
            model.addAttribute("found", false);
            model.addAttribute("error", "Package not found");
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/insurance";
    }

    // Help and support
    @GetMapping("/help")
    public String help(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("help"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/help";
    }

    // Contact page
    @GetMapping("/contact")
    public String contact(Model model) {
        try {
            model.addAttribute("navLinks", createCustomerNavLinks("contact"));
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userName", currentUser.getFirstName());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "customer/contact";
    }

    // Analytics page - BUSINESS ONLY (redirects to business analytics)
    @GetMapping("/analytics")
    public String analytics(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                // Check if user is a business - redirect to business analytics
                if (currentUser.getIsBusiness() != null && currentUser.getIsBusiness()) {
                    return "redirect:/business/analytics";
                }
            }
        } catch (Exception e) {
            // If error, redirect to dashboard
        }
        // Regular customers don't have analytics - redirect to dashboard
        return "redirect:/customer/dashboard";
    }

    // Support tickets page - BUSINESS ONLY
    @GetMapping("/support")
    public String supportTickets(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user is a business
            if (currentUser.getIsBusiness() != null && currentUser.getIsBusiness()) {
                model.addAttribute("navLinks", createBusinessNavLinks("support"));
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                return "business/support";
            } else {
                // Regular customers don't have access to support page
                return "redirect:/customer/dashboard";
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    // Login history page - BUSINESS ONLY
    @GetMapping("/login-history")
    public String loginHistory(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user is a business
            if (currentUser.getIsBusiness() != null && currentUser.getIsBusiness()) {
                model.addAttribute("navLinks", createBusinessNavLinks("login-history"));
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                return "business/login-history";
            } else {
                // Regular customers don't have access to login history page
                return "redirect:/customer/dashboard";
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    // Tier-aware tracking page - BUSINESS ONLY
    @GetMapping("/tier-aware-track")
    public String tierAwareTrack(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if user is a business
            if (currentUser.getIsBusiness() != null && currentUser.getIsBusiness()) {
                model.addAttribute("navLinks", createBusinessNavLinks("tier-aware-track"));
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("customerTier", currentUser.getCustomerTier());
                return "business/tier-aware-track";
            } else {
                // Regular customers don't have access to tier-aware tracking
                return "redirect:/customer/track";
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    // Profile page
    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("navLinks", createCustomerNavLinks("profile"));
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
    private List<Map<String, String>> createCustomerNavLinks() {
    List<Map<String, String>> navLinks = new ArrayList<>();

    navLinks.add(Map.of("label", "Dashboard", "url", "/customer/dashboard"));
    navLinks.add(Map.of("label", "Packages", "url", "/customer/packages"));
    navLinks.add(Map.of("label", "Track", "url", "/customer/track"));
    navLinks.add(Map.of("label", "Statistics", "url", "/customer/statistics"));
    navLinks.add(Map.of("label", "Store", "url", "/customer/store"));
    navLinks.add(Map.of("label", "Cancel", "url", "/customer/cancel"));

    // Optional: show business-only links if the current user is a business account
    try {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && Boolean.TRUE.equals(currentUser.getIsBusiness())) {
            navLinks.add(Map.of("label", "Invoices", "url", "/business/invoices"));
            navLinks.add(Map.of("label", "Webhooks", "url", "/business/webhooks"));
        }
    } catch (Exception e) {
        // ignore authentication errors for nav bar
    }

    return navLinks;
}

}

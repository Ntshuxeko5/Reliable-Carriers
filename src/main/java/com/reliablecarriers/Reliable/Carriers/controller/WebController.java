package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.model.DriverVerificationStatus;
import com.reliablecarriers.Reliable.Carriers.model.BusinessVerificationStatus;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private final AuthService authService;

    @Autowired
    public WebController(@Lazy AuthService authService) {
        this.authService = authService;
    }

    // Helper method to create navbar links for public pages
    private List<Map<String, Object>> createPublicNavLinks() {
        List<Map<String, Object>> links = new ArrayList<>();
        
        Map<String, Object> tracking = new HashMap<>();
        tracking.put("label", "Track Package");
        tracking.put("url", "/tracking");
        links.add(tracking);
        
        Map<String, Object> business = new HashMap<>();
        business.put("label", "Business Solutions");
        business.put("url", "/register/business");
        links.add(business);
        
        Map<String, Object> driver = new HashMap<>();
        driver.put("label", "Become a Driver");
        driver.put("url", "/register/driver");
        links.add(driver);
        
        Map<String, Object> about = new HashMap<>();
        about.put("label", "About");
        about.put("url", "/about");
        links.add(about);
        
        Map<String, Object> contact = new HashMap<>();
        contact.put("label", "Contact");
        contact.put("url", "/contact");
        links.add(contact);
        
        Map<String, Object> login = new HashMap<>();
        login.put("label", "Login");
        login.put("url", "/login");
        links.add(login);
        
        Map<String, Object> register = new HashMap<>();
        register.put("label", "Register");
        register.put("url", "/register");
        links.add(register);
        
        return links;
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
        
        // Business-specific pages (invoices, webhooks, API keys)
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

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        try {
            List<Map<String, Object>> navLinks = createPublicNavLinks();
            // Always add navLinks to model (even if null) to avoid Thymeleaf template errors
            model.addAttribute("navLinks", navLinks != null && !navLinks.isEmpty() ? navLinks : null);
            // Simple login page - no authentication check needed
            return "login";
        } catch (Exception e) {
            // If there's an error creating nav links, still show the page with default navigation
            // The navbar fragment will use default links if navLinks is null
            logger.error("Error loading login page: {}", e.getMessage(), e);
            // Explicitly set navLinks to null to avoid template errors
            model.addAttribute("navLinks", null);
            return "login";
        }
    }

    @GetMapping("/staff-login")
    public String staffLogin() {
        // Simple staff login page - no authentication check needed
        return "staff-login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        try {
            List<Map<String, Object>> navLinks = createPublicNavLinks();
            // Always add navLinks to model (even if null) to avoid Thymeleaf template errors
            model.addAttribute("navLinks", navLinks != null && !navLinks.isEmpty() ? navLinks : null);
            // Simple register page - no authentication check needed
            return "register";
        } catch (Exception e) {
            // If there's an error creating nav links, still show the page with default navigation
            // The navbar fragment will use default links if navLinks is null
            logger.error("Error loading register page: {}", e.getMessage(), e);
            // Explicitly set navLinks to null to avoid template errors
            model.addAttribute("navLinks", null);
            return "register";
        }
    }

    @GetMapping("/register/business")
    public String registerBusiness() {
        return "register-business";
    }

    @GetMapping("/register/driver")
    public String registerDriver() {
        return "register-driver";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        try {
            // Get user from authentication
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }

            // Check verification status for drivers and business users
            if (currentUser.getRole() == UserRole.DRIVER) {
                if (currentUser.getDriverVerificationStatus() == null || 
                    currentUser.getDriverVerificationStatus() == DriverVerificationStatus.PENDING) {
                    return "redirect:/waiting-approval";
                }
                return "redirect:/driver/dashboard";
            } else if (Boolean.TRUE.equals(currentUser.getIsBusiness())) {
                if (currentUser.getBusinessVerificationStatus() == null || 
                    currentUser.getBusinessVerificationStatus() == BusinessVerificationStatus.PENDING) {
                    return "redirect:/waiting-approval";
                }
                // Business users can access both customer and business dashboards
                // Default to customer dashboard which has more features
                return "redirect:/customer";
            }

            // Redirect based on role
            switch (currentUser.getRole()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case DRIVER:
                    return "redirect:/driver/dashboard";
                case TRACKING_MANAGER:
                    return "redirect:/tracking/dashboard";
                case STAFF:
                    return "redirect:/staff/dashboard";
                case CUSTOMER:
                default:
                    return "redirect:/customer";
            }
        } catch (Exception e) {
            // If authentication fails, redirect to login
            return "redirect:/login";
        }
    }

    @GetMapping("/admin")
    public String adminPanel() {
        return "admin";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/staff/dashboard")
    public String staffDashboard() {
        return "staff/dashboard";
    }

    @GetMapping("/tracking/{trackingNumber}")
    public String tracking(@PathVariable String trackingNumber, Model model) {
        model.addAttribute("trackingNumber", trackingNumber);
        return "tracking/basic";
    }

    @GetMapping("/tracking")
    public String tracking() {
        return "tracking/basic";
    }
    
    @GetMapping("/tracking/basic")
    public String basicTracking() {
        return "tracking/basic";
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public String notifications() {
        return "notifications";
    }

    @GetMapping("/services")
    public String services(Model model) {
        model.addAttribute("navLinks", createPublicNavLinks());
        return "services";
    }

    @GetMapping("/moving-services")
    public String movingServices(Model model) {
        model.addAttribute("navLinks", createPublicNavLinks());
        // Check if user is authenticated - route to logged-in version if so
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                // Redirect to customer version which handles logged-in state
                return "redirect:/customer/moving-services";
            }
        } catch (Exception e) {
            // User not authenticated, continue to public version
        }
        return "moving-services";
    }

    @GetMapping("/business/dashboard")
    public String businessDashboard(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Verify user is a business user
            if (currentUser.getIsBusiness() == null || !currentUser.getIsBusiness()) {
                return "redirect:/customer/dashboard";
            }
            
            // Check verification status
            if (currentUser.getBusinessVerificationStatus() == null || 
                currentUser.getBusinessVerificationStatus() == BusinessVerificationStatus.PENDING) {
                return "redirect:/waiting-approval";
            }
            
            model.addAttribute("navLinks", createBusinessNavLinks("dashboard"));
            model.addAttribute("user", currentUser);
            return "business/dashboard";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/business/analytics")
    public String businessAnalytics(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Verify user is a business user
            if (currentUser.getIsBusiness() == null || !currentUser.getIsBusiness()) {
                return "redirect:/customer/analytics";
            }
            
            // Check verification status
            if (currentUser.getBusinessVerificationStatus() == null || 
                currentUser.getBusinessVerificationStatus() == BusinessVerificationStatus.PENDING) {
                return "redirect:/waiting-approval";
            }
            
            model.addAttribute("navLinks", createBusinessNavLinks("analytics"));
            model.addAttribute("user", currentUser);
            return "business/analytics";
        } catch (Exception e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/shipments")
    public String shipments() {
        return "shipments";
    }

    @GetMapping("/drivers")
    public String drivers() {
        return "drivers";
    }

    @GetMapping("/vehicles")
    public String vehicles() {
        return "vehicles";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/booking")
    public String booking(Model model) {
        model.addAttribute("navLinks", createPublicNavLinks());
        try {
            // Check if user is authenticated and get user data
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userPhone", currentUser.getPhone());
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            // If authentication check fails, continue without user data
            model.addAttribute("isAuthenticated", false);
        }
        return "booking";
    }
    
    @GetMapping("/driver/workboard")
    public String driverWorkboard() {
        return "driver/workboard";
    }
    
    @GetMapping("/admin/driver-management")
    public String adminDriverManagement() {
        return "admin/driver-management";
    }
    
    @GetMapping("/admin/analytics")
    public String adminAnalytics() {
        return "admin/analytics";
    }
    
    @GetMapping("/admin/audit")
    public String adminAudit() {
        return "admin/audit";
    }
    
    @GetMapping("/admin/verification")
    public String adminVerification() {
        return "admin/verification-management";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/price-list")
    public String priceList() {
        return "price-list";
    }

    @GetMapping("/waiting-approval")
    public String waitingApproval(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userRole", currentUser.getRole().toString());
            }
        } catch (Exception e) {
            // Handle error
        }
        return "waiting-approval";
    }

    @GetMapping("/driver/dashboard")
    public String driverDashboard(HttpServletRequest request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Check if driver is approved
            if (currentUser.getRole() == UserRole.DRIVER) {
                if (currentUser.getDriverVerificationStatus() == null || 
                    currentUser.getDriverVerificationStatus() == DriverVerificationStatus.PENDING) {
                    return "redirect:/waiting-approval";
                }
            }
        } catch (Exception e) {
            return "redirect:/login";
        }
        return "driver/dashboard";
    }

    @GetMapping("/driver/earnings")
    @PreAuthorize("hasRole('DRIVER')")
    public String driverEarnings() {
        return "driver/earnings";
    }

    @GetMapping("/driver/profile")
    @PreAuthorize("hasRole('DRIVER')")
    public String driverProfile(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
                model.addAttribute("userPhone", currentUser.getPhone());
            }
        } catch (Exception e) {
            // Handle error
        }
        return "driver/profile";
    }

    @GetMapping("/driver/login-history")
    @PreAuthorize("hasRole('DRIVER')")
    public String driverLoginHistory(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
                model.addAttribute("userEmail", currentUser.getEmail());
            }
        } catch (Exception e) {
            // Handle error
        }
        return "driver/login-history";
    }

    @GetMapping("/admin/support-tickets")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public String adminSupportTickets(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
            }
        } catch (Exception e) {
            // Handle error
        }
        return "admin/support-tickets";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsers(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
            }
        } catch (Exception e) {
            // Handle error
        }
        return "admin/users";
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPayments(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
            }
        } catch (Exception e) {
            // Handle error
        }
        return "admin/payments";
    }

    @GetMapping("/admin/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminReports(Model model) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("userName", currentUser.getFirstName() + " " + currentUser.getLastName());
            }
        } catch (Exception e) {
            // Handle error
        }
        return "admin/reports";
    }

    @GetMapping("/driver/uber-dashboard")
    @PreAuthorize("hasRole('DRIVER')")
    public String driverUberDashboard() {
        return "driver/uber-dashboard";
    }

    @GetMapping("/payment")
    public String payment(Model model) {
        // Get current user information
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
                String email = auth.getName();
                // You can add user lookup here if needed
                model.addAttribute("userEmail", email);
                model.addAttribute("isAuthenticated", true);
            } else {
                model.addAttribute("isAuthenticated", false);
            }
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
        return "payment";
    }
    
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.trim().isEmpty()) {
            // If no token provided, redirect to forgot password page
            return "redirect:/forgot-password?error=invalid_token";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }
    
    @GetMapping("/payment-success")
    public String paymentSuccess() {
        return "payment-success";
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation() {
        return "booking-confirmation";
    }

    @GetMapping("/help-center")
    public String helpCenter() {
        return "help-center";
    }
    
    // Documentation endpoints - serve markdown files as HTML
    @GetMapping("/docs")
    public String docsIndex() {
        return "redirect:/help-center#documentation";
    }
    
    @GetMapping("/docs/{manual}")
    public String viewManual(@PathVariable String manual, Model model) {
        // Map URL-friendly names to actual file names
        String manualName = switch(manual) {
            case "customer-manual" -> "Customer Manual";
            case "driver-manual" -> "Driver Manual";
            case "admin-manual" -> "Admin Manual";
            case "business-manual" -> "Business Manual";
            case "quick-start" -> "Quick Start Guide";
            case "api-documentation" -> "API Documentation";
            default -> null;
        };
        
        String fileName = switch(manual) {
            case "customer-manual" -> "CUSTOMER_MANUAL";
            case "driver-manual" -> "DRIVER_MANUAL";
            case "admin-manual" -> "ADMIN_MANUAL";
            case "business-manual" -> "BUSINESS_MANUAL";
            case "quick-start" -> "QUICK_START_GUIDE";
            case "api-documentation" -> "API_DOCUMENTATION";
            default -> null;
        };
        
        if (manualName != null && fileName != null) {
            model.addAttribute("manualName", manualName);
            model.addAttribute("manualPath", "/docs/" + fileName + ".md");
            return "docs/viewer";
        }
        
        return "redirect:/help-center";
    }
}
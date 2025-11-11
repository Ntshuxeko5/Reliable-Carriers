package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    private final AuthService authService;

    @Autowired
    public WebController(@Lazy AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        // Simple login page - no authentication check needed
        return "login";
    }

    @GetMapping("/staff-login")
    public String staffLogin() {
        // Simple staff login page - no authentication check needed
        return "staff-login";
    }

    @GetMapping("/register")
    public String register() {
        // Simple register page - no authentication check needed
        return "register";
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
        // Get user role from session or authentication
        String userRole = null;
        
        // Try to get from session first
        HttpSession session = request.getSession(false);
        if (session != null) {
            userRole = (String) session.getAttribute("userRole");
        }
        
        // If no session, try to get from authentication
        if (userRole == null) {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    // Extract role from authorities
                    for (GrantedAuthority authority : userDetails.getAuthorities()) {
                        if (authority.getAuthority().startsWith("ROLE_")) {
                            userRole = authority.getAuthority().substring(5); // Remove "ROLE_" prefix
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // If authentication fails, continue with default behavior
            }
        }
        
        // Redirect based on role
        if (userRole != null) {
            switch (userRole) {
                case "ADMIN":
                    return "redirect:/admin/dashboard";
                case "DRIVER":
                    return "redirect:/driver/dashboard";
                case "TRACKING_MANAGER":
                    return "redirect:/tracking/dashboard";
                case "STAFF":
                    return "redirect:/staff/dashboard";
                case "CUSTOMER":
                default:
                    return "redirect:/customer";
            }
        }
        
        // Default fallback
        return "redirect:/customer";
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

    @GetMapping("/moving-services")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public String movingServices() {
        return "moving-services";
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

    @GetMapping("/driver/dashboard")
    public String driverDashboard() {
        return "driver/dashboard";
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
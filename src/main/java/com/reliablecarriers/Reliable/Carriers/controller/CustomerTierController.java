package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.CustomerTier;
import com.reliablecarriers.Reliable.Carriers.model.CustomerTierFeatures;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.CustomerTierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/tier")
public class CustomerTierController {

    @Autowired
    private CustomerTierService customerTierService;

    @Autowired
    private AuthService authService;

    /**
     * Get current user's tier information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getTierInfo() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            CustomerTierFeatures features = customerTierService.getTierFeatures(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("tier", currentUser.getCustomerTier().name());
            response.put("displayName", currentUser.getCustomerTier().getDisplayName());
            response.put("description", currentUser.getCustomerTier().getDescription());
            response.put("features", features.getAvailableFeatures());
            response.put("restrictions", features.getRestrictions());
            response.put("hasLiveTracking", features.hasLiveTracking());
            response.put("hasAnalytics", features.hasAnalytics());
            response.put("hasApiAccess", features.hasApiAccess());
            response.put("maxPackagesPerMonth", features.getMaxPackagesPerMonth());
            response.put("discountPercentage", features.getDiscountPercentage());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get tier information: " + e.getMessage()
            ));
        }
    }

    /**
     * Check if a specific feature is available for current user
     */
    @GetMapping("/feature/{featureName}")
    public ResponseEntity<Map<String, Object>> checkFeatureAvailability(@PathVariable String featureName) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            boolean isAvailable = customerTierService.isFeatureAvailable(currentUser, featureName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("feature", featureName);
            response.put("available", isAvailable);
            response.put("tier", currentUser.getCustomerTier().name());
            
            if (!isAvailable) {
                response.put("message", "This feature requires a higher tier");
                response.put("currentTier", currentUser.getCustomerTier().getDisplayName());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to check feature availability: " + e.getMessage()
            ));
        }
    }

    /**
     * Get available upgrade options for current user
     */
    @GetMapping("/upgrades")
    public ResponseEntity<Map<String, Object>> getAvailableUpgrades() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            CustomerTier currentTier = currentUser.getCustomerTier();
            Map<String, Object> response = new HashMap<>();
            response.put("currentTier", currentTier.name());
            response.put("currentDisplayName", currentTier.getDisplayName());
            
            // Define upgrade options based on current tier
            Map<String, Object> upgradeOptions = new HashMap<>();
            
            switch (currentTier) {
                case INDIVIDUAL:
                    upgradeOptions.put("BUSINESS", Map.of(
                        "displayName", "Business",
                        "description", "Enhanced features for small businesses",
                        "price", "R299/month",
                        "features", new String[]{"Bulk shipping", "Priority support", "Basic analytics"}
                    ));
                    upgradeOptions.put("ENTERPRISE", Map.of(
                        "displayName", "Enterprise",
                        "description", "Full features for large corporations",
                        "price", "R999/month",
                        "features", new String[]{"Live tracking", "Advanced analytics", "API access"}
                    ));
                    break;
                case BUSINESS:
                    upgradeOptions.put("ENTERPRISE", Map.of(
                        "displayName", "Enterprise",
                        "description", "Full features for large corporations",
                        "price", "R999/month",
                        "features", new String[]{"Live tracking", "Advanced analytics", "API access"}
                    ));
                    break;
                case ENTERPRISE:
                    upgradeOptions.put("PREMIUM", Map.of(
                        "displayName", "Premium",
                        "description", "VIP service with priority support",
                        "price", "R1999/month",
                        "features", new String[]{"VIP support", "Same-day delivery", "Custom delivery windows"}
                    ));
                    break;
                case PREMIUM:
                    response.put("message", "You are already at the highest tier");
                    break;
            }
            
            response.put("upgradeOptions", upgradeOptions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get upgrade options: " + e.getMessage()
            ));
        }
    }

    /**
     * Request tier upgrade (admin approval required)
     */
    @PostMapping("/upgrade-request")
    public ResponseEntity<Map<String, Object>> requestTierUpgrade(@RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            String requestedTier = request.get("tier");
            String reason = request.get("reason");
            
            if (requestedTier == null || requestedTier.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tier is required"
                ));
            }

            CustomerTier newTier;
            try {
                newTier = CustomerTier.valueOf(requestedTier.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid tier: " + requestedTier
                ));
            }

            // For now, just return success (in production, this would create a request for admin approval)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upgrade request submitted for admin approval");
            response.put("requestedTier", newTier.getDisplayName());
            response.put("reason", reason);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to submit upgrade request: " + e.getMessage()
            ));
        }
    }
}


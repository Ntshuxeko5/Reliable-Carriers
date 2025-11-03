package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Business API Analytics Controller
 * Provides analytics and usage statistics for business API usage
 */
@RestController
@RequestMapping("/api/business/analytics")
@CrossOrigin(origins = "*")
public class BusinessApiAnalyticsController {
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get API usage statistics
     */
    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getApiUsageStats(
            @RequestParam(required = false) String period, // day, week, month, year
            Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            List<ApiKey> apiKeys = apiKeyRepository.findByUser(businessUser);
            
            // Calculate total requests
            long totalRequests = apiKeys.stream()
                .mapToLong(ApiKey::getRequestsCount)
                .sum();
            
            // Calculate active keys
            long activeKeys = apiKeys.stream()
                .filter(k -> k.getStatus() == ApiKeyStatus.ACTIVE)
                .count();
            
            // Calculate requests per key
            Map<String, Object> requestsPerKey = apiKeys.stream()
                .collect(Collectors.toMap(
                    k -> k.getKeyName() != null ? k.getKeyName() : "Unnamed",
                    ApiKey::getRequestsCount
                ));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalRequests", totalRequests,
                    "activeKeys", activeKeys,
                    "totalKeys", apiKeys.size(),
                    "requestsPerKey", requestsPerKey,
                    "averageRequestsPerKey", apiKeys.isEmpty() ? 0 : totalRequests / apiKeys.size()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get shipping statistics
     */
    @GetMapping("/shipping")
    public ResponseEntity<Map<String, Object>> getShippingStats(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            List<Booking> bookings = bookingRepository.findByCustomerEmailOrderByCreatedAtDesc(businessUser.getEmail());
            
            Map<String, Long> statusCounts = bookings.stream()
                .collect(Collectors.groupingBy(
                    b -> b.getStatus().toString(),
                    Collectors.counting()
                ));
            
            // Calculate total revenue
            double totalRevenue = bookings.stream()
                .filter(b -> b.getTotalAmount() != null)
                .mapToDouble(b -> b.getTotalAmount().doubleValue())
                .sum();
            
            // Monthly breakdown
            Calendar cal = Calendar.getInstance();
            Map<String, Long> monthlyShipments = new LinkedHashMap<>();
            for (int i = 0; i < 12; i++) {
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - i);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                Date monthStart = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date monthEnd = cal.getTime();
                
                long count = bookings.stream()
                    .filter(b -> b.getCreatedAt().after(monthStart) && b.getCreatedAt().before(monthEnd))
                    .count();
                
                monthlyShipments.put(String.format("%d-%02d", 
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1), count);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "totalShipments", bookings.size(),
                    "statusCounts", statusCounts,
                    "totalRevenue", totalRevenue,
                    "monthlyShipments", monthlyShipments,
                    "averageShipmentValue", bookings.isEmpty() ? 0 : totalRevenue / bookings.size()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get comprehensive dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // API Usage Stats
            List<ApiKey> apiKeys = apiKeyRepository.findByUser(businessUser);
            long totalApiRequests = apiKeys.stream().mapToLong(ApiKey::getRequestsCount).sum();
            
            // Shipping Stats
            List<Booking> bookings = bookingRepository.findByCustomerEmailOrderByCreatedAtDesc(businessUser.getEmail());
            long totalShipments = bookings.size();
            long pendingShipments = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.PAYMENT_PENDING)
                .count();
            long inTransitShipments = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.IN_TRANSIT || b.getStatus() == BookingStatus.OUT_FOR_DELIVERY)
                .count();
            long deliveredShipments = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.DELIVERED)
                .count();
            
            double totalRevenue = bookings.stream()
                .filter(b -> b.getTotalAmount() != null)
                .mapToDouble(b -> b.getTotalAmount().doubleValue())
                .sum();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "apiStats", Map.of(
                        "totalKeys", apiKeys.size(),
                        "activeKeys", apiKeys.stream().filter(k -> k.getStatus() == ApiKeyStatus.ACTIVE).count(),
                        "totalRequests", totalApiRequests
                    ),
                    "shippingStats", Map.of(
                        "totalShipments", totalShipments,
                        "pending", pendingShipments,
                        "inTransit", inTransitShipments,
                        "delivered", deliveredShipments,
                        "totalRevenue", totalRevenue
                    ),
                    "businessInfo", Map.of(
                        "businessName", businessUser.getBusinessName(),
                        "verificationStatus", businessUser.getBusinessVerificationStatus(),
                        "creditLimit", businessUser.getCreditLimit(),
                        "currentBalance", businessUser.getCurrentBalance()
                    )
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    private User getAuthenticatedBusinessUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new SecurityException("Analytics are only available for business accounts");
        }
        
        return user;
    }
}






package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.AnalyticsData;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AnalyticsService;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private AuthService authService;

    /**
     * Get comprehensive analytics for current customer
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!analyticsService.hasAnalyticsAccess(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Analytics access not available for your tier",
                    "message", "Upgrade to Business plan or higher for analytics access"
                ));
            }

            AnalyticsData analytics = analyticsService.generateCustomerAnalytics(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analytics", analytics);
            response.put("generatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate analytics: " + e.getMessage()
            ));
        }
    }

    /**
     * Get analytics for specific date range
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getAnalyticsForRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!analyticsService.hasAnalyticsAccess(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Analytics access not available for your tier"
                ));
            }

            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            AnalyticsData analytics = analyticsService.generateCustomerAnalytics(currentUser, start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analytics", analytics);
            response.put("dateRange", Map.of("start", start, "end", end));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate analytics for date range: " + e.getMessage()
            ));
        }
    }

    /**
     * Get delivery performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getDeliveryPerformance() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!analyticsService.hasAnalyticsAccess(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Analytics access not available for your tier"
                ));
            }

            AnalyticsService.DeliveryPerformanceMetrics performance = 
                analyticsService.getDeliveryPerformance(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("performance", performance);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get delivery performance: " + e.getMessage()
            ));
        }
    }

    /**
     * Get cost analysis
     */
    @GetMapping("/costs")
    public ResponseEntity<Map<String, Object>> getCostAnalysis() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!analyticsService.hasAnalyticsAccess(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Analytics access not available for your tier"
                ));
            }

            AnalyticsService.CostAnalysis costAnalysis = analyticsService.getCostAnalysis(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("costAnalysis", costAnalysis);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get cost analysis: " + e.getMessage()
            ));
        }
    }

    /**
     * Get shipping trends
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getShippingTrends() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!analyticsService.hasAnalyticsAccess(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Analytics access not available for your tier"
                ));
            }

            AnalyticsService.ShippingTrends trends = analyticsService.getShippingTrends(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("trends", trends);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get shipping trends: " + e.getMessage()
            ));
        }
    }

    /**
     * Get optimization recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getOptimizationRecommendations() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!analyticsService.hasAnalyticsAccess(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Analytics access not available for your tier"
                ));
            }

            List<String> recommendations = analyticsService.getOptimizationRecommendations(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get recommendations: " + e.getMessage()
            ));
        }
    }

    /**
     * Download analytics report
     */
    @GetMapping("/report")
    public ResponseEntity<byte[]> downloadAnalyticsReport(@RequestParam(defaultValue = "pdf") String format) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (!analyticsService.hasAnalyticsAccess(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            byte[] reportData = analyticsService.generateAnalyticsReport(currentUser, format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "analytics-report." + format);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(reportData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check analytics access
     */
    @GetMapping("/access")
    public ResponseEntity<Map<String, Object>> checkAnalyticsAccess() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            boolean hasAccess = analyticsService.hasAnalyticsAccess(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasAccess", hasAccess);
            response.put("tier", currentUser.getCustomerTier().name());
            response.put("message", hasAccess ? 
                "Analytics access available" : 
                "Upgrade to Business plan or higher for analytics access");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to check analytics access: " + e.getMessage()
            ));
        }
    }
}
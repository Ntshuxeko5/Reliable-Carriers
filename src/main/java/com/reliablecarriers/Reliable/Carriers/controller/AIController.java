package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.service.AIService;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private AuthService authService;

    /**
     * Get AI recommendations for current user
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getAIRecommendations() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            List<AIService.AIRecommendation> recommendations = aiService.generateRecommendations(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendations", recommendations);
            response.put("count", recommendations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get AI recommendations: " + e.getMessage()
            ));
        }
    }

    /**
     * Predict delivery time for a shipment
     */
    @PostMapping("/predict-delivery")
    public ResponseEntity<Map<String, Object>> predictDeliveryTime(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            // Create shipment object from request
            Shipment shipment = new Shipment();
            shipment.setServiceType(ServiceType.valueOf(((String) request.get("serviceType")).toUpperCase()));
            shipment.setPickupCity((String) request.get("pickupCity"));
            shipment.setDeliveryCity((String) request.get("deliveryCity"));
            shipment.setWeight(Double.valueOf(request.get("weight").toString()));
            shipment.setCreatedAt(new Date());
            
            AIService.DeliveryPrediction prediction = aiService.predictDeliveryTime(shipment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prediction", prediction);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to predict delivery time: " + e.getMessage()
            ));
        }
    }

    /**
     * Get optimal service recommendation
     */
    @PostMapping("/suggest-service")
    public ResponseEntity<Map<String, Object>> suggestOptimalService(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            // Create shipment object from request
            Shipment shipment = new Shipment();
            shipment.setPickupCity((String) request.get("pickupCity"));
            shipment.setDeliveryCity((String) request.get("deliveryCity"));
            shipment.setWeight(Double.valueOf(request.get("weight").toString()));
            shipment.setServiceType(ServiceType.valueOf(((String) request.get("currentService")).toUpperCase()));
            
            AIService.ServiceRecommendation recommendation = aiService.suggestOptimalService(shipment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendation", recommendation);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to suggest optimal service: " + e.getMessage()
            ));
        }
    }

    /**
     * Predict demand for a route
     */
    @GetMapping("/predict-demand")
    public ResponseEntity<Map<String, Object>> predictDemand(
            @RequestParam String fromCity,
            @RequestParam String toCity,
            @RequestParam(required = false) String date) {
        
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            LocalDateTime targetDate = date != null ? 
                LocalDateTime.parse(date) : 
                LocalDateTime.now().plusDays(1);
            
            AIService.DemandPrediction prediction = aiService.predictDemand(fromCity, toCity, targetDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prediction", prediction);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to predict demand: " + e.getMessage()
            ));
        }
    }

    /**
     * Get personalized offers
     */
    @GetMapping("/offers")
    public ResponseEntity<Map<String, Object>> getPersonalizedOffers() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            List<AIService.PersonalizedOffer> offers = aiService.generatePersonalizedOffers(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("offers", offers);
            response.put("count", offers.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get personalized offers: " + e.getMessage()
            ));
        }
    }

    /**
     * Analyze shipping patterns
     */
    @GetMapping("/patterns")
    public ResponseEntity<Map<String, Object>> analyzeShippingPatterns() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            AIService.ShippingPatternAnalysis analysis = aiService.analyzeShippingPatterns(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analysis", analysis);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to analyze shipping patterns: " + e.getMessage()
            ));
        }
    }

    /**
     * Optimize delivery route
     */
    @PostMapping("/optimize-route")
    public ResponseEntity<Map<String, Object>> optimizeDeliveryRoute(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            // This would typically fetch shipments from the request
            // For now, we'll create a simplified version
            List<Shipment> shipments = List.of(); // Empty list for demonstration
            
            AIService.RouteOptimization optimization = aiService.optimizeDeliveryRoute(shipments);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("optimization", optimization);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to optimize delivery route: " + e.getMessage()
            ));
        }
    }

    /**
     * Predict customer satisfaction
     */
    @PostMapping("/predict-satisfaction")
    public ResponseEntity<Map<String, Object>> predictCustomerSatisfaction(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            // Create shipment object from request
            Shipment shipment = new Shipment();
            shipment.setServiceType(ServiceType.valueOf(((String) request.get("serviceType")).toUpperCase()));
            shipment.setPickupCity((String) request.get("pickupCity"));
            shipment.setDeliveryCity((String) request.get("deliveryCity"));
            shipment.setWeight(Double.valueOf(request.get("weight").toString()));
            
            AIService.SatisfactionPrediction prediction = aiService.predictCustomerSatisfaction(shipment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prediction", prediction);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to predict customer satisfaction: " + e.getMessage()
            ));
        }
    }

    /**
     * Get cost optimization suggestions
     */
    @GetMapping("/cost-optimizations")
    public ResponseEntity<Map<String, Object>> getCostOptimizations() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            List<AIService.CostOptimization> optimizations = aiService.generateCostOptimizations(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("optimizations", optimizations);
            response.put("count", optimizations.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get cost optimizations: " + e.getMessage()
            ));
        }
    }

    /**
     * Predict package delays
     */
    @PostMapping("/predict-delays")
    public ResponseEntity<Map<String, Object>> predictPackageDelays(@RequestBody Map<String, Object> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            // Create shipment object from request
            Shipment shipment = new Shipment();
            shipment.setServiceType(ServiceType.valueOf(((String) request.get("serviceType")).toUpperCase()));
            shipment.setPickupCity((String) request.get("pickupCity"));
            shipment.setDeliveryCity((String) request.get("deliveryCity"));
            shipment.setWeight(Double.valueOf(request.get("weight").toString()));
            shipment.setCreatedAt(new Date());
            
            AIService.DelayPrediction prediction = aiService.predictPackageDelays(shipment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("prediction", prediction);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to predict package delays: " + e.getMessage()
            ));
        }
    }

    /**
     * Get AI insights summary
     */
    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getAIInsights() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            // Get all AI insights
            List<AIService.AIRecommendation> recommendations = aiService.generateRecommendations(currentUser);
            List<AIService.PersonalizedOffer> offers = aiService.generatePersonalizedOffers(currentUser);
            AIService.ShippingPatternAnalysis patterns = aiService.analyzeShippingPatterns(currentUser);
            List<AIService.CostOptimization> optimizations = aiService.generateCostOptimizations(currentUser);
            
            Map<String, Object> insights = new HashMap<>();
            insights.put("recommendations", recommendations);
            insights.put("offers", offers);
            insights.put("patterns", patterns);
            insights.put("optimizations", optimizations);
            insights.put("generatedAt", LocalDateTime.now());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("insights", insights);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get AI insights: " + e.getMessage()
            ));
        }
    }
}


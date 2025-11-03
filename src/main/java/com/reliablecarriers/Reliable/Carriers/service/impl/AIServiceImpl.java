package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private ShipmentRepository shipmentRepository;
    

    @Override
    public List<AIRecommendation> generateRecommendations(User user) {
        List<AIRecommendation> recommendations = new ArrayList<>();
        
        // Get user's shipping history
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(user.getEmail());
        
        // Generate recommendations based on patterns
        if (shipments.size() > 10) {
            AIRecommendation bulkRecommendation = new AIRecommendation();
            bulkRecommendation.setType("COST_OPTIMIZATION");
            bulkRecommendation.setTitle("Bulk Shipping Discount");
            bulkRecommendation.setDescription("You ship frequently - consider bulk shipping for 15-20% savings");
            bulkRecommendation.setAction("Upgrade to Business plan for bulk discounts");
            bulkRecommendation.setConfidence(0.85);
            bulkRecommendation.setPotentialSavings(calculatePotentialSavings(shipments));
            recommendations.add(bulkRecommendation);
        }
        
        // Service optimization recommendation
        String mostUsedService = getMostUsedService(shipments);
        if (mostUsedService != null && !mostUsedService.equals("STANDARD")) {
            AIRecommendation serviceRecommendation = new AIRecommendation();
            serviceRecommendation.setType("SERVICE_OPTIMIZATION");
            serviceRecommendation.setTitle("Service Optimization");
            serviceRecommendation.setDescription("Consider using Standard delivery for non-urgent packages");
            serviceRecommendation.setAction("Switch to Standard delivery for 30% cost savings");
            serviceRecommendation.setConfidence(0.75);
            serviceRecommendation.setPotentialSavings(150.0);
            recommendations.add(serviceRecommendation);
        }
        
        // Route optimization
        if (hasFrequentRoute(shipments)) {
            AIRecommendation routeRecommendation = new AIRecommendation();
            routeRecommendation.setType("ROUTE_OPTIMIZATION");
            routeRecommendation.setTitle("Route Optimization");
            routeRecommendation.setDescription("You frequently ship to the same destinations");
            routeRecommendation.setAction("Consider scheduled pickups for better planning");
            routeRecommendation.setConfidence(0.80);
            routeRecommendation.setPotentialSavings(200.0);
            recommendations.add(routeRecommendation);
        }
        
        // Tier-specific recommendations
        if (user.getCustomerTier() == CustomerTier.INDIVIDUAL && shipments.size() > 5) {
            AIRecommendation tierRecommendation = new AIRecommendation();
            tierRecommendation.setType("TIER_UPGRADE");
            tierRecommendation.setTitle("Upgrade to Business Plan");
            tierRecommendation.setDescription("You qualify for business discounts and features");
            tierRecommendation.setAction("Upgrade to Business plan for better rates");
            tierRecommendation.setConfidence(0.90);
            tierRecommendation.setPotentialSavings(300.0);
            recommendations.add(tierRecommendation);
        }
        
        return recommendations;
    }

    @Override
    public DeliveryPrediction predictDeliveryTime(Shipment shipment) {
        DeliveryPrediction prediction = new DeliveryPrediction();
        
        // Simplified prediction logic
        String serviceType = shipment.getServiceType().toString();
        LocalDateTime baseTime = LocalDateTime.now().plusDays(1);
        
        switch (serviceType.toUpperCase()) {
            case "EXPRESS":
                prediction.setPredictedDeliveryTime(baseTime.plusHours(24));
                prediction.setConfidence(0.90);
                break;
            case "STANDARD":
                prediction.setPredictedDeliveryTime(baseTime.plusDays(2));
                prediction.setConfidence(0.85);
                break;
            case "ECONOMY":
                prediction.setPredictedDeliveryTime(baseTime.plusDays(3));
                prediction.setConfidence(0.80);
                break;
            default:
                prediction.setPredictedDeliveryTime(baseTime.plusDays(2));
                prediction.setConfidence(0.75);
        }
        
        // Add risk factors
        List<String> riskFactors = new ArrayList<>();
        if (isWeekend(shipment.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())) {
            riskFactors.add("Weekend shipping may cause delays");
        }
        if (isHolidayPeriod(shipment.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())) {
            riskFactors.add("Holiday period may affect delivery times");
        }
        if (isLongDistance(shipment)) {
            riskFactors.add("Long distance delivery may take longer");
        }
        
        prediction.setRiskFactors(riskFactors);
        prediction.setFactors("Based on service type, distance, and historical data");
        
        return prediction;
    }

    @Override
    public ServiceRecommendation suggestOptimalService(Shipment shipment) {
        ServiceRecommendation recommendation = new ServiceRecommendation();
        
        // Analyze shipment characteristics
        double weight = shipment.getWeight();
        boolean isUrgent = isUrgentShipment(shipment);
        
        if (isUrgent || weight < 1.0) {
            recommendation.setRecommendedService("EXPRESS");
            recommendation.setReason("Urgent delivery or lightweight package");
            recommendation.setCostSavings(0.0);
            recommendation.setTimeSavings(24.0);
            recommendation.setConfidence(0.90);
        } else if (isLongDistance(shipment)) {
            recommendation.setRecommendedService("STANDARD");
            recommendation.setReason("Long distance - standard service is most cost-effective");
            recommendation.setCostSavings(50.0);
            recommendation.setTimeSavings(0.0);
            recommendation.setConfidence(0.85);
        } else {
            recommendation.setRecommendedService("ECONOMY");
            recommendation.setReason("Short distance - economy service offers best value");
            recommendation.setCostSavings(100.0);
            recommendation.setTimeSavings(-24.0);
            recommendation.setConfidence(0.80);
        }
        
        return recommendation;
    }

    @Override
    public DemandPrediction predictDemand(String fromCity, String toCity, LocalDateTime date) {
        DemandPrediction prediction = new DemandPrediction();
        
        // Simplified demand prediction
        int baseDemand = 50;
        
        // Adjust based on day of week
        if (date.getDayOfWeek().getValue() <= 5) { // Weekday
            baseDemand += 20;
        }
        
        // Adjust based on route popularity
        if (isPopularRoute(fromCity, toCity)) {
            baseDemand += 30;
        }
        
        prediction.setPredictedDemand(baseDemand);
        prediction.setConfidence(0.75);
        prediction.setPeakTime("10:00 AM - 2:00 PM");
        
        List<String> recommendations = new ArrayList<>();
        if (baseDemand > 80) {
            recommendations.add("High demand expected - consider booking in advance");
        }
        if (isWeekend(date)) {
            recommendations.add("Weekend demand is typically lower");
        }
        
        prediction.setRecommendations(recommendations);
        
        return prediction;
    }

    @Override
    public List<PersonalizedOffer> generatePersonalizedOffers(User user) {
        List<PersonalizedOffer> offers = new ArrayList<>();
        
        // Get user's shipping patterns
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(user.getEmail());
        
        // Generate offers based on user behavior
        if (shipments.size() > 5) {
            PersonalizedOffer bulkOffer = new PersonalizedOffer();
            bulkOffer.setOfferType("BULK_DISCOUNT");
            bulkOffer.setTitle("Bulk Shipping Discount");
            bulkOffer.setDescription("Get 15% off your next 5 shipments");
            bulkOffer.setDiscount(15.0);
            bulkOffer.setExpiryDate(LocalDateTime.now().plusDays(30));
            bulkOffer.setConditions("Minimum 5 shipments, valid for 30 days");
            offers.add(bulkOffer);
        }
        
        if (user.getCustomerTier() == CustomerTier.INDIVIDUAL) {
            PersonalizedOffer upgradeOffer = new PersonalizedOffer();
            upgradeOffer.setOfferType("TIER_UPGRADE");
            upgradeOffer.setTitle("Business Plan Trial");
            upgradeOffer.setDescription("Try Business plan for 30 days with 20% discount");
            upgradeOffer.setDiscount(20.0);
            upgradeOffer.setExpiryDate(LocalDateTime.now().plusDays(14));
            upgradeOffer.setConditions("First-time Business plan users only");
            offers.add(upgradeOffer);
        }
        
        // Route-specific offers
        String frequentRoute = getFrequentRoute(shipments);
        if (frequentRoute != null) {
            PersonalizedOffer routeOffer = new PersonalizedOffer();
            routeOffer.setOfferType("ROUTE_DISCOUNT");
            routeOffer.setTitle("Frequent Route Discount");
            routeOffer.setDescription("Get 10% off your next shipment to " + frequentRoute);
            routeOffer.setDiscount(10.0);
            routeOffer.setExpiryDate(LocalDateTime.now().plusDays(7));
            routeOffer.setConditions("Valid for " + frequentRoute + " route only");
            offers.add(routeOffer);
        }
        
        return offers;
    }

    @Override
    public ShippingPatternAnalysis analyzeShippingPatterns(User user) {
        ShippingPatternAnalysis analysis = new ShippingPatternAnalysis();
        
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(user.getEmail());
        
        if (shipments.isEmpty()) {
            return analysis;
        }
        
        // Analyze peak shipping day
        Map<String, Long> dayCounts = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().getDayOfWeek().name(),
                Collectors.counting()
            ));
        
        String peakDay = dayCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Monday");
        analysis.setPeakShippingDay(peakDay);
        
        // Analyze peak shipping time
        analysis.setPeakShippingTime("10:00 AM - 2:00 PM");
        
        // Analyze most used service
        String mostUsedService = getMostUsedService(shipments);
        analysis.setMostUsedService(mostUsedService);
        
        // Analyze preferred route
        String preferredRoute = getFrequentRoute(shipments);
        analysis.setPreferredRoute(preferredRoute);
        
        // Calculate average cost
        double avgCost = shipments.stream()
            .mapToDouble(s -> s.getShippingCost().doubleValue())
            .average()
            .orElse(0.0);
        analysis.setAverageCost(avgCost);
        
        // Generate insights
        List<String> insights = new ArrayList<>();
        insights.add("You typically ship on " + peakDay + "s");
        insights.add("Your average shipping cost is R" + String.format("%.2f", avgCost));
        if (mostUsedService != null) {
            insights.add("You prefer " + mostUsedService + " delivery service");
        }
        if (preferredRoute != null) {
            insights.add("You frequently ship to " + preferredRoute);
        }
        
        analysis.setInsights(insights);
        
        return analysis;
    }

    @Override
    public RouteOptimization optimizeDeliveryRoute(List<Shipment> shipments) {
        RouteOptimization optimization = new RouteOptimization();
        
        // Simplified route optimization
        List<String> optimizedRoute = shipments.stream()
            .map(s -> s.getPickupCity() + " -> " + s.getDeliveryCity())
            .distinct()
            .collect(Collectors.toList());
        
        optimization.setOptimizedRoute(optimizedRoute);
        optimization.setTotalDistance(calculateTotalDistance(shipments));
        optimization.setEstimatedTime(calculateEstimatedTime(shipments));
        optimization.setFuelSavings(calculateFuelSavings(shipments));
        
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Group nearby deliveries together");
        recommendations.add("Use GPS optimization for fuel efficiency");
        recommendations.add("Consider time windows for customer availability");
        
        optimization.setRecommendations(recommendations);
        
        return optimization;
    }

    @Override
    public SatisfactionPrediction predictCustomerSatisfaction(Shipment shipment) {
        SatisfactionPrediction prediction = new SatisfactionPrediction();
        
        // Simplified satisfaction prediction
        double baseSatisfaction = 4.0;
        
        // Adjust based on service type
        String serviceType = shipment.getServiceType().toString();
        switch (serviceType.toUpperCase()) {
            case "EXPRESS":
                baseSatisfaction += 0.5;
                break;
            case "STANDARD":
                baseSatisfaction += 0.2;
                break;
            case "ECONOMY":
                baseSatisfaction -= 0.1;
                break;
        }
        
        // Adjust based on distance
        if (isLongDistance(shipment)) {
            baseSatisfaction -= 0.2;
        }
        
        prediction.setPredictedSatisfaction(Math.max(1.0, Math.min(5.0, baseSatisfaction)));
        prediction.setConfidence(0.75);
        
        List<String> factors = new ArrayList<>();
        factors.add("Service type: " + serviceType);
        factors.add("Distance: " + (isLongDistance(shipment) ? "Long" : "Short"));
        factors.add("Package weight: " + shipment.getWeight() + " kg");
        
        prediction.setFactors(factors);
        
        List<String> recommendations = new ArrayList<>();
        if (baseSatisfaction < 4.0) {
            recommendations.add("Consider upgrading to express delivery");
            recommendations.add("Provide real-time tracking updates");
        }
        
        prediction.setRecommendations(recommendations);
        
        return prediction;
    }

    @Override
    public List<CostOptimization> generateCostOptimizations(User user) {
        List<CostOptimization> optimizations = new ArrayList<>();
        
        List<Shipment> shipments = shipmentRepository.findBySenderEmailOrRecipientEmail(user.getEmail());
        
        // Bulk shipping optimization
        if (shipments.size() > 10) {
            CostOptimization bulkOptimization = new CostOptimization();
            bulkOptimization.setOptimizationType("BULK_SHIPPING");
            bulkOptimization.setDescription("Consolidate shipments for bulk discounts");
            bulkOptimization.setPotentialSavings(calculateBulkSavings(shipments));
            bulkOptimization.setAction("Use bulk shipping for 15-20% savings");
            bulkOptimization.setConfidence(0.85);
            optimizations.add(bulkOptimization);
        }
        
        // Service optimization
        String mostUsedService = getMostUsedService(shipments);
        if (mostUsedService != null && !mostUsedService.equals("ECONOMY")) {
            CostOptimization serviceOptimization = new CostOptimization();
            serviceOptimization.setOptimizationType("SERVICE_OPTIMIZATION");
            serviceOptimization.setDescription("Use economy service for non-urgent packages");
            serviceOptimization.setPotentialSavings(calculateServiceSavings(shipments));
            serviceOptimization.setAction("Switch to economy service when possible");
            serviceOptimization.setConfidence(0.80);
            optimizations.add(serviceOptimization);
        }
        
        // Route optimization
        if (hasFrequentRoute(shipments)) {
            CostOptimization routeOptimization = new CostOptimization();
            routeOptimization.setOptimizationType("ROUTE_OPTIMIZATION");
            routeOptimization.setDescription("Optimize delivery routes for fuel savings");
            routeOptimization.setPotentialSavings(calculateRouteSavings(shipments));
            routeOptimization.setAction("Use route optimization for fuel efficiency");
            routeOptimization.setConfidence(0.75);
            optimizations.add(routeOptimization);
        }
        
        return optimizations;
    }

    @Override
    public DelayPrediction predictPackageDelays(Shipment shipment) {
        DelayPrediction prediction = new DelayPrediction();
        
        // Simplified delay prediction
        double delayProbability = 0.1; // Base 10% chance
        
        // Adjust based on factors
        if (isWeekend(shipment.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())) {
            delayProbability += 0.1;
        }
        if (isHolidayPeriod(shipment.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())) {
            delayProbability += 0.2;
        }
        if (isLongDistance(shipment)) {
            delayProbability += 0.15;
        }
        if (shipment.getWeight() > 10.0) {
            delayProbability += 0.05;
        }
        
        prediction.setDelayPredicted(delayProbability > 0.3);
        prediction.setDelayProbability(delayProbability);
        
        if (prediction.isDelayPredicted()) {
            prediction.setPredictedDelayTime(LocalDateTime.now().plusDays(1));
        }
        
        List<String> delayFactors = new ArrayList<>();
        if (isWeekend(shipment.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())) {
            delayFactors.add("Weekend shipping");
        }
        if (isHolidayPeriod(shipment.getCreatedAt().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())) {
            delayFactors.add("Holiday period");
        }
        if (isLongDistance(shipment)) {
            delayFactors.add("Long distance delivery");
        }
        
        prediction.setDelayFactors(delayFactors);
        
        List<String> mitigationStrategies = new ArrayList<>();
        mitigationStrategies.add("Use express delivery for urgent packages");
        mitigationStrategies.add("Provide accurate delivery addresses");
        mitigationStrategies.add("Ensure recipient is available for delivery");
        
        prediction.setMitigationStrategies(mitigationStrategies);
        
        return prediction;
    }

    // Helper methods
    private double calculatePotentialSavings(List<Shipment> shipments) {
        return shipments.size() * 50.0; // R50 per shipment potential savings
    }
    
    private String getMostUsedService(List<Shipment> shipments) {
        if (shipments.isEmpty()) return null;
        
        Map<String, Long> serviceCounts = shipments.stream()
            .collect(Collectors.groupingBy(
                s -> s.getServiceType().toString(),
                Collectors.counting()
            ));
        
        return serviceCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    private boolean hasFrequentRoute(List<Shipment> shipments) {
        if (shipments.size() < 3) return false;
        
        Map<String, Long> routeCounts = shipments.stream()
            .map(s -> s.getPickupCity() + " -> " + s.getDeliveryCity())
            .collect(Collectors.groupingBy(
                route -> route,
                Collectors.counting()
            ));
        
        return routeCounts.values().stream()
            .anyMatch(count -> count >= 3);
    }
    
    private String getFrequentRoute(List<Shipment> shipments) {
        if (shipments.isEmpty()) return null;
        
        Map<String, Long> routeCounts = shipments.stream()
            .map(s -> s.getPickupCity() + " -> " + s.getDeliveryCity())
            .collect(Collectors.groupingBy(
                route -> route,
                Collectors.counting()
            ));
        
        return routeCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    private boolean isUrgentShipment(Shipment shipment) {
        // Simplified logic - in reality, this would check shipment metadata
        return shipment.getServiceType().equals("EXPRESS");
    }
    
    private boolean isLongDistance(Shipment shipment) {
        // Simplified logic - in reality, this would calculate actual distance
        return !shipment.getPickupCity().equals(shipment.getDeliveryCity());
    }
    
    private boolean isWeekend(LocalDateTime date) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // Saturday or Sunday
    }
    
    private boolean isHolidayPeriod(LocalDateTime date) {
        // Simplified logic - in reality, this would check against holiday calendar
        return false;
    }
    
    private boolean isPopularRoute(String fromCity, String toCity) {
        // Simplified logic - in reality, this would check against historical data
        return true;
    }
    
    private double calculateTotalDistance(List<Shipment> shipments) {
        // Simplified calculation
        return shipments.size() * 50.0; // 50km per shipment
    }
    
    private double calculateEstimatedTime(List<Shipment> shipments) {
        // Simplified calculation
        return shipments.size() * 2.0; // 2 hours per shipment
    }
    
    private double calculateFuelSavings(List<Shipment> shipments) {
        // Simplified calculation
        return shipments.size() * 25.0; // R25 savings per shipment
    }
    
    private double calculateBulkSavings(List<Shipment> shipments) {
        return shipments.size() * 30.0; // R30 per shipment bulk savings
    }
    
    private double calculateServiceSavings(List<Shipment> shipments) {
        return shipments.size() * 20.0; // R20 per shipment service savings
    }
    
    private double calculateRouteSavings(List<Shipment> shipments) {
        return shipments.size() * 15.0; // R15 per shipment route savings
    }
}


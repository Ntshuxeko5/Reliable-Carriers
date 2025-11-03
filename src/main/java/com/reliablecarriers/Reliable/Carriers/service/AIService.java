package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;

import java.time.LocalDateTime;
import java.util.List;

public interface AIService {
    
    /**
     * Generate smart recommendations for user
     */
    List<AIRecommendation> generateRecommendations(User user);
    
    /**
     * Predict delivery time for shipment
     */
    DeliveryPrediction predictDeliveryTime(Shipment shipment);
    
    /**
     * Suggest optimal shipping service
     */
    ServiceRecommendation suggestOptimalService(Shipment shipment);
    
    /**
     * Predict package demand for route
     */
    DemandPrediction predictDemand(String fromCity, String toCity, LocalDateTime date);
    
    /**
     * Generate personalized offers
     */
    List<PersonalizedOffer> generatePersonalizedOffers(User user);
    
    /**
     * Analyze shipping patterns
     */
    ShippingPatternAnalysis analyzeShippingPatterns(User user);
    
    /**
     * Optimize delivery routes
     */
    RouteOptimization optimizeDeliveryRoute(List<Shipment> shipments);
    
    /**
     * Predict customer satisfaction
     */
    SatisfactionPrediction predictCustomerSatisfaction(Shipment shipment);
    
    /**
     * Generate cost optimization suggestions
     */
    List<CostOptimization> generateCostOptimizations(User user);
    
    /**
     * Predict package delays
     */
    DelayPrediction predictPackageDelays(Shipment shipment);
    
    // Data classes for AI responses
    class AIRecommendation {
        private String type;
        private String title;
        private String description;
        private String action;
        private double confidence;
        private double potentialSavings;
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public double getPotentialSavings() { return potentialSavings; }
        public void setPotentialSavings(double potentialSavings) { this.potentialSavings = potentialSavings; }
    }
    
    class DeliveryPrediction {
        private LocalDateTime predictedDeliveryTime;
        private double confidence;
        private String factors;
        private List<String> riskFactors;
        
        // Getters and setters
        public LocalDateTime getPredictedDeliveryTime() { return predictedDeliveryTime; }
        public void setPredictedDeliveryTime(LocalDateTime predictedDeliveryTime) { this.predictedDeliveryTime = predictedDeliveryTime; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getFactors() { return factors; }
        public void setFactors(String factors) { this.factors = factors; }
        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    }
    
    class ServiceRecommendation {
        private String recommendedService;
        private String reason;
        private double costSavings;
        private double timeSavings;
        private double confidence;
        
        // Getters and setters
        public String getRecommendedService() { return recommendedService; }
        public void setRecommendedService(String recommendedService) { this.recommendedService = recommendedService; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public double getCostSavings() { return costSavings; }
        public void setCostSavings(double costSavings) { this.costSavings = costSavings; }
        public double getTimeSavings() { return timeSavings; }
        public void setTimeSavings(double timeSavings) { this.timeSavings = timeSavings; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
    
    class DemandPrediction {
        private int predictedDemand;
        private double confidence;
        private String peakTime;
        private List<String> recommendations;
        
        // Getters and setters
        public int getPredictedDemand() { return predictedDemand; }
        public void setPredictedDemand(int predictedDemand) { this.predictedDemand = predictedDemand; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getPeakTime() { return peakTime; }
        public void setPeakTime(String peakTime) { this.peakTime = peakTime; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
    
    class PersonalizedOffer {
        private String offerType;
        private String title;
        private String description;
        private double discount;
        private LocalDateTime expiryDate;
        private String conditions;
        
        // Getters and setters
        public String getOfferType() { return offerType; }
        public void setOfferType(String offerType) { this.offerType = offerType; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getDiscount() { return discount; }
        public void setDiscount(double discount) { this.discount = discount; }
        public LocalDateTime getExpiryDate() { return expiryDate; }
        public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
        public String getConditions() { return conditions; }
        public void setConditions(String conditions) { this.conditions = conditions; }
    }
    
    class ShippingPatternAnalysis {
        private String peakShippingDay;
        private String peakShippingTime;
        private String mostUsedService;
        private String preferredRoute;
        private double averageCost;
        private List<String> insights;
        
        // Getters and setters
        public String getPeakShippingDay() { return peakShippingDay; }
        public void setPeakShippingDay(String peakShippingDay) { this.peakShippingDay = peakShippingDay; }
        public String getPeakShippingTime() { return peakShippingTime; }
        public void setPeakShippingTime(String peakShippingTime) { this.peakShippingTime = peakShippingTime; }
        public String getMostUsedService() { return mostUsedService; }
        public void setMostUsedService(String mostUsedService) { this.mostUsedService = mostUsedService; }
        public String getPreferredRoute() { return preferredRoute; }
        public void setPreferredRoute(String preferredRoute) { this.preferredRoute = preferredRoute; }
        public double getAverageCost() { return averageCost; }
        public void setAverageCost(double averageCost) { this.averageCost = averageCost; }
        public List<String> getInsights() { return insights; }
        public void setInsights(List<String> insights) { this.insights = insights; }
    }
    
    class RouteOptimization {
        private List<String> optimizedRoute;
        private double totalDistance;
        private double estimatedTime;
        private double fuelSavings;
        private List<String> recommendations;
        
        // Getters and setters
        public List<String> getOptimizedRoute() { return optimizedRoute; }
        public void setOptimizedRoute(List<String> optimizedRoute) { this.optimizedRoute = optimizedRoute; }
        public double getTotalDistance() { return totalDistance; }
        public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }
        public double getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(double estimatedTime) { this.estimatedTime = estimatedTime; }
        public double getFuelSavings() { return fuelSavings; }
        public void setFuelSavings(double fuelSavings) { this.fuelSavings = fuelSavings; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
    
    class SatisfactionPrediction {
        private double predictedSatisfaction;
        private double confidence;
        private List<String> factors;
        private List<String> recommendations;
        
        // Getters and setters
        public double getPredictedSatisfaction() { return predictedSatisfaction; }
        public void setPredictedSatisfaction(double predictedSatisfaction) { this.predictedSatisfaction = predictedSatisfaction; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public List<String> getFactors() { return factors; }
        public void setFactors(List<String> factors) { this.factors = factors; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
    
    class CostOptimization {
        private String optimizationType;
        private String description;
        private double potentialSavings;
        private String action;
        private double confidence;
        
        // Getters and setters
        public String getOptimizationType() { return optimizationType; }
        public void setOptimizationType(String optimizationType) { this.optimizationType = optimizationType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getPotentialSavings() { return potentialSavings; }
        public void setPotentialSavings(double potentialSavings) { this.potentialSavings = potentialSavings; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
    }
    
    class DelayPrediction {
        private boolean isDelayPredicted;
        private double delayProbability;
        private LocalDateTime predictedDelayTime;
        private List<String> delayFactors;
        private List<String> mitigationStrategies;
        
        // Getters and setters
        public boolean isDelayPredicted() { return isDelayPredicted; }
        public void setDelayPredicted(boolean delayPredicted) { isDelayPredicted = delayPredicted; }
        public double getDelayProbability() { return delayProbability; }
        public void setDelayProbability(double delayProbability) { this.delayProbability = delayProbability; }
        public LocalDateTime getPredictedDelayTime() { return predictedDelayTime; }
        public void setPredictedDelayTime(LocalDateTime predictedDelayTime) { this.predictedDelayTime = predictedDelayTime; }
        public List<String> getDelayFactors() { return delayFactors; }
        public void setDelayFactors(List<String> delayFactors) { this.delayFactors = delayFactors; }
        public List<String> getMitigationStrategies() { return mitigationStrategies; }
        public void setMitigationStrategies(List<String> mitigationStrategies) { this.mitigationStrategies = mitigationStrategies; }
    }
}


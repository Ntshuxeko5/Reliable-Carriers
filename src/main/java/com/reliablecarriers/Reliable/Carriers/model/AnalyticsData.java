package com.reliablecarriers.Reliable.Carriers.model;

import java.time.LocalDateTime;
import java.util.List;

public class AnalyticsData {
    private String customerId;
    private String customerName;
    private CustomerTier customerTier;
    private LocalDateTime generatedAt;
    
    // Delivery Statistics
    private int totalShipments;
    private int deliveredShipments;
    private int pendingShipments;
    private int inTransitShipments;
    private double onTimeDeliveryRate;
    private double averageDeliveryTime; // in hours
    private double totalShippingCost;
    private double averageShippingCost;
    
    // Monthly Trends
    private List<MonthlyData> monthlyTrends;
    private List<ServiceTypeData> serviceTypeBreakdown;
    private List<RouteData> popularRoutes;
    
    // Performance Metrics
    private double customerSatisfactionScore;
    private int totalDelays;
    private double delayRate;
    private String mostUsedService;
    private String peakDeliveryDay;
    private String peakDeliveryTime;
    
    // Cost Analysis
    private double monthlySavings;
    private double potentialSavings;
    private List<CostBreakdown> costBreakdown;
    
    // Recommendations
    private List<String> recommendations;
    private List<String> optimizationSuggestions;
    
    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public CustomerTier getCustomerTier() {
        return customerTier;
    }
    
    public void setCustomerTier(CustomerTier customerTier) {
        this.customerTier = customerTier;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
    
    public int getTotalShipments() {
        return totalShipments;
    }
    
    public void setTotalShipments(int totalShipments) {
        this.totalShipments = totalShipments;
    }
    
    public int getDeliveredShipments() {
        return deliveredShipments;
    }
    
    public void setDeliveredShipments(int deliveredShipments) {
        this.deliveredShipments = deliveredShipments;
    }
    
    public int getPendingShipments() {
        return pendingShipments;
    }
    
    public void setPendingShipments(int pendingShipments) {
        this.pendingShipments = pendingShipments;
    }
    
    public int getInTransitShipments() {
        return inTransitShipments;
    }
    
    public void setInTransitShipments(int inTransitShipments) {
        this.inTransitShipments = inTransitShipments;
    }
    
    public double getOnTimeDeliveryRate() {
        return onTimeDeliveryRate;
    }
    
    public void setOnTimeDeliveryRate(double onTimeDeliveryRate) {
        this.onTimeDeliveryRate = onTimeDeliveryRate;
    }
    
    public double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }
    
    public void setAverageDeliveryTime(double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }
    
    public double getTotalShippingCost() {
        return totalShippingCost;
    }
    
    public void setTotalShippingCost(double totalShippingCost) {
        this.totalShippingCost = totalShippingCost;
    }
    
    public double getAverageShippingCost() {
        return averageShippingCost;
    }
    
    public void setAverageShippingCost(double averageShippingCost) {
        this.averageShippingCost = averageShippingCost;
    }
    
    public List<MonthlyData> getMonthlyTrends() {
        return monthlyTrends;
    }
    
    public void setMonthlyTrends(List<MonthlyData> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }
    
    public List<ServiceTypeData> getServiceTypeBreakdown() {
        return serviceTypeBreakdown;
    }
    
    public void setServiceTypeBreakdown(List<ServiceTypeData> serviceTypeBreakdown) {
        this.serviceTypeBreakdown = serviceTypeBreakdown;
    }
    
    public List<RouteData> getPopularRoutes() {
        return popularRoutes;
    }
    
    public void setPopularRoutes(List<RouteData> popularRoutes) {
        this.popularRoutes = popularRoutes;
    }
    
    public double getCustomerSatisfactionScore() {
        return customerSatisfactionScore;
    }
    
    public void setCustomerSatisfactionScore(double customerSatisfactionScore) {
        this.customerSatisfactionScore = customerSatisfactionScore;
    }
    
    public int getTotalDelays() {
        return totalDelays;
    }
    
    public void setTotalDelays(int totalDelays) {
        this.totalDelays = totalDelays;
    }
    
    public double getDelayRate() {
        return delayRate;
    }
    
    public void setDelayRate(double delayRate) {
        this.delayRate = delayRate;
    }
    
    public String getMostUsedService() {
        return mostUsedService;
    }
    
    public void setMostUsedService(String mostUsedService) {
        this.mostUsedService = mostUsedService;
    }
    
    public String getPeakDeliveryDay() {
        return peakDeliveryDay;
    }
    
    public void setPeakDeliveryDay(String peakDeliveryDay) {
        this.peakDeliveryDay = peakDeliveryDay;
    }
    
    public String getPeakDeliveryTime() {
        return peakDeliveryTime;
    }
    
    public void setPeakDeliveryTime(String peakDeliveryTime) {
        this.peakDeliveryTime = peakDeliveryTime;
    }
    
    public double getMonthlySavings() {
        return monthlySavings;
    }
    
    public void setMonthlySavings(double monthlySavings) {
        this.monthlySavings = monthlySavings;
    }
    
    public double getPotentialSavings() {
        return potentialSavings;
    }
    
    public void setPotentialSavings(double potentialSavings) {
        this.potentialSavings = potentialSavings;
    }
    
    public List<CostBreakdown> getCostBreakdown() {
        return costBreakdown;
    }
    
    public void setCostBreakdown(List<CostBreakdown> costBreakdown) {
        this.costBreakdown = costBreakdown;
    }
    
    public List<String> getRecommendations() {
        return recommendations;
    }
    
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
    
    public List<String> getOptimizationSuggestions() {
        return optimizationSuggestions;
    }
    
    public void setOptimizationSuggestions(List<String> optimizationSuggestions) {
        this.optimizationSuggestions = optimizationSuggestions;
    }
    
    // Inner classes for data structures
    public static class MonthlyData {
        private String month;
        private int shipments;
        private double totalCost;
        private double averageDeliveryTime;
        
        // Getters and setters
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public int getShipments() { return shipments; }
        public void setShipments(int shipments) { this.shipments = shipments; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
        public double getAverageDeliveryTime() { return averageDeliveryTime; }
        public void setAverageDeliveryTime(double averageDeliveryTime) { this.averageDeliveryTime = averageDeliveryTime; }
    }
    
    public static class ServiceTypeData {
        private String serviceType;
        private int count;
        private double percentage;
        private double averageCost;
        
        // Getters and setters
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
        public double getAverageCost() { return averageCost; }
        public void setAverageCost(double averageCost) { this.averageCost = averageCost; }
    }
    
    public static class RouteData {
        private String fromCity;
        private String toCity;
        private int frequency;
        private double averageCost;
        private double averageDeliveryTime;
        
        // Getters and setters
        public String getFromCity() { return fromCity; }
        public void setFromCity(String fromCity) { this.fromCity = fromCity; }
        public String getToCity() { return toCity; }
        public void setToCity(String toCity) { this.toCity = toCity; }
        public int getFrequency() { return frequency; }
        public void setFrequency(int frequency) { this.frequency = frequency; }
        public double getAverageCost() { return averageCost; }
        public void setAverageCost(double averageCost) { this.averageCost = averageCost; }
        public double getAverageDeliveryTime() { return averageDeliveryTime; }
        public void setAverageDeliveryTime(double averageDeliveryTime) { this.averageDeliveryTime = averageDeliveryTime; }
    }
    
    public static class CostBreakdown {
        private String category;
        private double amount;
        private double percentage;
        
        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
}


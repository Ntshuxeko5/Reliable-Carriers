package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.AnalyticsData;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {
    
    /**
     * Generate comprehensive analytics for a customer
     */
    AnalyticsData generateCustomerAnalytics(User customer);
    
    /**
     * Generate analytics for a specific time period
     */
    AnalyticsData generateCustomerAnalytics(User customer, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get delivery performance metrics
     */
    DeliveryPerformanceMetrics getDeliveryPerformance(User customer);
    
    /**
     * Get cost analysis and savings recommendations
     */
    CostAnalysis getCostAnalysis(User customer);
    
    /**
     * Get shipping trends and patterns
     */
    ShippingTrends getShippingTrends(User customer);
    
    /**
     * Get optimization recommendations
     */
    List<String> getOptimizationRecommendations(User customer);
    
    /**
     * Check if customer has analytics access
     */
    boolean hasAnalyticsAccess(User customer);
    
    /**
     * Generate analytics report for download
     */
    byte[] generateAnalyticsReport(User customer, String format);
    
    // Inner classes for specific analytics
    class DeliveryPerformanceMetrics {
        private double onTimeDeliveryRate;
        private double averageDeliveryTime;
        private int totalDelays;
        private double delayRate;
        private String bestPerformingRoute;
        private String worstPerformingRoute;
        
        // Getters and setters
        public double getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
        public void setOnTimeDeliveryRate(double onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }
        public double getAverageDeliveryTime() { return averageDeliveryTime; }
        public void setAverageDeliveryTime(double averageDeliveryTime) { this.averageDeliveryTime = averageDeliveryTime; }
        public int getTotalDelays() { return totalDelays; }
        public void setTotalDelays(int totalDelays) { this.totalDelays = totalDelays; }
        public double getDelayRate() { return delayRate; }
        public void setDelayRate(double delayRate) { this.delayRate = delayRate; }
        public String getBestPerformingRoute() { return bestPerformingRoute; }
        public void setBestPerformingRoute(String bestPerformingRoute) { this.bestPerformingRoute = bestPerformingRoute; }
        public String getWorstPerformingRoute() { return worstPerformingRoute; }
        public void setWorstPerformingRoute(String worstPerformingRoute) { this.worstPerformingRoute = worstPerformingRoute; }
    }
    
    class CostAnalysis {
        private double totalSpent;
        private double averageCostPerShipment;
        private double potentialSavings;
        private String mostExpensiveService;
        private String mostCostEffectiveService;
        private List<CostBreakdown> costBreakdown;
        
        // Getters and setters
        public double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent; }
        public double getAverageCostPerShipment() { return averageCostPerShipment; }
        public void setAverageCostPerShipment(double averageCostPerShipment) { this.averageCostPerShipment = averageCostPerShipment; }
        public double getPotentialSavings() { return potentialSavings; }
        public void setPotentialSavings(double potentialSavings) { this.potentialSavings = potentialSavings; }
        public String getMostExpensiveService() { return mostExpensiveService; }
        public void setMostExpensiveService(String mostExpensiveService) { this.mostExpensiveService = mostExpensiveService; }
        public String getMostCostEffectiveService() { return mostCostEffectiveService; }
        public void setMostCostEffectiveService(String mostCostEffectiveService) { this.mostCostEffectiveService = mostCostEffectiveService; }
        public List<CostBreakdown> getCostBreakdown() { return costBreakdown; }
        public void setCostBreakdown(List<CostBreakdown> costBreakdown) { this.costBreakdown = costBreakdown; }
    }
    
    class ShippingTrends {
        private List<MonthlyTrend> monthlyTrends;
        private String peakShippingDay;
        private String peakShippingTime;
        private List<PopularRoute> popularRoutes;
        private List<ServiceUsage> serviceUsage;
        
        // Getters and setters
        public List<MonthlyTrend> getMonthlyTrends() { return monthlyTrends; }
        public void setMonthlyTrends(List<MonthlyTrend> monthlyTrends) { this.monthlyTrends = monthlyTrends; }
        public String getPeakShippingDay() { return peakShippingDay; }
        public void setPeakShippingDay(String peakShippingDay) { this.peakShippingDay = peakShippingDay; }
        public String getPeakShippingTime() { return peakShippingTime; }
        public void setPeakShippingTime(String peakShippingTime) { this.peakShippingTime = peakShippingTime; }
        public List<PopularRoute> getPopularRoutes() { return popularRoutes; }
        public void setPopularRoutes(List<PopularRoute> popularRoutes) { this.popularRoutes = popularRoutes; }
        public List<ServiceUsage> getServiceUsage() { return serviceUsage; }
        public void setServiceUsage(List<ServiceUsage> serviceUsage) { this.serviceUsage = serviceUsage; }
    }
    
    class CostBreakdown {
        private String category;
        private double amount;
        private double percentage;
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
    
    class MonthlyTrend {
        private String month;
        private int shipments;
        private double totalCost;
        
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public int getShipments() { return shipments; }
        public void setShipments(int shipments) { this.shipments = shipments; }
        public double getTotalCost() { return totalCost; }
        public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    }
    
    class PopularRoute {
        private String fromCity;
        private String toCity;
        private int frequency;
        private double averageCost;
        
        public String getFromCity() { return fromCity; }
        public void setFromCity(String fromCity) { this.fromCity = fromCity; }
        public String getToCity() { return toCity; }
        public void setToCity(String toCity) { this.toCity = toCity; }
        public int getFrequency() { return frequency; }
        public void setFrequency(int frequency) { this.frequency = frequency; }
        public double getAverageCost() { return averageCost; }
        public void setAverageCost(double averageCost) { this.averageCost = averageCost; }
    }
    
    class ServiceUsage {
        private String serviceType;
        private int count;
        private double percentage;
        
        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
}
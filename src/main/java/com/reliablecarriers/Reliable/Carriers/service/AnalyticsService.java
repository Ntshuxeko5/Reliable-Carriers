package com.reliablecarriers.Reliable.Carriers.service;

import java.util.Map;

public interface AnalyticsService {
    
    /**
     * Get comprehensive analytics data for the specified time range
     */
    Map<String, Object> getAnalytics(int timeRange);
    
    /**
     * Get revenue analytics
     */
    Map<String, Object> getRevenueAnalytics(int timeRange);
    
    /**
     * Get shipment analytics
     */
    Map<String, Object> getShipmentAnalytics(int timeRange);
    
    /**
     * Get customer analytics
     */
    Map<String, Object> getCustomerAnalytics(int timeRange);
    
    /**
     * Get driver performance analytics
     */
    Map<String, Object> getDriverAnalytics(int timeRange);
    
    /**
     * Get geographic analytics
     */
    Map<String, Object> getGeographicAnalytics(int timeRange);
    
    /**
     * Get time-based analytics
     */
    Map<String, Object> getTimeBasedAnalytics(int timeRange);
    
    /**
     * Get service performance analytics
     */
    Map<String, Object> getServiceAnalytics(int timeRange);
    
    /**
     * Get customer satisfaction analytics
     */
    Map<String, Object> getSatisfactionAnalytics(int timeRange);
    
    /**
     * Export analytics data
     */
    Map<String, Object> exportAnalytics(int timeRange, String format);
    
    /**
     * Get real-time analytics
     */
    Map<String, Object> getRealTimeAnalytics();
    
    /**
     * Get predictive analytics
     */
    Map<String, Object> getPredictiveAnalytics(int forecastDays);
}

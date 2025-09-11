package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get comprehensive analytics data
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> analytics = analyticsService.getAnalytics(timeRange);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get revenue analytics
     */
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> revenueData = analyticsService.getRevenueAnalytics(timeRange);
        return ResponseEntity.ok(revenueData);
    }

    /**
     * Get shipment analytics
     */
    @GetMapping("/shipments")
    public ResponseEntity<Map<String, Object>> getShipmentAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> shipmentData = analyticsService.getShipmentAnalytics(timeRange);
        return ResponseEntity.ok(shipmentData);
    }

    /**
     * Get customer analytics
     */
    @GetMapping("/customers")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> customerData = analyticsService.getCustomerAnalytics(timeRange);
        return ResponseEntity.ok(customerData);
    }

    /**
     * Get driver performance analytics
     */
    @GetMapping("/drivers")
    public ResponseEntity<Map<String, Object>> getDriverAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> driverData = analyticsService.getDriverAnalytics(timeRange);
        return ResponseEntity.ok(driverData);
    }

    /**
     * Get geographic analytics
     */
    @GetMapping("/geographic")
    public ResponseEntity<Map<String, Object>> getGeographicAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> geographicData = analyticsService.getGeographicAnalytics(timeRange);
        return ResponseEntity.ok(geographicData);
    }

    /**
     * Get time-based analytics
     */
    @GetMapping("/time-based")
    public ResponseEntity<Map<String, Object>> getTimeBasedAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> timeData = analyticsService.getTimeBasedAnalytics(timeRange);
        return ResponseEntity.ok(timeData);
    }

    /**
     * Get service performance analytics
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getServiceAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> serviceData = analyticsService.getServiceAnalytics(timeRange);
        return ResponseEntity.ok(serviceData);
    }

    /**
     * Get customer satisfaction analytics
     */
    @GetMapping("/satisfaction")
    public ResponseEntity<Map<String, Object>> getSatisfactionAnalytics(
            @RequestParam(defaultValue = "30") int timeRange) {
        
        Map<String, Object> satisfactionData = analyticsService.getSatisfactionAnalytics(timeRange);
        return ResponseEntity.ok(satisfactionData);
    }

    /**
     * Export analytics data
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportAnalytics(
            @RequestParam(defaultValue = "30") int timeRange,
            @RequestParam(defaultValue = "json") String format) {
        
        Map<String, Object> exportData = analyticsService.exportAnalytics(timeRange, format);
        return ResponseEntity.ok(exportData);
    }

    /**
     * Get real-time analytics
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealTimeAnalytics() {
        
        Map<String, Object> realTimeData = analyticsService.getRealTimeAnalytics();
        return ResponseEntity.ok(realTimeData);
    }

    /**
     * Get predictive analytics
     */
    @GetMapping("/predictive")
    public ResponseEntity<Map<String, Object>> getPredictiveAnalytics(
            @RequestParam(defaultValue = "30") int forecastDays) {
        
        Map<String, Object> predictiveData = analyticsService.getPredictiveAnalytics(forecastDays);
        return ResponseEntity.ok(predictiveData);
    }
}

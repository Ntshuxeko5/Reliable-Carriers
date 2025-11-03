package com.reliablecarriers.Reliable.Carriers.service;

import java.util.List;
import java.util.Map;

public interface AdminDriverTrackingService {
    
    /**
     * Get all driver locations for admin tracking
     */
    Map<String, Object> getAllDriverLocations();
    
    /**
     * Get specific driver details
     */
    Map<String, Object> getDriverDetails(Long driverId);
    
    /**
     * Get driver location history
     */
    List<Map<String, Object>> getDriverLocationHistory(Long driverId, int hours);
    
    /**
     * Get driver statistics
     */
    Map<String, Object> getDriverStatistics();
    
    /**
     * Contact driver (send notification)
     */
    void contactDriver(Long driverId, String message);
    
    /**
     * Assign package to driver
     */
    void assignPackageToDriver(Long driverId, Long packageId);
    
    /**
     * Get driver performance metrics
     */
    Map<String, Object> getDriverPerformance(Long driverId);
    
    /**
     * Update driver status
     */
    void updateDriverStatus(Long driverId, String status);
    
    /**
     * Get real-time driver locations for WebSocket
     */
    List<Map<String, Object>> getRealTimeDriverLocations();
}


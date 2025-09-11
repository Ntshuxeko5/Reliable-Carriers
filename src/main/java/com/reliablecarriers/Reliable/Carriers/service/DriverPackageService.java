package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageInfo;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.util.List;
import java.util.Map;

public interface DriverPackageService {
    
    /**
     * Get all packages assigned to a driver with route optimization
     */
    List<DriverPackageInfo> getDriverPackagesWithRouteOptimization(Long driverId, Double currentLat, Double currentLng);
    
    /**
     * Get packages currently being carried by the driver
     */
    List<DriverPackageInfo> getCurrentlyCarriedPackages(Long driverId);
    
    /**
     * Get packages that need to be picked up by the driver
     */
    List<DriverPackageInfo> getPackagesToPickup(Long driverId);
    
    /**
     * Get packages ready for delivery (already picked up)
     */
    List<DriverPackageInfo> getPackagesForDelivery(Long driverId);
    
    /**
     * Get optimized delivery route for driver's packages
     */
    List<DriverPackageInfo> getOptimizedDeliveryRoute(Long driverId, Double currentLat, Double currentLng);
    
    /**
     * Get package delivery statistics for a driver
     */
    Map<String, Object> getDriverPackageStatistics(Long driverId);
    
    /**
     * Update package status (pickup/delivery)
     */
    DriverPackageInfo updatePackageStatus(Long packageId, String newStatus, String location, String notes);
    
    /**
     * Get next recommended package to deliver
     */
    DriverPackageInfo getNextRecommendedPackage(Long driverId, Double currentLat, Double currentLng);
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2);
    
    /**
     * Estimate travel time between two points (in minutes)
     */
    Integer estimateTravelTime(Double lat1, Double lng1, Double lat2, Double lng2);
}

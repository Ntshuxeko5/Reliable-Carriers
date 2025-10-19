package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageInfo;
import com.reliablecarriers.Reliable.Carriers.dto.PackagePickupRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PackageDeliveryRequest;
import com.reliablecarriers.Reliable.Carriers.dto.WorkboardStats;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface DriverWorkboardService {
    
    /**
     * Get comprehensive workboard statistics
     */
    WorkboardStats getWorkboardStats(Long driverId, Double currentLat, Double currentLng);
    
    /**
     * Get available packages for pickup with distance-based recommendations
     */
    List<DriverPackageInfo> getAvailablePackagesForPickup(Long driverId, Double currentLat, Double currentLng, Double maxDistance, Integer page, Integer size);
    
    /**
     * Get packages currently assigned to driver
     */
    List<DriverPackageInfo> getAssignedPackages(Long driverId, Double currentLat, Double currentLng);
    
    /**
     * Get optimized delivery route
     */
    List<DriverPackageInfo> getOptimizedRoute(Long driverId, Double currentLat, Double currentLng);
    
    /**
     * Pick up a package with signature and photo capture
     */
    DriverPackageInfo pickupPackage(PackagePickupRequest request);
    
    /**
     * Deliver a package with signature and photo capture
     */
    DriverPackageInfo deliverPackage(PackageDeliveryRequest request);
    
    /**
     * Request to pick up additional packages
     */
    void requestPackagePickup(Long driverId, Long packageId);
    
    /**
     * Get nearby packages for potential pickup
     */
    List<DriverPackageInfo> getNearbyPackages(Long driverId, Double currentLat, Double currentLng, Double radius);
    
    /**
     * Get detailed package information
     */
    Map<String, Object> getPackageDetails(Long driverId, Long packageId);
    
    /**
     * Update driver's current location
     */
    void updateDriverLocation(Long driverId, Double lat, Double lng, String address);
    
    /**
     * Get driver's current location
     */
    Map<String, Object> getDriverLocation(Long driverId);
    
    /**
     * Get today's work summary
     */
    Map<String, Object> getTodaySummary(Long driverId);
    
    /**
     * Mark package as failed delivery
     */
    DriverPackageInfo markFailedDelivery(Long driverId, Long packageId, String reason, String notes, MultipartFile photo);
    
    /**
     * Get delivery history
     */
    List<Map<String, Object>> getDeliveryHistory(Long driverId, String date);
    
    /**
     * Get earnings summary
     */
    Map<String, Object> getEarningsSummary(Long driverId, String period);
    
    /**
     * Calculate distance between two points
     */
    Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2);
    
    /**
     * Estimate travel time between two points
     */
    Integer estimateTravelTime(Double lat1, Double lng1, Double lat2, Double lng2);
    
    /**
     * Get package pickup history
     */
    List<Map<String, Object>> getPickupHistory(Long driverId, String date);
    
    /**
     * Get driver performance metrics
     */
    Map<String, Object> getDriverPerformanceMetrics(Long driverId);
    
    /**
     * Update driver status
     */
    void updateDriverStatus(Long driverId, String status);
    
    /**
     * Get real-time package tracking updates
     */
    List<Map<String, Object>> getRealTimeUpdates(Long driverId);
    
    /**
     * Accept a package assignment
     */
    boolean acceptPackage(Long driverId, Long packageId);
    
    /**
     * Reject a package assignment
     */
    boolean rejectPackage(Long driverId, Long packageId, String reason);
}

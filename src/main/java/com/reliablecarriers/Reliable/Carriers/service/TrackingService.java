package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.DriverLocationResponse;
import com.reliablecarriers.Reliable.Carriers.dto.TrackingRequest;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TrackingService {
    
    /**
     * Get all active driver locations for tracking
     */
    List<DriverLocationResponse> getAllActiveDriverLocations();
    
    /**
     * Get driver locations based on tracking request filters
     */
    List<DriverLocationResponse> getDriverLocationsByFilters(TrackingRequest request);
    
    /**
     * Get real-time location updates for specific drivers
     */
    List<DriverLocationResponse> getRealTimeLocations(List<Long> driverIds);
    
    /**
     * Get driver location history within a time range
     */
    List<DriverLocationResponse> getDriverLocationHistory(Long driverId, Date startTime, Date endTime);
    
    /**
     * Get vehicle location history within a time range
     */
    List<DriverLocationResponse> getVehicleLocationHistory(Long vehicleId, Date startTime, Date endTime);
    
    /**
     * Get drivers within a geographic bounding box
     */
    List<DriverLocationResponse> getDriversInBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng);
    
    /**
     * Get drivers by city and state
     */
    List<DriverLocationResponse> getDriversByLocation(String city, String state);
    
    /**
     * Search drivers by name, vehicle plate, or other criteria
     */
    List<DriverLocationResponse> searchDrivers(String searchTerm);
    
    /**
     * Get tracking statistics
     */
    Map<String, Object> getTrackingStatistics();
    
    /**
     * Get driver status summary
     */
    Map<String, Long> getDriverStatusSummary();
    
    /**
     * Get vehicle tracking summary
     */
    Map<String, Object> getVehicleTrackingSummary(Long vehicleId);
    
    /**
     * Get driver tracking summary
     */
    Map<String, Object> getDriverTrackingSummary(Long driverId);
    
    /**
     * Check if driver is currently online
     */
    boolean isDriverOnline(Long driverId);
    
    /**
     * Get last known location for a driver
     */
    DriverLocationResponse getLastKnownLocation(Long driverId);
    
    /**
     * Get all drivers with their current status
     */
    List<Map<String, Object>> getAllDriversWithStatus();
    
    /**
     * Get all vehicles with their current status
     */
    List<Map<String, Object>> getAllVehiclesWithStatus();
}

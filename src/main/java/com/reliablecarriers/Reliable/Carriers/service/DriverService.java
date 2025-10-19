package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.DriverResponse;
// Removed unused import
import com.reliablecarriers.Reliable.Carriers.model.UserRole;

import java.util.List;
import java.util.Map;

public interface DriverService {
    
    /**
     * Get driver by ID with vehicle information
     */
    DriverResponse getDriverById(Long driverId);
    
    /**
     * Get all drivers with their vehicle information
     */
    List<DriverResponse> getAllDrivers();
    
    /**
     * Get all drivers with a specific role
     */
    List<DriverResponse> getDriversByRole(UserRole role);
    
    /**
     * Get drivers by vehicle make and model
     */
    List<DriverResponse> getDriversByVehicleMakeAndModel(String make, String model);
    
    /**
     * Get drivers by vehicle type
     */
    List<DriverResponse> getDriversByVehicleType(String vehicleType);
    
    /**
     * Get drivers by location (city and state)
     */
    List<DriverResponse> getDriversByLocation(String city, String state);
    
    /**
     * Get online drivers
     */
    List<DriverResponse> getOnlineDrivers();
    
    /**
     * Get offline drivers
     */
    List<DriverResponse> getOfflineDrivers();
    
    /**
     * Get drivers with active packages
     */
    List<DriverResponse> getDriversWithActivePackages();
    
    /**
     * Get drivers without assigned vehicles
     */
    List<DriverResponse> getDriversWithoutVehicles();
    
    /**
     * Search drivers by name, email, or vehicle information
     */
    List<DriverResponse> searchDrivers(String searchTerm);
    
    /**
     * Get driver statistics
     */
    Map<String, Object> getDriverStatistics();
    
    /**
     * Get driver performance metrics
     */
    Map<String, Object> getDriverPerformanceMetrics(Long driverId);
    
    /**
     * Get drivers by vehicle registration number
     */
    DriverResponse getDriverByVehicleRegistration(String registrationNumber);
    
    /**
     * Get drivers with vehicles due for maintenance
     */
    List<DriverResponse> getDriversWithVehiclesDueForMaintenance();
    
    /**
     * Get drivers by vehicle capacity range
     */
    List<DriverResponse> getDriversByVehicleCapacityRange(Double minCapacity, Double maxCapacity);
    
    /**
     * Get drivers by vehicle year range
     */
    List<DriverResponse> getDriversByVehicleYearRange(Integer startYear, Integer endYear);
    
    /**
     * Get drivers by vehicle fuel type
     */
    List<DriverResponse> getDriversByVehicleFuelType(String fuelType);
    
    /**
     * Get drivers by vehicle color
     */
    List<DriverResponse> getDriversByVehicleColor(String color);
    
    /**
     * Get drivers with vehicles having specific features
     */
    List<DriverResponse> getDriversByVehicleFeatures(String... features);
    
    /**
     * Get driver count by vehicle make
     */
    Map<String, Long> getDriverCountByVehicleMake();
    
    /**
     * Get driver count by vehicle type
     */
    Map<String, Long> getDriverCountByVehicleType();
    
    /**
     * Get driver count by location
     */
    Map<String, Long> getDriverCountByLocation();
    
    /**
     * Get driver count by status
     */
    Map<String, Long> getDriverCountByStatus();
}

package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;

import java.time.LocalDateTime;
import java.util.List;

public interface DriverLocationService {
    
    // Create a new driver location entry
    DriverLocation createDriverLocation(DriverLocation driverLocation);
    
    // Get a specific driver location by ID
    DriverLocation getDriverLocationById(Long id);
    
    // Get all driver locations
    List<DriverLocation> getAllDriverLocations();
    
    // Get the most recent location for a specific driver
    DriverLocation getMostRecentDriverLocation(User driver);
    
    // Get all locations for a specific driver
    List<DriverLocation> getDriverLocationsByDriver(User driver);
    
    // Get locations for a specific driver within a time range
    List<DriverLocation> getDriverLocationsByDriverAndTimeRange(User driver, LocalDateTime startTime, LocalDateTime endTime);
    
    // Get locations for a specific vehicle
    List<DriverLocation> getDriverLocationsByVehicle(Vehicle vehicle);
    
    // Get locations within a geographic area (by city and state)
    List<DriverLocation> getDriverLocationsByCityAndState(String city, String state);
    
    // Update a driver location
    DriverLocation updateDriverLocation(Long id, DriverLocation driverLocation);
    
    // Delete a driver location
    void deleteDriverLocation(Long id);
    
    // Get drivers within a specific radius of coordinates (for future implementation)
    // List<DriverLocation> getDriversNearLocation(Double latitude, Double longitude, Double radiusInKm);
}
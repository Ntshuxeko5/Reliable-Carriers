package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    
    // Find the most recent location for a specific driver
    DriverLocation findTopByDriverOrderByTimestampDesc(User driver);
    
    // Find all locations for a specific driver ordered by timestamp (most recent first)
    List<DriverLocation> findByDriverOrderByTimestampDesc(User driver);
    
    // Find locations for a specific driver within a time range
    List<DriverLocation> findByDriverAndTimestampBetweenOrderByTimestampDesc(User driver, Date startTime, Date endTime);
    
    // Find locations for a specific vehicle
    List<DriverLocation> findByVehicleOrderByTimestampDesc(Vehicle vehicle);
    
    // Find the most recent location for a specific vehicle
    DriverLocation findTopByVehicleOrderByTimestampDesc(Vehicle vehicle);
    
    // Find locations for a specific vehicle within a time range
    List<DriverLocation> findByVehicleAndTimestampBetweenOrderByTimestampDesc(Vehicle vehicle, Date startTime, Date endTime);
    
    // Find locations within a geographic area (approximate by city and state)
    List<DriverLocation> findByCityAndStateOrderByTimestampDesc(String city, String state);
    
    // Find locations by city and state (simplified method name)
    List<DriverLocation> findByCityAndState(String city, String state);
    
    // Find locations within a geographic bounding box
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.latitude BETWEEN :minLat AND :maxLat AND dl.longitude BETWEEN :minLng AND :maxLng")
    List<DriverLocation> findByLatitudeBetweenAndLongitudeBetween(
        @Param("minLat") Double minLat, 
        @Param("maxLat") Double maxLat, 
        @Param("minLng") Double minLng, 
        @Param("maxLng") Double maxLng
    );
    
    // Find locations within a specific radius (would require custom implementation or native query)
    // This is a placeholder for future implementation
    // List<DriverLocation> findByLatitudeAndLongitudeWithinRadius(Double latitude, Double longitude, Double radiusInKm);
    
    // Driver workboard methods
    DriverLocation findByDriverIdOrderByTimestampDesc(Long driverId);
    
    // Analytics methods
    @Query("SELECT COUNT(DISTINCT dl.driver.id) FROM DriverLocation dl WHERE dl.timestamp >= :timestamp")
    long countActiveDrivers(@Param("timestamp") Date timestamp);
}
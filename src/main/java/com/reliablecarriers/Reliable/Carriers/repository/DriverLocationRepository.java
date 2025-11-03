package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    
    /**
     * Find the latest location for a specific driver
     */
    DriverLocation findTopByDriverIdOrderByTimestampDesc(Long driverId);
    
    /**
     * Find locations for a driver after a specific timestamp
     */
    List<DriverLocation> findByDriverIdAndTimestampAfterOrderByTimestampDesc(
        Long driverId, LocalDateTime timestamp);
    
    /**
     * Find all locations for a driver within a time range
     */
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driverId = :driverId " +
           "AND dl.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY dl.timestamp DESC")
    List<DriverLocation> findByDriverIdAndTimestampBetween(
        @Param("driverId") Long driverId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find recent locations for all drivers (within last 5 minutes)
     */
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.timestamp > :cutoffTime " +
           "ORDER BY dl.timestamp DESC")
    List<DriverLocation> findRecentLocations(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find locations within a geographic bounding box
     */
    @Query("SELECT dl FROM DriverLocation dl WHERE " +
           "dl.latitude BETWEEN :minLat AND :maxLat AND " +
           "dl.longitude BETWEEN :minLng AND :maxLng AND " +
           "dl.timestamp > :cutoffTime " +
           "ORDER BY dl.timestamp DESC")
    List<DriverLocation> findLocationsInBounds(
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng,
        @Param("maxLng") Double maxLng,
        @Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Delete old location records (cleanup)
     */
    void deleteByTimestampBefore(LocalDateTime cutoffTime);
    
    /**
     * Count locations for a driver
     */
    long countByDriverId(Long driverId);
    
    /**
     * Find the first location for a driver
     */
    DriverLocation findFirstByDriverIdOrderByTimestampAsc(Long driverId);
    
    // Additional methods for compatibility - using driverId instead of driver object
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driverId = :driverId ORDER BY dl.timestamp DESC")
    DriverLocation findTopByDriverOrderByTimestampDesc(@Param("driverId") Long driverId);
    
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driverId = :driverId ORDER BY dl.timestamp DESC")
    List<DriverLocation> findByDriverOrderByTimestampDesc(@Param("driverId") Long driverId);
    
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driverId = :driverId " +
           "AND dl.timestamp BETWEEN :startDate AND :endDate ORDER BY dl.timestamp DESC")
    List<DriverLocation> findByDriverAndTimestampBetweenOrderByTimestampDesc(
        @Param("driverId") Long driverId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.vehicle = :vehicleRegistration ORDER BY dl.timestamp DESC")
    DriverLocation findTopByVehicleOrderByTimestampDesc(@Param("vehicleRegistration") String vehicleRegistration);
    
    List<DriverLocation> findByCityAndStateOrderByTimestampDesc(String city, String state);
    List<DriverLocation> findByLatitudeBetweenAndLongitudeBetween(Double minLat, Double maxLat, Double minLng, Double maxLng);
    List<DriverLocation> findByCityAndState(String city, String state);
    
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.vehicle = :vehicleRegistration " +
           "AND dl.timestamp BETWEEN :startDate AND :endDate ORDER BY dl.timestamp DESC")
    List<DriverLocation> findByVehicleAndTimestampBetweenOrderByTimestampDesc(
        @Param("vehicleRegistration") String vehicleRegistration, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    // Additional method for finding by vehicle registration string
    List<DriverLocation> findByVehicleOrderByTimestampDesc(String vehicleRegistration);
}
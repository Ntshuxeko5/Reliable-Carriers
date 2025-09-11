package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.DriverLocationResponse;
import com.reliablecarriers.Reliable.Carriers.dto.TrackingRequest;
import com.reliablecarriers.Reliable.Carriers.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
@PreAuthorize("hasRole('TRACKING_MANAGER') or hasRole('ADMIN')")
public class TrackingController {

    private final TrackingService trackingService;

    @Autowired
    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Get all active driver locations for real-time tracking
     */
    @GetMapping("/active-drivers")
    public ResponseEntity<List<DriverLocationResponse>> getActiveDrivers() {
        List<DriverLocationResponse> activeDrivers = trackingService.getAllActiveDriverLocations();
        return ResponseEntity.ok(activeDrivers);
    }

    /**
     * Get driver locations with advanced filtering
     */
    @PostMapping("/filter")
    public ResponseEntity<List<DriverLocationResponse>> getDriversByFilters(@RequestBody TrackingRequest request) {
        List<DriverLocationResponse> drivers = trackingService.getDriverLocationsByFilters(request);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get real-time location updates for specific drivers
     */
    @PostMapping("/realtime")
    public ResponseEntity<List<DriverLocationResponse>> getRealTimeLocations(@RequestBody List<Long> driverIds) {
        List<DriverLocationResponse> realTimeLocations = trackingService.getRealTimeLocations(driverIds);
        return ResponseEntity.ok(realTimeLocations);
    }

    /**
     * Get driver location history within a time range
     */
    @GetMapping("/driver/{driverId}/history")
    public ResponseEntity<List<DriverLocationResponse>> getDriverHistory(
            @PathVariable Long driverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        
        List<DriverLocationResponse> history = trackingService.getDriverLocationHistory(driverId, startTime, endTime);
        return ResponseEntity.ok(history);
    }

    /**
     * Get vehicle location history within a time range
     */
    @GetMapping("/vehicle/{vehicleId}/history")
    public ResponseEntity<List<DriverLocationResponse>> getVehicleHistory(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        
        List<DriverLocationResponse> history = trackingService.getVehicleLocationHistory(vehicleId, startTime, endTime);
        return ResponseEntity.ok(history);
    }

    /**
     * Get drivers within a geographic bounding box
     */
    @GetMapping("/bounding-box")
    public ResponseEntity<List<DriverLocationResponse>> getDriversInBoundingBox(
            @RequestParam Double minLat,
            @RequestParam Double maxLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLng) {
        
        List<DriverLocationResponse> drivers = trackingService.getDriversInBoundingBox(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get drivers by city and state
     */
    @GetMapping("/location")
    public ResponseEntity<List<DriverLocationResponse>> getDriversByLocation(
            @RequestParam String city,
            @RequestParam String state) {
        
        List<DriverLocationResponse> drivers = trackingService.getDriversByLocation(city, state);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Search drivers by name, vehicle plate, or other criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<DriverLocationResponse>> searchDrivers(@RequestParam String searchTerm) {
        List<DriverLocationResponse> drivers = trackingService.searchDrivers(searchTerm);
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get tracking statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTrackingStatistics() {
        Map<String, Object> statistics = trackingService.getTrackingStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get driver status summary
     */
    @GetMapping("/driver-status-summary")
    public ResponseEntity<Map<String, Long>> getDriverStatusSummary() {
        Map<String, Long> summary = trackingService.getDriverStatusSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Get vehicle tracking summary
     */
    @GetMapping("/vehicle/{vehicleId}/summary")
    public ResponseEntity<Map<String, Object>> getVehicleTrackingSummary(@PathVariable Long vehicleId) {
        Map<String, Object> summary = trackingService.getVehicleTrackingSummary(vehicleId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get driver tracking summary
     */
    @GetMapping("/driver/{driverId}/summary")
    public ResponseEntity<Map<String, Object>> getDriverTrackingSummary(@PathVariable Long driverId) {
        Map<String, Object> summary = trackingService.getDriverTrackingSummary(driverId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Check if driver is currently online
     */
    @GetMapping("/driver/{driverId}/online-status")
    public ResponseEntity<Map<String, Boolean>> isDriverOnline(@PathVariable Long driverId) {
        boolean isOnline = trackingService.isDriverOnline(driverId);
        return ResponseEntity.ok(Map.of("isOnline", isOnline));
    }

    /**
     * Get last known location for a driver
     */
    @GetMapping("/driver/{driverId}/last-location")
    public ResponseEntity<DriverLocationResponse> getLastKnownLocation(@PathVariable Long driverId) {
        DriverLocationResponse location = trackingService.getLastKnownLocation(driverId);
        if (location != null) {
            return ResponseEntity.ok(location);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all drivers with their current status
     */
    @GetMapping("/drivers-with-status")
    public ResponseEntity<List<Map<String, Object>>> getAllDriversWithStatus() {
        List<Map<String, Object>> drivers = trackingService.getAllDriversWithStatus();
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get all vehicles with their current status
     */
    @GetMapping("/vehicles-with-status")
    public ResponseEntity<List<Map<String, Object>>> getAllVehiclesWithStatus() {
        List<Map<String, Object>> vehicles = trackingService.getAllVehiclesWithStatus();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Get map view data - combines multiple tracking endpoints for map display
     */
    @GetMapping("/map-view")
    public ResponseEntity<Map<String, Object>> getMapViewData() {
        Map<String, Object> mapData = Map.of(
            "activeDrivers", trackingService.getAllActiveDriverLocations(),
            "statistics", trackingService.getTrackingStatistics(),
            "driverStatusSummary", trackingService.getDriverStatusSummary(),
            "allDrivers", trackingService.getAllDriversWithStatus(),
            "allVehicles", trackingService.getAllVehiclesWithStatus()
        );
        return ResponseEntity.ok(mapData);
    }

    /**
     * Test endpoint to get driver with package information
     */
    @GetMapping("/driver/{driverId}/with-packages")
    public ResponseEntity<DriverLocationResponse> getDriverWithPackages(@PathVariable Long driverId) {
        DriverLocationResponse location = trackingService.getLastKnownLocation(driverId);
        if (location != null) {
            return ResponseEntity.ok(location);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

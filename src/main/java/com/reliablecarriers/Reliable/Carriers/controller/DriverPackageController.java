package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageInfo;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.DriverPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('DRIVER')")
public class DriverPackageController {

    @Autowired
    private DriverPackageService driverPackageService;

    @Autowired
    private AuthService authService;

    /**
     * Get all packages assigned to the current driver with route optimization
     */
    @GetMapping("/packages")
    public ResponseEntity<List<DriverPackageInfo>> getDriverPackages(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = driverPackageService.getDriverPackagesWithRouteOptimization(driverId, currentLat, currentLng);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get packages currently being carried by the driver
     */
    @GetMapping("/packages/carrying")
    public ResponseEntity<List<DriverPackageInfo>> getCurrentlyCarriedPackages() {
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = driverPackageService.getCurrentlyCarriedPackages(driverId);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get packages that need to be picked up
     */
    @GetMapping("/packages/pickup")
    public ResponseEntity<List<DriverPackageInfo>> getPackagesToPickup() {
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = driverPackageService.getPackagesToPickup(driverId);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get packages ready for delivery
     */
    @GetMapping("/packages/delivery")
    public ResponseEntity<List<DriverPackageInfo>> getPackagesForDelivery() {
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = driverPackageService.getPackagesForDelivery(driverId);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get optimized delivery route
     */
    @GetMapping("/packages/optimized-route")
    public ResponseEntity<List<DriverPackageInfo>> getOptimizedDeliveryRoute(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        List<DriverPackageInfo> packages = driverPackageService.getOptimizedDeliveryRoute(driverId, currentLat, currentLng);
        return ResponseEntity.ok(packages);
    }

    /**
     * Get package delivery statistics
     */
    @GetMapping("/packages/statistics")
    public ResponseEntity<Map<String, Object>> getDriverPackageStatistics() {
        Long driverId = authService.getCurrentUser().getId();
        Map<String, Object> statistics = driverPackageService.getDriverPackageStatistics(driverId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Update package status (pickup/delivery)
     */
    @PutMapping("/packages/{packageId}/status")
    public ResponseEntity<DriverPackageInfo> updatePackageStatus(
            @PathVariable Long packageId,
            @RequestParam String newStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes) {
        
        DriverPackageInfo updatedPackage = driverPackageService.updatePackageStatus(packageId, newStatus, location, notes);
        return ResponseEntity.ok(updatedPackage);
    }

    /**
     * Get next recommended package to deliver
     */
    @GetMapping("/packages/next-recommended")
    public ResponseEntity<DriverPackageInfo> getNextRecommendedPackage(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        DriverPackageInfo nextPackage = driverPackageService.getNextRecommendedPackage(driverId, currentLat, currentLng);
        
        if (nextPackage == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(nextPackage);
    }

    /**
     * Calculate distance between two points
     */
    @GetMapping("/calculate-distance")
    public ResponseEntity<Double> calculateDistance(
            @RequestParam Double lat1,
            @RequestParam Double lng1,
            @RequestParam Double lat2,
            @RequestParam Double lng2) {
        
        Double distance = driverPackageService.calculateDistance(lat1, lng1, lat2, lng2);
        return ResponseEntity.ok(distance);
    }

    /**
     * Estimate travel time between two points
     */
    @GetMapping("/estimate-travel-time")
    public ResponseEntity<Integer> estimateTravelTime(
            @RequestParam Double lat1,
            @RequestParam Double lng1,
            @RequestParam Double lat2,
            @RequestParam Double lng2) {
        
        Integer travelTime = driverPackageService.estimateTravelTime(lat1, lng1, lat2, lng2);
        return ResponseEntity.ok(travelTime);
    }
}

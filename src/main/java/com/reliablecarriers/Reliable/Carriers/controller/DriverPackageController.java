package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageInfo;
import com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.DriverPackageService;
import com.reliablecarriers.Reliable.Carriers.service.UnifiedPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('DRIVER')")
public class DriverPackageController {

    @Autowired
    private DriverPackageService driverPackageService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UnifiedPackageService unifiedPackageService;

    /**
     * Get all packages assigned to the current driver with route optimization - Uses UnifiedPackageService
     */
    @GetMapping("/packages")
    public ResponseEntity<List<DriverPackageInfo>> getDriverPackages(
            @RequestParam(required = false) Double currentLat,
            @RequestParam(required = false) Double currentLng) {
        
        Long driverId = authService.getCurrentUser().getId();
        
        try {
            // Use unified service for seamless integration
            List<UnifiedPackageDTO> unifiedPackages = unifiedPackageService.getPackagesByDriverId(driverId);
            List<DriverPackageInfo> packages = unifiedPackages.stream()
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
            
            // Apply route optimization if coordinates provided
            if (currentLat != null && currentLng != null && !packages.isEmpty()) {
                // Use original service for route optimization
                List<DriverPackageInfo> optimized = driverPackageService.getDriverPackagesWithRouteOptimization(driverId, currentLat, currentLng);
                // Merge optimization data
                for (DriverPackageInfo pkg : packages) {
                    optimized.stream()
                        .filter(opt -> opt.getTrackingNumber().equals(pkg.getTrackingNumber()))
                        .findFirst()
                        .ifPresent(opt -> {
                            pkg.setDeliveryPriority(opt.getDeliveryPriority());
                            pkg.setDistanceFromCurrentLocation(opt.getDistanceFromCurrentLocation());
                            pkg.setEstimatedTimeMinutes(opt.getEstimatedTimeMinutes());
                            pkg.setSuggestedRoute(opt.getSuggestedRoute());
                        });
                }
            }
            
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            // Fallback to original service
            List<DriverPackageInfo> packages = driverPackageService.getDriverPackagesWithRouteOptimization(driverId, currentLat, currentLng);
            return ResponseEntity.ok(packages);
        }
    }

    /**
     * Get packages currently being carried by the driver - Uses UnifiedPackageService
     */
    @GetMapping("/packages/carrying")
    public ResponseEntity<List<DriverPackageInfo>> getCurrentlyCarriedPackages() {
        Long driverId = authService.getCurrentUser().getId();
        
        try {
            // Use unified service
            List<UnifiedPackageDTO> unifiedPackages = unifiedPackageService.getPackagesByDriverId(driverId);
            List<DriverPackageInfo> packages = unifiedPackages.stream()
                .filter(p -> p.isPickedUp() || p.isInTransit())
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            // Fallback to original service
            List<DriverPackageInfo> packages = driverPackageService.getCurrentlyCarriedPackages(driverId);
            return ResponseEntity.ok(packages);
        }
    }

    /**
     * Get packages that need to be picked up - Uses UnifiedPackageService
     */
    @GetMapping("/packages/pickup")
    public ResponseEntity<List<DriverPackageInfo>> getPackagesToPickup() {
        Long driverId = authService.getCurrentUser().getId();
        
        try {
            // Use unified service
            List<UnifiedPackageDTO> unifiedPackages = unifiedPackageService.getPackagesByDriverId(driverId);
            List<DriverPackageInfo> packages = unifiedPackages.stream()
                .filter(p -> p.isPending() || p.isAssigned())
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            // Fallback to original service
            List<DriverPackageInfo> packages = driverPackageService.getPackagesToPickup(driverId);
            return ResponseEntity.ok(packages);
        }
    }

    /**
     * Get packages ready for delivery - Uses UnifiedPackageService
     */
    @GetMapping("/packages/delivery")
    public ResponseEntity<List<DriverPackageInfo>> getPackagesForDelivery() {
        Long driverId = authService.getCurrentUser().getId();
        
        try {
            // Use unified service
            List<UnifiedPackageDTO> unifiedPackages = unifiedPackageService.getPackagesByDriverId(driverId);
            List<DriverPackageInfo> packages = unifiedPackages.stream()
                .filter(p -> p.isPickedUp() || p.isInTransit())
                .map(this::convertToDriverPackageInfo)
                .collect(Collectors.toList());
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            // Fallback to original service
            List<DriverPackageInfo> packages = driverPackageService.getPackagesForDelivery(driverId);
            return ResponseEntity.ok(packages);
        }
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
     * Update package status (pickup/delivery) - Uses UnifiedPackageService for synchronization
     */
    @PutMapping("/packages/{packageId}/status")
    public ResponseEntity<DriverPackageInfo> updatePackageStatus(
            @PathVariable Long packageId,
            @RequestParam String newStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes) {
        
        try {
            // Get package by ID to find tracking number
            List<UnifiedPackageDTO> allPackages = unifiedPackageService.getAllPackages();
            UnifiedPackageDTO unifiedPackage = allPackages.stream()
                .filter(p -> p.getId() != null && p.getId().equals(packageId))
                .findFirst()
                .orElse(null);
            
            if (unifiedPackage == null || unifiedPackage.getTrackingNumber() == null) {
                // Fallback to original service
                DriverPackageInfo updatedPackage = driverPackageService.updatePackageStatus(packageId, newStatus, location, notes);
                return ResponseEntity.ok(updatedPackage);
            }
            
            // Use unified service to update status (synchronizes Booking and Shipment)
            UnifiedPackageDTO updated = unifiedPackageService.updatePackageStatus(
                unifiedPackage.getTrackingNumber(), newStatus);
            
            DriverPackageInfo result = convertToDriverPackageInfo(updated);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // Fallback to original service
            DriverPackageInfo updatedPackage = driverPackageService.updatePackageStatus(packageId, newStatus, location, notes);
            return ResponseEntity.ok(updatedPackage);
        }
    }

    /**
     * Convert UnifiedPackageDTO to DriverPackageInfo for backward compatibility
     */
    private DriverPackageInfo convertToDriverPackageInfo(UnifiedPackageDTO unified) {
        DriverPackageInfo info = new DriverPackageInfo();
        
        info.setId(unified.getId() != null ? unified.getId() : 
            (unified.getShipmentId() != null ? unified.getShipmentId() : unified.getBookingId()));
        info.setTrackingNumber(unified.getTrackingNumber());
        
        // Recipient information
        info.setRecipientName(unified.getRecipientName());
        info.setRecipientPhone(unified.getRecipientPhone());
        
        // Pickup details
        info.setPickupAddress(unified.getPickupAddress());
        info.setPickupCity(unified.getPickupCity());
        info.setPickupState(unified.getPickupState());
        
        // Delivery details
        info.setDeliveryAddress(unified.getDeliveryAddress());
        info.setDeliveryCity(unified.getDeliveryCity());
        info.setDeliveryState(unified.getDeliveryState());
        
        // Package details
        info.setWeight(unified.getWeight());
        info.setDescription(unified.getDescription());
        
        // Status - convert unified status to ShipmentStatus
        if (unified.getShipmentStatus() != null) {
            info.setStatus(unified.getShipmentStatus());
        } else if (unified.getUnifiedStatus() != null) {
            try {
                info.setStatus(com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus.valueOf(unified.getUnifiedStatus()));
            } catch (IllegalArgumentException e) {
                info.setStatus(com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus.PENDING);
            }
        }
        info.setFormattedStatus(unified.getFormattedStatus());
        
        // Dates
        info.setEstimatedDeliveryDate(unified.getEstimatedDeliveryDate());
        info.setActualDeliveryDate(unified.getActualDeliveryDate());
        info.setFormattedEstimatedDelivery(unified.getFormattedEstimatedDelivery());
        
        // Verification codes
        info.setCollectionCode(unified.getCollectionCode());
        info.setDropOffCode(unified.getDropOffCode());
        
        // Route optimization fields (can be set by route optimization service)
        info.setIsCurrentlyCarrying(unified.getIsCurrentlyCarrying());
        info.setDeliveryPriority(unified.getDeliveryPriority());
        info.setDistanceFromCurrentLocation(unified.getDistanceFromCurrentLocation());
        info.setEstimatedTimeMinutes(unified.getEstimatedTimeMinutes());
        info.setSuggestedRoute(unified.getSuggestedRoute());
        
        return info;
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

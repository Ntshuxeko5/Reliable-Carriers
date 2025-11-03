package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageInfo;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.DriverPackageService;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DriverPackageServiceImpl implements DriverPackageService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShipmentService shipmentService;

    @Override
    public List<DriverPackageInfo> getDriverPackagesWithRouteOptimization(Long driverId, Double currentLat, Double currentLng) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Get all active shipments assigned to this driver
        List<Shipment> shipments = shipmentRepository.findByAssignedDriver(driver);
        
        // Filter out delivered and cancelled shipments
        List<Shipment> activeShipments = shipments.stream()
                .filter(shipment -> shipment.getStatus() != ShipmentStatus.DELIVERED 
                    && shipment.getStatus() != ShipmentStatus.CANCELLED)
                .collect(Collectors.toList());

        // Convert to DriverPackageInfo and calculate distances
        List<DriverPackageInfo> packageInfos = activeShipments.stream()
                .map(shipment -> {
                    DriverPackageInfo info = new DriverPackageInfo(shipment);
                    
                    // Determine if package is currently being carried
                    info.setIsCurrentlyCarrying(isPackageCurrentlyCarried(shipment.getStatus()));
                    
                    // Calculate distance from current location
                    if (currentLat != null && currentLng != null) {
                        Double distance = calculateDistanceToPackage(info, currentLat, currentLng);
                        info.setDistanceFromCurrentLocation(distance);
                        info.setEstimatedTimeMinutes(estimateTravelTimeFromDistance(distance));
                    }
                    
                    return info;
                })
                .collect(Collectors.toList());

        // Sort by priority (pickup first, then by distance)
        packageInfos.sort((p1, p2) -> {
            // First priority: packages to pickup
            boolean p1NeedsPickup = p1.getStatus() == ShipmentStatus.PENDING || p1.getStatus() == ShipmentStatus.ASSIGNED;
            boolean p2NeedsPickup = p2.getStatus() == ShipmentStatus.PENDING || p2.getStatus() == ShipmentStatus.ASSIGNED;
            
            if (p1NeedsPickup && !p2NeedsPickup) return -1;
            if (!p1NeedsPickup && p2NeedsPickup) return 1;
            
            // Second priority: distance from current location
            if (p1.getDistanceFromCurrentLocation() != null && p2.getDistanceFromCurrentLocation() != null) {
                return Double.compare(p1.getDistanceFromCurrentLocation(), p2.getDistanceFromCurrentLocation());
            }
            
            return 0;
        });

        // Set delivery priority
        for (int i = 0; i < packageInfos.size(); i++) {
            packageInfos.get(i).setDeliveryPriority(i + 1);
        }

        return packageInfos;
    }

    @Override
    public List<DriverPackageInfo> getCurrentlyCarriedPackages(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<Shipment> shipments = shipmentRepository.findByAssignedDriver(driver);
        
        return shipments.stream()
                .filter(shipment -> isPackageCurrentlyCarried(shipment.getStatus()))
                .map(DriverPackageInfo::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverPackageInfo> getPackagesToPickup(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<Shipment> shipments = shipmentRepository.findByAssignedDriver(driver);
        
        return shipments.stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.PENDING 
                    || shipment.getStatus() == ShipmentStatus.ASSIGNED)
                .map(DriverPackageInfo::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverPackageInfo> getPackagesForDelivery(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<Shipment> shipments = shipmentRepository.findByAssignedDriver(driver);
        
        return shipments.stream()
                .filter(shipment -> isPackageCurrentlyCarried(shipment.getStatus()))
                .map(DriverPackageInfo::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<DriverPackageInfo> getOptimizedDeliveryRoute(Long driverId, Double currentLat, Double currentLng) {
        // Get packages ready for delivery
        List<DriverPackageInfo> deliveryPackages = getPackagesForDelivery(driverId);
        
        if (currentLat == null || currentLng == null) {
            return deliveryPackages;
        }

        // Calculate distances and sort by nearest neighbor algorithm
        for (DriverPackageInfo packageInfo : deliveryPackages) {
            Double distance = calculateDistanceToPackage(packageInfo, currentLat, currentLng);
            packageInfo.setDistanceFromCurrentLocation(distance);
            packageInfo.setEstimatedTimeMinutes(estimateTravelTimeFromDistance(distance));
        }

        // Sort by distance (nearest neighbor)
        deliveryPackages.sort((p1, p2) -> {
            if (p1.getDistanceFromCurrentLocation() != null && p2.getDistanceFromCurrentLocation() != null) {
                return Double.compare(p1.getDistanceFromCurrentLocation(), p2.getDistanceFromCurrentLocation());
            }
            return 0;
        });

        // Set priority
        for (int i = 0; i < deliveryPackages.size(); i++) {
            deliveryPackages.get(i).setDeliveryPriority(i + 1);
        }

        return deliveryPackages;
    }

    @Override
    public Map<String, Object> getDriverPackageStatistics(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<Shipment> shipments = shipmentRepository.findByAssignedDriver(driver);
        
        Map<String, Object> stats = new HashMap<>();
        
        // Total packages
        stats.put("totalPackages", shipments.size());
        
        // Packages by status
        Map<ShipmentStatus, Long> statusCounts = shipments.stream()
                .collect(Collectors.groupingBy(Shipment::getStatus, Collectors.counting()));
        
        stats.put("packagesToPickup", statusCounts.getOrDefault(ShipmentStatus.PENDING, 0L) + 
                                     statusCounts.getOrDefault(ShipmentStatus.ASSIGNED, 0L));
        stats.put("packagesInTransit", statusCounts.getOrDefault(ShipmentStatus.IN_TRANSIT, 0L));
        stats.put("packagesOutForDelivery", statusCounts.getOrDefault(ShipmentStatus.OUT_FOR_DELIVERY, 0L));
        stats.put("packagesDelivered", statusCounts.getOrDefault(ShipmentStatus.DELIVERED, 0L));
        
        // Total weight
        double totalWeight = shipments.stream()
                .mapToDouble(shipment -> shipment.getWeight() != null ? shipment.getWeight() : 0.0)
                .sum();
        stats.put("totalWeight", totalWeight);
        
        // Today's deliveries
        long todaysDeliveries = shipments.stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.DELIVERED 
                    && shipment.getActualDeliveryDate() != null
                    && isToday(shipment.getActualDeliveryDate()))
                .count();
        stats.put("todaysDeliveries", todaysDeliveries);
        
        return stats;
    }

    @Override
    public DriverPackageInfo updatePackageStatus(Long packageId, String newStatus, String location, String notes) {
        // Get shipment to validate it exists
        shipmentService.getShipmentById(packageId);
        
        ShipmentStatus status;
        try {
            status = ShipmentStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }
        
        shipmentService.updateShipmentStatus(packageId, status, location, notes);
        
        return new DriverPackageInfo(shipmentService.getShipmentById(packageId));
    }

    @Override
    public DriverPackageInfo getNextRecommendedPackage(Long driverId, Double currentLat, Double currentLng) {
        List<DriverPackageInfo> packages = getDriverPackagesWithRouteOptimization(driverId, currentLat, currentLng);
        
        if (packages.isEmpty()) {
            return null;
        }
        
        return packages.get(0); // First package after optimization
    }

    @Override
    public Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return null;
        }
        
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    @Override
    public Integer estimateTravelTime(Double lat1, Double lng1, Double lat2, Double lng2) {
        Double distance = calculateDistance(lat1, lng1, lat2, lng2);
        if (distance == null) {
            return null;
        }
        
        // Assume average speed of 30 km/h in urban areas
        double averageSpeedKmh = 30.0;
        double timeHours = distance / averageSpeedKmh;
        
        return (int) Math.round(timeHours * 60); // Convert to minutes
    }

    // Helper method to estimate travel time from distance
    private Integer estimateTravelTimeFromDistance(Double distance) {
        if (distance == null) {
            return null;
        }
        
        // Assume average speed of 30 km/h in urban areas
        double averageSpeedKmh = 30.0;
        double timeHours = distance / averageSpeedKmh;
        
        return (int) Math.round(timeHours * 60); // Convert to minutes
    }

    // Helper methods
    private boolean isPackageCurrentlyCarried(ShipmentStatus status) {
        return status == ShipmentStatus.PICKED_UP || 
               status == ShipmentStatus.IN_TRANSIT || 
               status == ShipmentStatus.OUT_FOR_DELIVERY;
    }

    private Double calculateDistanceToPackage(DriverPackageInfo packageInfo, Double currentLat, Double currentLng) {
        // For packages to pickup, calculate distance to pickup location
        // For packages being carried, calculate distance to delivery location
        if (packageInfo.getStatus() == ShipmentStatus.PENDING || packageInfo.getStatus() == ShipmentStatus.ASSIGNED) {
            // Need to pickup - calculate distance to pickup location
            // For now, we'll use a simple approximation based on city/state
            // In a real implementation, you'd geocode the addresses
            return calculateApproximateDistance(currentLat, currentLng, packageInfo.getPickupCity(), packageInfo.getPickupState());
        } else {
            // Being carried - calculate distance to delivery location
            return calculateApproximateDistance(currentLat, currentLng, packageInfo.getDeliveryCity(), packageInfo.getDeliveryState());
        }
    }

    private Double calculateApproximateDistance(Double currentLat, Double currentLng, String targetCity, String targetState) {
        // This is a simplified distance calculation
        // In a real implementation, you would:
        // 1. Geocode the city/state to get coordinates
        // 2. Use the Haversine formula to calculate actual distance
        
        // For now, return a random distance between 1-50 km for demonstration
        return Math.random() * 50 + 1;
    }

    private boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar checkDate = Calendar.getInstance();
        checkDate.setTime(date);
        
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR);
    }
}

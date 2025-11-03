package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.DriverLocationRepository;
import com.reliablecarriers.Reliable.Carriers.service.AdminDriverTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDriverTrackingServiceImpl implements AdminDriverTrackingService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Override
    public Map<String, Object> getAllDriverLocations() {
        try {
            // Get all drivers
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            
            List<Map<String, Object>> driverList = drivers.stream().map(driver -> {
                // Get latest location
                DriverLocation latestLocation = driverLocationRepository
                    .findTopByDriverIdOrderByTimestampDesc(driver.getId());
                
                // Get active deliveries
                List<Shipment> allDriverShipments = shipmentRepository.findByAssignedDriverId(driver.getId());
                List<Shipment> activeDeliveries = allDriverShipments.stream()
                    .filter(shipment -> shipment.getStatus() == ShipmentStatus.PICKED_UP || 
                                      shipment.getStatus() == ShipmentStatus.IN_TRANSIT)
                    .collect(Collectors.toList());
                
                Map<String, Object> driverMap = new HashMap<>();
                driverMap.put("id", driver.getId());
                driverMap.put("firstName", driver.getFirstName());
                driverMap.put("lastName", driver.getLastName());
                driverMap.put("email", driver.getEmail());
                driverMap.put("phone", driver.getPhone());
                driverMap.put("status", getDriverStatus(driver));
                driverMap.put("latitude", latestLocation != null ? latestLocation.getLatitude() : null);
                driverMap.put("longitude", latestLocation != null ? latestLocation.getLongitude() : null);
                driverMap.put("currentLocation", latestLocation != null ? latestLocation.getAddress() : "Unknown");
                driverMap.put("lastSeen", latestLocation != null ? 
                    latestLocation.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")) : "Unknown");
                driverMap.put("activeDeliveries", activeDeliveries.size());
                driverMap.put("vehicleInfo", "No vehicle info"); // Vehicle info would be stored in a separate Vehicle entity
                return driverMap;
            }).collect(Collectors.toList());
            
            // Calculate statistics
            Map<String, Object> statistics = getDriverStatistics();
            
            return Map.of(
                "drivers", driverList,
                "statistics", statistics
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver locations: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDriverDetails(Long driverId) {
        try {
            User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
            
            // Get latest location
            DriverLocation latestLocation = driverLocationRepository
                .findTopByDriverIdOrderByTimestampDesc(driverId);
            
            // Get active deliveries
            List<Shipment> allDriverShipments = shipmentRepository.findByAssignedDriverId(driverId);
            List<Shipment> activeDeliveries = allDriverShipments.stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.PICKED_UP || 
                                  shipment.getStatus() == ShipmentStatus.IN_TRANSIT)
                .collect(Collectors.toList());
            
            // Get total deliveries
            List<Shipment> totalDeliveries = shipmentRepository
                .findByAssignedDriverId(driverId);
            
            Map<String, Object> driverMap = new HashMap<>();
            driverMap.put("id", driver.getId());
            driverMap.put("firstName", driver.getFirstName());
            driverMap.put("lastName", driver.getLastName());
            driverMap.put("email", driver.getEmail());
            driverMap.put("phone", driver.getPhone());
            driverMap.put("status", getDriverStatus(driver));
            driverMap.put("latitude", latestLocation != null ? latestLocation.getLatitude() : null);
            driverMap.put("longitude", latestLocation != null ? latestLocation.getLongitude() : null);
            driverMap.put("currentLocation", latestLocation != null ? latestLocation.getAddress() : "Unknown");
            driverMap.put("lastSeen", latestLocation != null ? 
                latestLocation.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Unknown");
            driverMap.put("activeDeliveries", activeDeliveries.size());
            driverMap.put("totalDeliveries", totalDeliveries.size());
            driverMap.put("vehicleInfo", "No vehicle info"); // Vehicle info would be stored in a separate Vehicle entity
            return driverMap;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver details: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getDriverLocationHistory(Long driverId, int hours) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
            
            List<DriverLocation> locations = driverLocationRepository
                .findByDriverIdAndTimestampAfterOrderByTimestampDesc(driverId, startTime);
            
            return locations.stream().map(location -> {
                Map<String, Object> locationMap = new HashMap<>();
                locationMap.put("latitude", location.getLatitude());
                locationMap.put("longitude", location.getLongitude());
                locationMap.put("address", location.getAddress());
                locationMap.put("timestamp", location.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                locationMap.put("speed", location.getSpeed() != null ? location.getSpeed() : 0);
                return locationMap;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver location history: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDriverStatistics() {
        try {
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            
            long onlineDrivers = drivers.stream()
                .mapToLong(driver -> {
                    DriverLocation latestLocation = driverLocationRepository
                        .findTopByDriverIdOrderByTimestampDesc(driver.getId());
                    if (latestLocation != null) {
                        return latestLocation.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(5)) ? 1 : 0;
                    }
                    return 0;
                })
                .sum();
            
            // Get all shipments and filter for active ones
            List<Shipment> allShipments = shipmentRepository.findAll();
            List<Shipment> activeDeliveries = allShipments.stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.PICKED_UP || 
                                  shipment.getStatus() == ShipmentStatus.IN_TRANSIT)
                .collect(Collectors.toList());
            
            return Map.of(
                "totalDrivers", drivers.size(),
                "onlineDrivers", onlineDrivers,
                "offlineDrivers", drivers.size() - onlineDrivers,
                "activeDeliveries", activeDeliveries.size(),
                "avgResponseTime", "2.5m" // This would be calculated from actual data
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver statistics: " + e.getMessage());
        }
    }

    @Override
    public void contactDriver(Long driverId, String message) {
        try {
            // This would integrate with notification service
            // For now, just log the action
            System.out.println("Contacting driver " + driverId + " with message: " + message);
            
            // In a real implementation, this would:
            // 1. Send push notification to driver's mobile app
            // 2. Send SMS if push notification fails
            // 3. Log the communication in the system
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to contact driver: " + e.getMessage());
        }
    }

    @Override
    public void assignPackageToDriver(Long driverId, Long packageId) {
        try {
            Shipment shipment = shipmentRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));
            
            User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
            
            shipment.setAssignedDriver(driver);
            shipment.setStatus(ShipmentStatus.ASSIGNED);
            shipment.setUpdatedAt(new Date());
            shipmentRepository.save(shipment);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign package to driver: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDriverPerformance(Long driverId) {
        try {
            List<Shipment> deliveries = shipmentRepository.findByAssignedDriverId(driverId);
            
            long completedDeliveries = deliveries.stream()
                .mapToLong(shipment -> ShipmentStatus.DELIVERED.equals(shipment.getStatus()) ? 1 : 0)
                .sum();
            
            long onTimeDeliveries = deliveries.stream()
                .mapToLong(shipment -> {
                    if (ShipmentStatus.DELIVERED.equals(shipment.getStatus()) && 
                        shipment.getActualDeliveryDate() != null && 
                        shipment.getEstimatedDeliveryDate() != null) {
                        return shipment.getActualDeliveryDate().before(shipment.getEstimatedDeliveryDate()) ? 1 : 0;
                    }
                    return 0;
                })
                .sum();
            
            double onTimeRate = completedDeliveries > 0 ? (double) onTimeDeliveries / completedDeliveries * 100 : 0;
            
            return Map.of(
                "totalDeliveries", deliveries.size(),
                "completedDeliveries", completedDeliveries,
                "onTimeDeliveries", onTimeDeliveries,
                "onTimeRate", Math.round(onTimeRate * 100.0) / 100.0,
                "averageDeliveryTime", "45m", // This would be calculated from actual data
                "rating", "4.8" // This would come from customer feedback
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get driver performance: " + e.getMessage());
        }
    }

    @Override
    public void updateDriverStatus(Long driverId, String status) {
        try {
            // Update driver status (this would be stored in a separate status field)
            // For now, we'll just log the action
            System.out.println("Updating driver " + driverId + " status to: " + status);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update driver status: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getRealTimeDriverLocations() {
        try {
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            
            return drivers.stream().map(driver -> {
                DriverLocation latestLocation = driverLocationRepository
                    .findTopByDriverIdOrderByTimestampDesc(driver.getId());
                
                Map<String, Object> driverMap = new HashMap<>();
                driverMap.put("driverId", driver.getId());
                driverMap.put("driverName", driver.getFirstName() + " " + driver.getLastName());
                driverMap.put("status", getDriverStatus(driver));
                driverMap.put("latitude", latestLocation != null ? latestLocation.getLatitude() : null);
                driverMap.put("longitude", latestLocation != null ? latestLocation.getLongitude() : null);
                driverMap.put("speed", latestLocation != null ? latestLocation.getSpeed() : 0);
                driverMap.put("timestamp", latestLocation != null ? latestLocation.getTimestamp() : LocalDateTime.now());
                return driverMap;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get real-time driver locations: " + e.getMessage());
        }
    }
    
    private String getDriverStatus(User driver) {
        DriverLocation latestLocation = driverLocationRepository
            .findTopByDriverIdOrderByTimestampDesc(driver.getId());
        
        if (latestLocation == null) {
            return "OFFLINE";
        }
        
        // Check if location is recent (within last 5 minutes)
        if (latestLocation.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(5))) {
            // Check if driver has active deliveries
            List<Shipment> allDriverShipments = shipmentRepository.findByAssignedDriverId(driver.getId());
            List<Shipment> activeDeliveries = allDriverShipments.stream()
                .filter(shipment -> shipment.getStatus() == ShipmentStatus.PICKED_UP || 
                                  shipment.getStatus() == ShipmentStatus.IN_TRANSIT)
                .collect(Collectors.toList());
            
            return activeDeliveries.isEmpty() ? "ONLINE" : "BUSY";
        }
        
        return "OFFLINE";
    }
}

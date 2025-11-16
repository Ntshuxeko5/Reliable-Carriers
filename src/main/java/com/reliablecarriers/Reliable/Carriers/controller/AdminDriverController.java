package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.DriverLocationResponse;
import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageAssignmentRequest;
import com.reliablecarriers.Reliable.Carriers.dto.DriverPackageAssignmentResponse;
import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.DriverLocationRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/drivers")
@PreAuthorize("hasAnyRole('ADMIN','TRACKING_MANAGER')")
@CrossOrigin(origins = "*")
public class AdminDriverController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all driver locations
     */
    @GetMapping("/locations")
    public ResponseEntity<List<DriverLocationResponse>> getAllDriverLocations() {
        try {
            // Get all drivers
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            
            List<DriverLocationResponse> driverLocations = new ArrayList<>();
            
            for (User driver : drivers) {
                DriverLocationResponse response = new DriverLocationResponse();
                response.setDriverId(driver.getId());
                response.setDriverName(driver.getFirstName() + " " + driver.getLastName());
                response.setDriverPhone(driver.getPhone());
                response.setDriverEmail(driver.getEmail());
                response.setDriverStatus("ACTIVE"); // Default status since User model doesn't have status field
                
                // Get latest location
                DriverLocation latestLocation = driverLocationRepository.findTopByDriverIdOrderByTimestampDesc(driver.getId());
                if (latestLocation != null) {
                    response.setLatitude(latestLocation.getLatitude());
                    response.setLongitude(latestLocation.getLongitude());
                    response.setAddress(latestLocation.getAddress());
                    response.setLastLocationUpdate(java.sql.Timestamp.valueOf(latestLocation.getTimestamp()));
                }
                
                // Get driver's current package count
                List<Shipment> assignedPackages = shipmentRepository.findByAssignedDriverId(driver.getId());
                response.setAssignedPackageCount(assignedPackages.size());
                
                // Get packages by status
                long packagesToPickup = assignedPackages.stream()
                    .filter(pkg -> pkg.getStatus() == ShipmentStatus.PENDING)
                    .count();
                long packagesInVehicle = assignedPackages.stream()
                    .filter(pkg -> pkg.getStatus() == ShipmentStatus.IN_TRANSIT)
                    .count();
                
                response.setPackagesToPickup((int) packagesToPickup);
                response.setPackagesInVehicle((int) packagesInVehicle);
                
                driverLocations.add(response);
            }
            
            return ResponseEntity.ok(driverLocations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available packages for assignment
     */
    @GetMapping("/available-packages")
    public ResponseEntity<List<Map<String, Object>>> getAvailablePackages() {
        try {
            List<Shipment> availablePackages = shipmentRepository.findByStatusAndAssignedDriverIsNull(ShipmentStatus.PENDING);
            
            List<Map<String, Object>> packages = availablePackages.stream()
                .map(pkg -> {
                    Map<String, Object> packageMap = new HashMap<>();
                    packageMap.put("id", pkg.getId());
                    packageMap.put("trackingNumber", pkg.getTrackingNumber() != null ? pkg.getTrackingNumber() : "N/A");
                    
                    // Handle null sender
                    if (pkg.getSender() != null) {
                        String senderName = (pkg.getSender().getFirstName() != null ? pkg.getSender().getFirstName() : "") + 
                                          " " + (pkg.getSender().getLastName() != null ? pkg.getSender().getLastName() : "");
                        packageMap.put("senderName", senderName.trim());
                    } else {
                        packageMap.put("senderName", "N/A");
                    }
                    
                    packageMap.put("recipientName", pkg.getRecipientName() != null ? pkg.getRecipientName() : "N/A");
                    packageMap.put("pickupAddress", pkg.getPickupAddress() != null ? pkg.getPickupAddress() : "");
                    packageMap.put("deliveryAddress", pkg.getDeliveryAddress() != null ? pkg.getDeliveryAddress() : "");
                    packageMap.put("pickupCity", pkg.getPickupCity() != null ? pkg.getPickupCity() : "");
                    packageMap.put("deliveryCity", pkg.getDeliveryCity() != null ? pkg.getDeliveryCity() : "");
                    packageMap.put("deliveryState", pkg.getDeliveryState() != null ? pkg.getDeliveryState() : "");
                    packageMap.put("weight", pkg.getWeight() != null ? pkg.getWeight() : 0);
                    packageMap.put("dimensions", pkg.getDimensions() != null ? pkg.getDimensions() : "");
                    packageMap.put("createdAt", pkg.getCreatedAt() != null ? pkg.getCreatedAt() : new Date());
                    packageMap.put("status", pkg.getStatus() != null ? pkg.getStatus().toString() : "PENDING");
                    return packageMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Assign package to driver
     */
    @PostMapping("/assign-package")
    public ResponseEntity<DriverPackageAssignmentResponse> assignPackageToDriver(@RequestBody DriverPackageAssignmentRequest request) {
        try {
            // Validate driver exists
            Optional<User> driverOpt = userRepository.findById(request.getDriverId());
            if (!driverOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Driver not found"));
            }

            // Ensure the selected user is actually a driver
            if (driverOpt.get().getRole() != UserRole.DRIVER) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Selected user is not a driver"));
            }
            
            // Validate package exists and is available
            Optional<Shipment> packageOpt = shipmentRepository.findById(request.getPackageId());
            if (!packageOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Package not found"));
            }
            
            Shipment shipment = packageOpt.get();
            if (shipment.getAssignedDriver() != null) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Package already assigned"));
            }
            
            if (shipment.getStatus() != ShipmentStatus.PENDING) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Package not available for pickup"));
            }
            
            // Assign package to driver
            shipment.setAssignedDriver(driverOpt.get());
            shipment.setStatus(ShipmentStatus.ASSIGNED);
            shipment.setUpdatedAt(new Date());
            shipmentRepository.save(shipment);
            
            // Send notification to driver
            User driver = driverOpt.get();
            String message = String.format("You have been assigned package %s for pickup. Please check your workboard.", shipment.getTrackingNumber());
            notificationService.sendCustomSmsNotification(driver.getPhone(), message);
            
            return ResponseEntity.ok(new DriverPackageAssignmentResponse(true, "Package assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new DriverPackageAssignmentResponse(false, "Error assigning package"));
        }
    }

    /**
     * Bulk assign packages to driver
     */
    @Transactional
    @PostMapping("/bulk-assign-packages")
    public ResponseEntity<DriverPackageAssignmentResponse> bulkAssignPackages(@RequestBody Map<String, Object> request) {
        try {
            // Debug logging
            System.out.println("Received request: " + request);
            System.out.println("DriverId: " + request.get("driverId"));
            System.out.println("PackageIds: " + request.get("packageIds"));
            
            Long driverId = Long.valueOf(request.get("driverId").toString());
            @SuppressWarnings("unchecked")
            List<Object> packageIdsRaw = (List<Object>) request.get("packageIds");
            
            // Convert package IDs to Long, handling both Integer and Long types
            List<Long> packageIds = packageIdsRaw.stream()
                .map(id -> {
                    if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    } else if (id instanceof Long) {
                        return (Long) id;
                    } else {
                        return Long.valueOf(id.toString());
                    }
                })
                .collect(Collectors.toList());
            
            // Validate driver exists
            Optional<User> driverOpt = userRepository.findById(driverId);
            if (!driverOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Driver not found"));
            }
            
            User driver = driverOpt.get();

            // Ensure the selected user is actually a driver
            if (driver.getRole() != UserRole.DRIVER) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Selected user is not a driver"));
            }
            
            int assignedCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (Long packageId : packageIds) {
                try {
                    Optional<Shipment> packageOpt = shipmentRepository.findById(packageId);
                    if (packageOpt.isPresent()) {
                        Shipment shipment = packageOpt.get();
                        if (shipment.getAssignedDriver() == null && shipment.getStatus() == ShipmentStatus.PENDING) {
                            shipment.setAssignedDriver(driver);
                            shipment.setStatus(ShipmentStatus.ASSIGNED);
                            shipment.setUpdatedAt(new Date());
                            shipmentRepository.save(shipment);
                            assignedCount++;
                        } else {
                            errors.add("Package " + shipment.getTrackingNumber() + " not available");
                        }
                    } else {
                        errors.add("Package ID " + packageId + " not found");
                    }
                } catch (Exception e) {
                    errors.add("Error assigning package ID " + packageId);
                }
            }
            
            // Send notification to driver
            if (assignedCount > 0) {
                String message = String.format("You have been assigned %d new packages. Please check your workboard.", assignedCount);
                notificationService.sendCustomSmsNotification(driver.getPhone(), message);
            }
            
            String resultMessage = String.format("Assigned %d packages successfully", assignedCount);
            if (!errors.isEmpty()) {
                resultMessage += ". Errors: " + String.join(", ", errors);
            }
            
            return ResponseEntity.ok(new DriverPackageAssignmentResponse(true, resultMessage));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new DriverPackageAssignmentResponse(false, "Error bulk assigning packages: " + e.getMessage()));
        }
    }

    /**
     * Get driver's assigned packages
     */
    @GetMapping("/{driverId}/packages")
    public ResponseEntity<List<Map<String, Object>>> getDriverPackages(@PathVariable Long driverId) {
        try {
            List<Shipment> packages = shipmentRepository.findByAssignedDriverId(driverId);
            
            List<Map<String, Object>> packageList = packages.stream()
                .map(pkg -> {
                    Map<String, Object> packageMap = new HashMap<>();
                    packageMap.put("id", pkg.getId());
                    packageMap.put("trackingNumber", pkg.getTrackingNumber());
                    packageMap.put("senderName", pkg.getSender().getFirstName() + " " + pkg.getSender().getLastName());
                    packageMap.put("recipientName", pkg.getRecipientName());
                    packageMap.put("pickupAddress", pkg.getPickupAddress());
                    packageMap.put("deliveryAddress", pkg.getDeliveryAddress());
                    packageMap.put("status", pkg.getStatus());
                    packageMap.put("weight", pkg.getWeight());
                    packageMap.put("createdAt", pkg.getCreatedAt());
                    return packageMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(packageList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Unassign package from driver
     */
    @PostMapping("/unassign-package/{packageId}")
    public ResponseEntity<DriverPackageAssignmentResponse> unassignPackage(@PathVariable Long packageId) {
        try {
            Optional<Shipment> packageOpt = shipmentRepository.findById(packageId);
            if (!packageOpt.isPresent()) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Package not found"));
            }
            
            Shipment shipment = packageOpt.get();
            if (shipment.getAssignedDriver() == null) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Package not assigned to any driver"));
            }
            
            // Only allow unassigning if package hasn't been picked up yet
            if (shipment.getStatus() == ShipmentStatus.IN_TRANSIT || shipment.getStatus() == ShipmentStatus.DELIVERED) {
                return ResponseEntity.badRequest().body(new DriverPackageAssignmentResponse(false, "Cannot unassign package that has been picked up"));
            }
            
            User driver = shipment.getAssignedDriver();
            shipment.setAssignedDriver(null);
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setUpdatedAt(new Date());
            shipmentRepository.save(shipment);
            
            // Send notification to driver
            String message = String.format("Package %s has been unassigned from you.", shipment.getTrackingNumber());
            notificationService.sendCustomSmsNotification(driver.getPhone(), message);
            
            return ResponseEntity.ok(new DriverPackageAssignmentResponse(true, "Package unassigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new DriverPackageAssignmentResponse(false, "Error unassigning package"));
        }
    }

    /**
     * Get driver statistics
     */
    @GetMapping("/{driverId}/stats")
    public ResponseEntity<Map<String, Object>> getDriverStats(@PathVariable Long driverId) {
        try {
            Optional<User> driverOpt = userRepository.findById(driverId);
            if (!driverOpt.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<Shipment> allPackages = shipmentRepository.findByAssignedDriverId(driverId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPackages", allPackages.size());
            stats.put("packagesToPickup", allPackages.stream().filter(pkg -> pkg.getStatus() == ShipmentStatus.ASSIGNED).count());
            stats.put("packagesInVehicle", allPackages.stream().filter(pkg -> pkg.getStatus() == ShipmentStatus.IN_TRANSIT).count());
            stats.put("packagesDelivered", allPackages.stream().filter(pkg -> pkg.getStatus() == ShipmentStatus.DELIVERED).count());
            
            // Get today's packages
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date today = cal.getTime();
            
            long todayPackages = allPackages.stream()
                .filter(pkg -> pkg.getUpdatedAt() != null && pkg.getUpdatedAt().after(today))
                .count();
            stats.put("todayPackages", todayPackages);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

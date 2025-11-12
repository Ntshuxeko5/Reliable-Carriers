package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.*;
import com.reliablecarriers.Reliable.Carriers.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin Package Management Controller
 * Handles package assignment and management by admins and tracking managers
 */
@RestController
@RequestMapping("/api/admin/packages")
@PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
public class AdminPackageController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // Note: shipmentRepository kept for potential future use
    // @Autowired
    // private ShipmentRepository shipmentRepository;

    @Autowired
    private ComprehensiveNotificationService notificationService;

    @Autowired
    private RealtimeTrackingService realtimeTrackingService;

    @Autowired
    private com.reliablecarriers.Reliable.Carriers.service.UnifiedPackageService unifiedPackageService;

    /**
     * Get all packages pending assignment
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingPackages() {
        try {
            List<Booking> pendingBookings = bookingRepository.findByStatusOrderByCreatedAtDesc(BookingStatus.CONFIRMED);
            List<Map<String, Object>> packages = new ArrayList<>();

            for (Booking booking : pendingBookings) {
                Map<String, Object> packageInfo = new HashMap<>();
                packageInfo.put("id", booking.getId());
                packageInfo.put("bookingNumber", booking.getBookingNumber());
                packageInfo.put("trackingNumber", booking.getTrackingNumber());
                packageInfo.put("customerName", booking.getCustomerName());
                packageInfo.put("customerEmail", booking.getCustomerEmail());
                packageInfo.put("customerPhone", booking.getCustomerPhone());
                packageInfo.put("status", booking.getStatus() != null ? booking.getStatus().name() : "PENDING");
                packageInfo.put("serviceType", booking.getServiceType().getDisplayName());
                packageInfo.put("description", booking.getDescription());
                packageInfo.put("weight", booking.getWeight());
                packageInfo.put("dimensions", booking.getDimensions());
                packageInfo.put("totalAmount", booking.getTotalAmount());
                packageInfo.put("createdAt", booking.getCreatedAt());
                
                // Pickup details
                Map<String, Object> pickup = new HashMap<>();
                pickup.put("address", booking.getPickupAddress());
                pickup.put("city", booking.getPickupCity());
                pickup.put("state", booking.getPickupState());
                pickup.put("contact", booking.getPickupContactName());
                pickup.put("phone", booking.getPickupContactPhone());
                pickup.put("code", booking.getCustomerPickupCode());
                packageInfo.put("pickup", pickup);
                
                // Delivery details
                Map<String, Object> delivery = new HashMap<>();
                delivery.put("address", booking.getDeliveryAddress());
                delivery.put("city", booking.getDeliveryCity());
                delivery.put("state", booking.getDeliveryState());
                delivery.put("contact", booking.getDeliveryContactName());
                delivery.put("phone", booking.getDeliveryContactPhone());
                delivery.put("code", booking.getCustomerDeliveryCode());
                packageInfo.put("delivery", delivery);
                
                packages.add(packageInfo);
            }

            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all available drivers
     */
    @GetMapping("/drivers/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableDrivers() {
        try {
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            List<Map<String, Object>> availableDrivers = new ArrayList<>();

            for (User driver : drivers) {
                // Count active assignments
                long activeAssignments = bookingRepository.countByDriverAndStatusIn(
                    driver, 
                    Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.PICKED_UP, BookingStatus.IN_TRANSIT, BookingStatus.OUT_FOR_DELIVERY)
                );

                Map<String, Object> driverInfo = new HashMap<>();
                driverInfo.put("id", driver.getId());
                driverInfo.put("name", driver.getFirstName() + " " + driver.getLastName());
                driverInfo.put("email", driver.getEmail());
                driverInfo.put("phone", driver.getPhone());
                driverInfo.put("city", driver.getCity());
                driverInfo.put("activeAssignments", activeAssignments);
                driverInfo.put("available", activeAssignments < 5); // Max 5 active assignments
                
                availableDrivers.add(driverInfo);
            }

            // Sort by availability and active assignments
            availableDrivers.sort((a, b) -> {
                boolean aAvailable = (Boolean) a.get("available");
                boolean bAvailable = (Boolean) b.get("available");
                if (aAvailable != bAvailable) {
                    return aAvailable ? -1 : 1;
                }
                return Long.compare((Long) a.get("activeAssignments"), (Long) b.get("activeAssignments"));
            });

            return ResponseEntity.ok(availableDrivers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Assign package to driver
     */
    @PostMapping("/{bookingId}/assign/{driverId}")
    public ResponseEntity<Map<String, Object>> assignPackageToDriver(
            @PathVariable Long bookingId, 
            @PathVariable Long driverId) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find booking
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Find driver
            User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            if (driver.getRole() != UserRole.DRIVER) {
                result.put("success", false);
                result.put("message", "Selected user is not a driver");
                return ResponseEntity.badRequest().body(result);
            }

            // Check if already assigned
            if (booking.getDriver() != null) {
                result.put("success", false);
                result.put("message", "Package already assigned to another driver");
                return ResponseEntity.badRequest().body(result);
            }

            // Use unified service to assign package (synchronizes Booking and Shipment)
            if (booking.getTrackingNumber() != null) {
                UnifiedPackageDTO updatedPackage = unifiedPackageService.assignPackageToDriver(
                    booking.getTrackingNumber(), driverId);
                
                // Send notifications
                notificationService.sendDriverAssignmentNotification(booking, driver);

                // Send real-time updates
                if (updatedPackage.getShipmentStatus() != null) {
                    realtimeTrackingService.sendPackageStatusUpdate(
                        booking.getTrackingNumber(), 
                        updatedPackage.getShipmentStatus(), 
                        "Package assigned to driver", 
                        "Package assigned to " + driver.getFirstName() + " " + driver.getLastName()
                    );
                }

                result.put("success", true);
                result.put("message", "Package assigned successfully");
                result.put("driverName", updatedPackage.getDriverName());
                result.put("driverPhone", updatedPackage.getDriverPhone());
                result.put("status", updatedPackage.getUnifiedStatus());
            } else {
                // Fallback for bookings without tracking numbers
                booking.setDriver(driver);
                booking.setStatus(BookingStatus.ASSIGNED);
                booking.setUpdatedAt(new Date());
                bookingRepository.save(booking);
                
                notificationService.sendDriverAssignmentNotification(booking, driver);
                
                result.put("success", true);
                result.put("message", "Package assigned successfully");
                result.put("driverName", driver.getFirstName() + " " + driver.getLastName());
                result.put("driverPhone", driver.getPhone());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to assign package: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get all packages (with filters) - Uses UnifiedPackageService for seamless integration
     */
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllPackages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            List<com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO> unifiedPackages;
            
            // Use unified service to get packages
            if (status != null && driverId != null) {
                // Filter by both status and driver
                unifiedPackages = unifiedPackageService.getPackagesByDriverId(driverId);
                unifiedPackages = unifiedPackages.stream()
                    .filter(p -> p.getUnifiedStatus() != null && 
                        p.getUnifiedStatus().equalsIgnoreCase(status))
                    .collect(java.util.stream.Collectors.toList());
            } else if (status != null) {
                unifiedPackages = unifiedPackageService.getPackagesByStatus(status);
            } else if (driverId != null) {
                unifiedPackages = unifiedPackageService.getPackagesByDriverId(driverId);
            } else {
                unifiedPackages = unifiedPackageService.getAllPackages();
            }

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, unifiedPackages.size());
            if (start >= unifiedPackages.size()) {
                unifiedPackages = new ArrayList<>();
            } else {
                unifiedPackages = unifiedPackages.subList(start, end);
            }

            // Convert UnifiedPackageDTO to Map for backward compatibility
            List<Map<String, Object>> packages = new ArrayList<>();
            for (com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO unifiedPackage : unifiedPackages) {
                Map<String, Object> packageInfo = new HashMap<>();
                packageInfo.put("id", unifiedPackage.getId());
                packageInfo.put("bookingId", unifiedPackage.getBookingId());
                packageInfo.put("shipmentId", unifiedPackage.getShipmentId());
                packageInfo.put("bookingNumber", unifiedPackage.getBookingNumber());
                packageInfo.put("trackingNumber", unifiedPackage.getTrackingNumber());
                packageInfo.put("status", unifiedPackage.getUnifiedStatus() != null ? unifiedPackage.getUnifiedStatus() : "PENDING");
                packageInfo.put("formattedStatus", unifiedPackage.getFormattedStatus());
                packageInfo.put("customerName", unifiedPackage.getCustomerName() != null ? unifiedPackage.getCustomerName() : unifiedPackage.getSenderName());
                packageInfo.put("recipientName", unifiedPackage.getRecipientName());
                packageInfo.put("serviceType", unifiedPackage.getServiceType());
                packageInfo.put("totalAmount", unifiedPackage.getTotalAmount() != null ? unifiedPackage.getTotalAmount() : unifiedPackage.getShippingCost());
                packageInfo.put("shippingCost", unifiedPackage.getShippingCost());
                packageInfo.put("createdAt", unifiedPackage.getCreatedAt());
                packageInfo.put("estimatedDeliveryDate", unifiedPackage.getEstimatedDeliveryDate());
                packageInfo.put("pickupAddress", unifiedPackage.getPickupAddress());
                packageInfo.put("pickupCity", unifiedPackage.getPickupCity());
                packageInfo.put("pickupState", unifiedPackage.getPickupState());
                packageInfo.put("deliveryAddress", unifiedPackage.getDeliveryAddress());
                packageInfo.put("deliveryCity", unifiedPackage.getDeliveryCity());
                packageInfo.put("deliveryState", unifiedPackage.getDeliveryState());
                
                // Driver information
                if (unifiedPackage.getDriverId() != null) {
                    Map<String, Object> driverInfo = new HashMap<>();
                    driverInfo.put("id", unifiedPackage.getDriverId());
                    driverInfo.put("name", unifiedPackage.getDriverName());
                    driverInfo.put("phone", unifiedPackage.getDriverPhone());
                    driverInfo.put("email", unifiedPackage.getDriverEmail());
                    packageInfo.put("driver", driverInfo);
                    packageInfo.put("assignedDriverName", unifiedPackage.getDriverName());
                    packageInfo.put("assignedDriverPhone", unifiedPackage.getDriverPhone());
                } else {
                    packageInfo.put("assignedDriverName", null);
                    packageInfo.put("assignedDriverPhone", null);
                }
                
                packages.add(packageInfo);
            }

            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update package status manually (admin override) - Uses UnifiedPackageService for synchronization
     */
    @PutMapping("/{bookingId}/status")
    public ResponseEntity<Map<String, Object>> updatePackageStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            String newStatus = request.get("status");
            String notes = request.get("notes");

            // Use unified service to update status (synchronizes Booking and Shipment)
            if (booking.getTrackingNumber() != null) {
                UnifiedPackageDTO updatedPackage = unifiedPackageService.updatePackageStatus(
                    booking.getTrackingNumber(), newStatus);
                
                // Send real-time updates
                if (updatedPackage.getShipmentStatus() != null) {
                    realtimeTrackingService.sendPackageStatusUpdate(
                        booking.getTrackingNumber(), 
                        updatedPackage.getShipmentStatus(), 
                        "Status updated by admin", 
                        notes != null ? notes : "Status manually updated"
                    );
                }
                
                result.put("success", true);
                result.put("message", "Status updated successfully");
                result.put("newStatus", updatedPackage.getUnifiedStatus());
                result.put("formattedStatus", updatedPackage.getFormattedStatus());
            } else {
                // Fallback for bookings without tracking numbers
                BookingStatus bookingStatus = BookingStatus.valueOf(newStatus.toUpperCase());
                booking.setStatus(bookingStatus);
                booking.setUpdatedAt(new Date());
                bookingRepository.save(booking);
                
                result.put("success", true);
                result.put("message", "Status updated successfully");
                result.put("newStatus", bookingStatus.toString());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to update status: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get package statistics for dashboard - Uses UnifiedPackageService
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPackageStatistics() {
        try {
            UnifiedPackageService.PackageStatistics unifiedStats = unifiedPackageService.getPackageStatistics();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", unifiedStats.getTotalPackages());
            stats.put("pending", unifiedStats.getPendingPackages());
            stats.put("assigned", unifiedStats.getAssignedPackages());
            stats.put("inTransit", unifiedStats.getInTransitPackages());
            stats.put("delivered", unifiedStats.getDeliveredPackages());
            stats.put("cancelled", unifiedStats.getCancelledPackages());
            
            // Additional stats for backward compatibility
            stats.put("totalPackages", unifiedStats.getTotalPackages());
            stats.put("pendingPackages", unifiedStats.getPendingPackages());
            stats.put("inTransitPackages", unifiedStats.getInTransitPackages());
            stats.put("deliveredPackages", unifiedStats.getDeliveredPackages());
            
            // Active deliveries
            long activeDeliveries = bookingRepository.countByStatusIn(
                Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.PICKED_UP, BookingStatus.IN_TRANSIT, BookingStatus.OUT_FOR_DELIVERY)
            );
            stats.put("activeDeliveries", activeDeliveries);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper method to map booking status to shipment status
    // Kept for potential future use when converting bookings to shipments
    @SuppressWarnings("unused")
    private ShipmentStatus mapBookingToShipmentStatus(BookingStatus bookingStatus) {
        switch (bookingStatus) {
            case CONFIRMED:
            case PENDING:
                return ShipmentStatus.PENDING;
            case ASSIGNED:
                return ShipmentStatus.ASSIGNED;
            case PICKED_UP:
                return ShipmentStatus.PICKED_UP;
            case IN_TRANSIT:
                return ShipmentStatus.IN_TRANSIT;
            case OUT_FOR_DELIVERY:
                return ShipmentStatus.OUT_FOR_DELIVERY;
            case DELIVERED:
                return ShipmentStatus.DELIVERED;
            case CANCELLED:
                return ShipmentStatus.CANCELLED;
            default:
                return ShipmentStatus.PENDING;
        }
    }
}

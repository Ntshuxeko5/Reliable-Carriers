package com.reliablecarriers.Reliable.Carriers.controller;

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

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ComprehensiveNotificationService notificationService;

    @Autowired
    private RealtimeTrackingService realtimeTrackingService;

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

            // Assign driver
            booking.setDriver(driver);
            booking.setStatus(BookingStatus.ASSIGNED);
            booking.setUpdatedAt(new Date());
            bookingRepository.save(booking);

            // Create or update shipment
            Shipment shipment = shipmentRepository.findByTrackingNumber(booking.getTrackingNumber())
                .orElse(new Shipment());
            
            if (shipment.getId() == null) {
                shipment.setTrackingNumber(booking.getTrackingNumber());
                shipment.setDescription(booking.getDescription());
                shipment.setPickupAddress(booking.getPickupAddress() + ", " + booking.getPickupCity());
                shipment.setDeliveryAddress(booking.getDeliveryAddress() + ", " + booking.getDeliveryCity());
                shipment.setShippingCost(booking.getTotalAmount());
                shipment.setServiceType(ServiceType.valueOf(booking.getServiceType().name()));
                shipment.setCreatedAt(new Date());
            }
            
            shipment.setAssignedDriver(driver);
            shipment.setStatus(ShipmentStatus.ASSIGNED);
            shipmentRepository.save(shipment);

            // Send notifications
            notificationService.sendDriverAssignmentNotification(booking, driver);

            // Send real-time updates
            realtimeTrackingService.sendPackageStatusUpdate(
                booking.getTrackingNumber(), 
                ShipmentStatus.ASSIGNED, 
                "Package assigned to driver", 
                "Package assigned to " + driver.getFirstName() + " " + driver.getLastName()
            );

            result.put("success", true);
            result.put("message", "Package assigned successfully");
            result.put("driverName", driver.getFirstName() + " " + driver.getLastName());
            result.put("driverPhone", driver.getPhone());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to assign package: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get all packages (with filters)
     */
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllPackages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            List<Booking> bookings;
            
            if (status != null && driverId != null) {
                User driver = userRepository.findById(driverId).orElse(null);
                bookings = bookingRepository.findByStatusAndDriverOrderByCreatedAtDesc(
                    BookingStatus.valueOf(status.toUpperCase()), driver);
            } else if (status != null) {
                bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(
                    BookingStatus.valueOf(status.toUpperCase()));
            } else if (driverId != null) {
                User driver = userRepository.findById(driverId).orElse(null);
                bookings = bookingRepository.findByDriverOrderByCreatedAtDesc(driver);
            } else {
                bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
            }

            // Apply pagination manually (in a real app, use Pageable)
            int start = page * size;
            int end = Math.min(start + size, bookings.size());
            if (start >= bookings.size()) {
                bookings = new ArrayList<>();
            } else {
                bookings = bookings.subList(start, end);
            }

            List<Map<String, Object>> packages = new ArrayList<>();
            for (Booking booking : bookings) {
                Map<String, Object> packageInfo = new HashMap<>();
                packageInfo.put("id", booking.getId());
                packageInfo.put("bookingNumber", booking.getBookingNumber());
                packageInfo.put("trackingNumber", booking.getTrackingNumber());
                packageInfo.put("status", booking.getStatus());
                packageInfo.put("customerName", booking.getCustomerName());
                packageInfo.put("serviceType", booking.getServiceType().getDisplayName());
                packageInfo.put("totalAmount", booking.getTotalAmount());
                packageInfo.put("createdAt", booking.getCreatedAt());
                packageInfo.put("updatedAt", booking.getUpdatedAt());
                
                if (booking.getDriver() != null) {
                    Map<String, Object> driverInfo = new HashMap<>();
                    driverInfo.put("id", booking.getDriver().getId());
                    driverInfo.put("name", booking.getDriver().getFirstName() + " " + booking.getDriver().getLastName());
                    driverInfo.put("phone", booking.getDriver().getPhone());
                    packageInfo.put("driver", driverInfo);
                }
                
                packages.add(packageInfo);
            }

            return ResponseEntity.ok(packages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update package status manually (admin override)
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

            BookingStatus bookingStatus = BookingStatus.valueOf(newStatus.toUpperCase());
            booking.setStatus(bookingStatus);
            booking.setUpdatedAt(new Date());
            bookingRepository.save(booking);

            // Update shipment if exists
            shipmentRepository.findByTrackingNumber(booking.getTrackingNumber())
                .ifPresent(shipment -> {
                    ShipmentStatus shipmentStatus = mapBookingToShipmentStatus(bookingStatus);
                    shipment.setStatus(shipmentStatus);
                    shipmentRepository.save(shipment);
                    
                    // Send real-time updates
                    realtimeTrackingService.sendPackageStatusUpdate(
                        booking.getTrackingNumber(), 
                        shipmentStatus, 
                        "Status updated by admin", 
                        notes != null ? notes : "Status manually updated"
                    );
                });

            result.put("success", true);
            result.put("message", "Status updated successfully");
            result.put("newStatus", bookingStatus);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to update status: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get package statistics for dashboard
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPackageStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Count by status
            for (BookingStatus status : BookingStatus.values()) {
                long count = bookingRepository.countByStatus(status);
                stats.put(status.name().toLowerCase() + "Count", count);
            }
            
            // Total packages
            long totalPackages = bookingRepository.count();
            stats.put("totalPackages", totalPackages);
            
            // Packages needing assignment
            long needingAssignment = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
            stats.put("needingAssignment", needingAssignment);
            
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

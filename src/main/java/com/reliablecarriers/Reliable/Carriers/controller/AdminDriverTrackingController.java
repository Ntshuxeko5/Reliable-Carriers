package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.BookingStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Driver Tracking Controller
 * Provides real-time driver location and status tracking for admins and tracking managers
 */
@RestController
@RequestMapping("/api/admin/drivers/tracking")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
public class AdminDriverTrackingController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Get all online drivers with their current locations
     * GET /api/admin/drivers/tracking/online
     */
    @GetMapping("/online")
    public ResponseEntity<Map<String, Object>> getOnlineDrivers() {
        try {
            List<User> onlineDrivers = userRepository.findByRoleAndIsOnline(UserRole.DRIVER, true);
            
            List<Map<String, Object>> driversData = onlineDrivers.stream()
                .map(driver -> {
                    Map<String, Object> driverData = new HashMap<>();
                    driverData.put("driverId", driver.getId());
                    driverData.put("firstName", driver.getFirstName());
                    driverData.put("lastName", driver.getLastName());
                    driverData.put("email", driver.getEmail());
                    driverData.put("phone", driver.getPhone());
                    driverData.put("latitude", driver.getCurrentLatitude());
                    driverData.put("longitude", driver.getCurrentLongitude());
                    driverData.put("lastLocationUpdate", driver.getLastLocationUpdate());
                    driverData.put("driverRating", driver.getDriverRating());
                    driverData.put("totalDeliveries", driver.getTotalDeliveries());
                    driverData.put("verificationStatus", driver.getDriverVerificationStatus());
                    driverData.put("vehicleMake", driver.getVehicleMake());
                    driverData.put("vehicleModel", driver.getVehicleModel());
                    driverData.put("vehicleRegistration", driver.getVehicleRegistration());
                    
                    // Get driver's current bookings
                    List<Booking> activeBookings = bookingRepository.findByDriverIdAndStatusIn(
                        driver.getId(),
                        Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.IN_TRANSIT, BookingStatus.OUT_FOR_DELIVERY)
                    );
                    
                    driverData.put("activeBookingsCount", activeBookings.size());
                    driverData.put("status", getDriverStatus(driver, activeBookings));
                    
                    // Add booking details if any
                    if (!activeBookings.isEmpty()) {
                        List<Map<String, Object>> bookingsData = activeBookings.stream()
                            .map(booking -> {
                                Map<String, Object> bookingData = new HashMap<>();
                                bookingData.put("bookingId", booking.getId());
                                bookingData.put("bookingNumber", booking.getBookingNumber());
                                bookingData.put("trackingNumber", booking.getTrackingNumber());
                                bookingData.put("pickupAddress", booking.getPickupAddress());
                                bookingData.put("deliveryAddress", booking.getDeliveryAddress());
                                bookingData.put("pickupLatitude", booking.getPickupLatitude());
                                bookingData.put("pickupLongitude", booking.getPickupLongitude());
                                bookingData.put("deliveryLatitude", booking.getDeliveryLatitude());
                                bookingData.put("deliveryLongitude", booking.getDeliveryLongitude());
                                bookingData.put("status", booking.getStatus().toString());
                                return bookingData;
                            })
                            .collect(Collectors.toList());
                        driverData.put("activeBookings", bookingsData);
                    }
                    
                    return driverData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", driversData,
                "count", driversData.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get all drivers (online and offline) with their latest locations
     * GET /api/admin/drivers/tracking/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllDrivers() {
        try {
            List<User> allDrivers = userRepository.findByRole(UserRole.DRIVER);
            
            List<Map<String, Object>> driversData = allDrivers.stream()
                .map(driver -> {
                    Map<String, Object> driverData = new HashMap<>();
                    driverData.put("driverId", driver.getId());
                    driverData.put("firstName", driver.getFirstName());
                    driverData.put("lastName", driver.getLastName());
                    driverData.put("email", driver.getEmail());
                    driverData.put("phone", driver.getPhone());
                    driverData.put("isOnline", driver.getIsOnline() != null && driver.getIsOnline());
                    driverData.put("latitude", driver.getCurrentLatitude());
                    driverData.put("longitude", driver.getCurrentLongitude());
                    driverData.put("lastLocationUpdate", driver.getLastLocationUpdate());
                    driverData.put("driverRating", driver.getDriverRating());
                    driverData.put("totalDeliveries", driver.getTotalDeliveries());
                    driverData.put("totalEarnings", driver.getTotalEarnings());
                    driverData.put("verificationStatus", driver.getDriverVerificationStatus());
                    driverData.put("vehicleMake", driver.getVehicleMake());
                    driverData.put("vehicleModel", driver.getVehicleModel());
                    driverData.put("vehicleRegistration", driver.getVehicleRegistration());
                    
                    // Get driver's current bookings
                    List<Booking> activeBookings = bookingRepository.findByDriverIdAndStatusIn(
                        driver.getId(),
                        Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.IN_TRANSIT, BookingStatus.OUT_FOR_DELIVERY)
                    );
                    
                    driverData.put("activeBookingsCount", activeBookings.size());
                    driverData.put("status", getDriverStatus(driver, activeBookings));
                    
                    return driverData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", driversData,
                "count", driversData.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get specific driver location and status
     * GET /api/admin/drivers/tracking/{driverId}
     */
    @GetMapping("/{driverId}")
    public ResponseEntity<Map<String, Object>> getDriverLocation(@PathVariable Long driverId) {
        try {
            User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
            
            if (driver.getRole() != UserRole.DRIVER) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User is not a driver"
                ));
            }
            
            // Get active bookings
            List<Booking> activeBookings = bookingRepository.findByDriverIdAndStatusIn(
                driver.getId(),
                Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.IN_TRANSIT, BookingStatus.OUT_FOR_DELIVERY)
            );
            
            Map<String, Object> driverData = new HashMap<>();
            driverData.put("driverId", driver.getId());
            driverData.put("firstName", driver.getFirstName());
            driverData.put("lastName", driver.getLastName());
            driverData.put("email", driver.getEmail());
            driverData.put("phone", driver.getPhone());
            driverData.put("isOnline", driver.getIsOnline() != null && driver.getIsOnline());
            driverData.put("latitude", driver.getCurrentLatitude());
            driverData.put("longitude", driver.getCurrentLongitude());
            driverData.put("lastLocationUpdate", driver.getLastLocationUpdate());
            driverData.put("driverRating", driver.getDriverRating());
            driverData.put("totalDeliveries", driver.getTotalDeliveries());
            driverData.put("totalEarnings", driver.getTotalEarnings());
            driverData.put("verificationStatus", driver.getDriverVerificationStatus());
            driverData.put("vehicleMake", driver.getVehicleMake());
            driverData.put("vehicleModel", driver.getVehicleModel());
            driverData.put("vehicleRegistration", driver.getVehicleRegistration());
            driverData.put("vehicleColor", driver.getVehicleColor());
            driverData.put("activeBookingsCount", activeBookings.size());
            driverData.put("status", getDriverStatus(driver, activeBookings));
            
            if (!activeBookings.isEmpty()) {
                List<Map<String, Object>> bookingsData = activeBookings.stream()
                    .map(booking -> {
                        Map<String, Object> bookingData = new HashMap<>();
                        bookingData.put("bookingId", booking.getId());
                        bookingData.put("bookingNumber", booking.getBookingNumber());
                        bookingData.put("trackingNumber", booking.getTrackingNumber());
                        bookingData.put("pickupAddress", booking.getPickupAddress());
                        bookingData.put("deliveryAddress", booking.getDeliveryAddress());
                        bookingData.put("pickupLatitude", booking.getPickupLatitude());
                        bookingData.put("pickupLongitude", booking.getPickupLongitude());
                        bookingData.put("deliveryLatitude", booking.getDeliveryLatitude());
                        bookingData.put("deliveryLongitude", booking.getDeliveryLongitude());
                        bookingData.put("status", booking.getStatus().toString());
                        bookingData.put("customerName", booking.getCustomerName());
                        bookingData.put("customerPhone", booking.getCustomerPhone());
                        return bookingData;
                    })
                    .collect(Collectors.toList());
                driverData.put("activeBookings", bookingsData);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", driverData
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get driver location history (optional future enhancement)
     * GET /api/admin/drivers/tracking/{driverId}/history
     */
    @GetMapping("/{driverId}/history")
    public ResponseEntity<Map<String, Object>> getDriverHistory(
            @PathVariable Long driverId,
            @RequestParam(required = false) Integer hours) {
        // Future implementation: Store location history in separate table
        // For now, return current location
        return getDriverLocation(driverId);
    }
    
    /**
     * Determine driver status based on online status and active bookings
     */
    private String getDriverStatus(User driver, List<Booking> activeBookings) {
        if (driver.getIsOnline() == null || !driver.getIsOnline()) {
            return "OFFLINE";
        }
        
        if (activeBookings.isEmpty()) {
            return "ONLINE";
        }
        
        // Check if driver is en-route or on delivery
        boolean hasInTransit = activeBookings.stream()
            .anyMatch(b -> b.getStatus() == BookingStatus.IN_TRANSIT);
        boolean hasOutForDelivery = activeBookings.stream()
            .anyMatch(b -> b.getStatus() == BookingStatus.OUT_FOR_DELIVERY);
        boolean hasAssigned = activeBookings.stream()
            .anyMatch(b -> b.getStatus() == BookingStatus.ASSIGNED);
        
        if (hasOutForDelivery) {
            return "ON_DELIVERY";
        } else if (hasInTransit) {
            return "EN_ROUTE";
        } else if (hasAssigned) {
            return "ASSIGNED";
        }
        
        return "ONLINE";
    }
}







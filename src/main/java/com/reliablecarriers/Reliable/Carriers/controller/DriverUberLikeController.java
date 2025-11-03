package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Uber-like Driver Controller
 * Provides real-time delivery requests, accept/reject, and driver status management
 */
@RestController
@RequestMapping("/api/driver/uber")
@CrossOrigin(origins = "*")
public class DriverUberLikeController {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Toggle driver online/offline status
     * POST /api/driver/uber/status
     */
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            Boolean isOnline = request.get("isOnline");
            driver.setIsOnline(isOnline != null ? isOnline : false);
            userRepository.save(driver);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "isOnline", driver.getIsOnline(),
                "message", driver.getIsOnline() ? "You are now online" : "You are now offline"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Update driver location
     * POST /api/driver/uber/location
     */
    @PostMapping("/location")
    public ResponseEntity<Map<String, Object>> updateLocation(
            @RequestBody Map<String, Double> request,
            Authentication authentication) {
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            Double latitude = request.get("latitude");
            Double longitude = request.get("longitude");
            
            if (latitude != null && longitude != null) {
                driver.setCurrentLatitude(java.math.BigDecimal.valueOf(latitude));
                driver.setCurrentLongitude(java.math.BigDecimal.valueOf(longitude));
                driver.setLastLocationUpdate(new Date());
                
                // Update driver status based on active bookings
                List<Booking> activeBookings = bookingRepository.findByDriverIdAndStatusIn(
                    driver.getId(),
                    Arrays.asList(BookingStatus.ASSIGNED, BookingStatus.IN_TRANSIT, BookingStatus.OUT_FOR_DELIVERY)
                );
                
                // Ensure driver is online if they have active bookings
                if (!activeBookings.isEmpty() && (driver.getIsOnline() == null || !driver.getIsOnline())) {
                    driver.setIsOnline(true);
                }
                
                userRepository.save(driver);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Location updated"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get available delivery requests near driver
     * GET /api/driver/uber/requests
     */
    @GetMapping("/requests")
    public ResponseEntity<Map<String, Object>> getAvailableRequests(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "10") Double radiusKm,
            Authentication authentication) {
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            // Only show requests if driver is online
            if (driver.getIsOnline() == null || !driver.getIsOnline()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "You must be online to see delivery requests",
                    "data", Collections.emptyList()
                ));
            }
            
            // Get unassigned confirmed bookings
            List<Booking> availableBookings = bookingRepository.findByStatusAndDriverIsNull(BookingStatus.CONFIRMED);
            
            // Filter by proximity if location provided
            List<Map<String, Object>> requests = new ArrayList<>();
            for (Booking booking : availableBookings) {
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("bookingId", booking.getId());
                requestData.put("bookingNumber", booking.getBookingNumber());
                requestData.put("pickupAddress", booking.getPickupAddress());
                requestData.put("deliveryAddress", booking.getDeliveryAddress());
                requestData.put("totalAmount", booking.getTotalAmount());
                requestData.put("weight", booking.getWeight());
                requestData.put("createdAt", booking.getCreatedAt());
                
                // Calculate distance if driver location provided
                if (latitude != null && longitude != null && 
                    booking.getPickupLatitude() != null && booking.getPickupLongitude() != null) {
                    double distance = calculateDistance(
                        latitude, longitude,
                        booking.getPickupLatitude().doubleValue(),
                        booking.getPickupLongitude().doubleValue()
                    );
                    requestData.put("distanceKm", Math.round(distance * 100.0) / 100.0);
                    
                    // Only include if within radius
                    if (distance <= radiusKm) {
                        requests.add(requestData);
                    }
                } else {
                    requests.add(requestData);
                }
            }
            
            // Sort by distance (nearest first)
            if (latitude != null && longitude != null) {
                requests.sort((a, b) -> {
                    Double distA = (Double) a.get("distanceKm");
                    Double distB = (Double) b.get("distanceKm");
                    if (distA == null) return 1;
                    if (distB == null) return -1;
                    return distA.compareTo(distB);
                });
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", requests,
                "count", requests.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Accept delivery request
     * POST /api/driver/uber/accept/{bookingId}
     */
    @PostMapping("/accept/{bookingId}")
    public ResponseEntity<Map<String, Object>> acceptRequest(
            @PathVariable Long bookingId,
            Authentication authentication) {
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            if (driver.getIsOnline() == null || !driver.getIsOnline()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "You must be online to accept delivery requests"
                ));
            }
            
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
            
            if (booking.getDriver() != null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "This delivery has already been assigned to another driver"
                ));
            }
            
            // Assign booking to driver
            booking.setDriver(driver);
            booking.setStatus(BookingStatus.ASSIGNED);
            bookingRepository.save(booking);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Delivery request accepted",
                "bookingNumber", booking.getBookingNumber()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get driver earnings and statistics
     * GET /api/driver/uber/earnings
     */
    @GetMapping("/earnings")
    public ResponseEntity<Map<String, Object>> getEarnings(
            @RequestParam(required = false) String period, // today, week, month
            Authentication authentication) {
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            List<Booking> deliveries = bookingRepository.findByDriverIdAndStatusIn(
                driver.getId(),
                List.of(BookingStatus.DELIVERED)
            );
            
            // Calculate earnings
            double totalEarnings = deliveries.stream()
                .filter(b -> b.getTotalAmount() != null)
                .mapToDouble(b -> b.getTotalAmount().doubleValue())
                .sum();
            
            // Today's earnings
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date todayStart = cal.getTime();
            
            double todayEarnings = deliveries.stream()
                .filter(b -> b.getUpdatedAt() != null && b.getUpdatedAt().after(todayStart))
                .filter(b -> b.getTotalAmount() != null)
                .mapToDouble(b -> b.getTotalAmount().doubleValue())
                .sum();
            
            // Weekly earnings
            cal.add(Calendar.DAY_OF_WEEK, -7);
            Date weekStart = cal.getTime();
            double weekEarnings = deliveries.stream()
                .filter(b -> b.getUpdatedAt() != null && b.getUpdatedAt().after(weekStart))
                .filter(b -> b.getTotalAmount() != null)
                .mapToDouble(b -> b.getTotalAmount().doubleValue())
                .sum();
            
            // Monthly earnings
            cal.add(Calendar.DAY_OF_WEEK, -23); // Total 30 days
            Date monthStart = cal.getTime();
            double monthEarnings = deliveries.stream()
                .filter(b -> b.getUpdatedAt() != null && b.getUpdatedAt().after(monthStart))
                .filter(b -> b.getTotalAmount() != null)
                .mapToDouble(b -> b.getTotalAmount().doubleValue())
                .sum();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "currency", "ZAR",
                "currencySymbol", "R",
                "data", Map.of(
                    "totalEarnings", totalEarnings,
                    "todayEarnings", todayEarnings,
                    "weekEarnings", weekEarnings,
                    "monthEarnings", monthEarnings,
                    "totalDeliveries", deliveries.size(),
                    "driverRating", driver.getDriverRating() != null ? driver.getDriverRating() : 0,
                    "isOnline", driver.getIsOnline() != null && driver.getIsOnline()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get driver's assigned deliveries
     * GET /api/driver/uber/deliveries
     */
    @GetMapping("/deliveries")
    public ResponseEntity<Map<String, Object>> getDeliveries(
            @RequestParam(required = false, defaultValue = "ASSIGNED") String status,
            Authentication authentication) {
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            BookingStatus bookingStatus = BookingStatus.valueOf(status);
            List<Booking> bookings = bookingRepository.findByDriverIdAndStatusIn(
                driver.getId(),
                List.of(bookingStatus)
            );
            
            List<Map<String, Object>> deliveries = bookings.stream()
                .map(booking -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("bookingId", booking.getId());
                    data.put("bookingNumber", booking.getBookingNumber());
                    data.put("trackingNumber", booking.getTrackingNumber());
                    data.put("pickupAddress", booking.getPickupAddress());
                    data.put("deliveryAddress", booking.getDeliveryAddress());
                    data.put("status", booking.getStatus().toString());
                    data.put("totalAmount", booking.getTotalAmount());
                    return data;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", deliveries
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    private User getAuthenticatedDriver(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        if (user.getRole() != UserRole.DRIVER) {
            throw new SecurityException("Only drivers can access this endpoint");
        }
        
        return user;
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}


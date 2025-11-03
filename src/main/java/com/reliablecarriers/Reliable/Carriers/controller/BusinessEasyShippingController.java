package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.BookingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.BookingResponse;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

/**
 * Business Easy Shipping Controller
 * Provides easy shipping features for businesses:
 * - Bulk shipping (create multiple shipments at once)
 * - Saved addresses (frequently used addresses)
 * - Quick order creation templates
 * 
 * Note: This controller accepts both API key authentication (for external integrations)
 * and session-based authentication (for web UI)
 */
@RestController
@RequestMapping("/api/business/easy-shipping")
@CrossOrigin(origins = "*")
public class BusinessEasyShippingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Create multiple shipments in bulk
     * POST /api/business/easy-shipping/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> createBulkShipments(
            @RequestBody List<@Valid BookingRequest> requests,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            List<Map<String, Object>> results = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (int i = 0; i < requests.size(); i++) {
                BookingRequest request = requests.get(i);
                try {
                    // Set business customer details
                    request.setCustomerEmail(businessUser.getEmail());
                    request.setCustomerName(businessUser.getBusinessName() != null ? 
                        businessUser.getBusinessName() : businessUser.getFirstName() + " " + businessUser.getLastName());
                    request.setCustomerPhone(businessUser.getPhone());
                    
                    // Create booking
                    BookingResponse booking = bookingService.createBooking(request);
                    
                    results.add(Map.of(
                        "index", i,
                        "success", true,
                        "bookingNumber", booking.getBookingNumber(),
                        "trackingNumber", booking.getTrackingNumber(),
                        "totalAmount", booking.getTotalAmount()
                    ));
                } catch (Exception e) {
                    errors.add("Shipment " + (i + 1) + ": " + e.getMessage());
                    results.add(Map.of(
                        "index", i,
                        "success", false,
                        "error", e.getMessage()
                    ));
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", String.format("Processed %d shipments. %d successful, %d failed", 
                    requests.size(), results.stream().mapToInt(r -> (Boolean) r.get("success") ? 1 : 0).sum(),
                    errors.size()),
                "results", results,
                "errors", errors
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get saved addresses for the business
     * GET /api/business/easy-shipping/saved-addresses
     */
    @GetMapping("/saved-addresses")
    public ResponseEntity<Map<String, Object>> getSavedAddresses(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Get addresses from recent bookings
            List<Booking> recentBookings = bookingRepository
                .findByCustomerEmailOrderByCreatedAtDesc(businessUser.getEmail());
            
            Set<Map<String, Object>> pickupAddresses = new LinkedHashSet<>();
            Set<Map<String, Object>> deliveryAddresses = new LinkedHashSet<>();
            
            // Extract unique addresses (last 50 bookings)
            recentBookings.stream()
                .limit(50)
                .forEach(booking -> {
                    // Pickup addresses
                    Map<String, Object> pickupAddr = Map.of(
                        "address", booking.getPickupAddress(),
                        "city", booking.getPickupCity(),
                        "state", booking.getPickupState(),
                        "postalCode", booking.getPickupPostalCode(),
                        "label", booking.getPickupAddress() + ", " + booking.getPickupCity()
                    );
                    if (pickupAddresses.size() < 20) {
                        pickupAddresses.add(pickupAddr);
                    }
                    
                    // Delivery addresses
                    Map<String, Object> deliveryAddr = Map.of(
                        "address", booking.getDeliveryAddress(),
                        "city", booking.getDeliveryCity(),
                        "state", booking.getDeliveryState(),
                        "postalCode", booking.getDeliveryPostalCode(),
                        "label", booking.getDeliveryAddress() + ", " + booking.getDeliveryCity()
                    );
                    if (deliveryAddresses.size() < 20) {
                        deliveryAddresses.add(deliveryAddr);
                    }
                });
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "pickupAddresses", new ArrayList<>(pickupAddresses),
                    "deliveryAddresses", new ArrayList<>(deliveryAddresses)
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
     * Create a quick shipment using template
     * POST /api/business/easy-shipping/quick
     */
    @PostMapping("/quick")
    public ResponseEntity<Map<String, Object>> createQuickShipment(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Extract minimal required fields for quick shipping
            BookingRequest bookingRequest = new BookingRequest();
            
            // Set service type (default to ECONOMY if not specified)
            String serviceTypeStr = (String) request.getOrDefault("serviceType", "ECONOMY");
            bookingRequest.setServiceType(ServiceType.valueOf(serviceTypeStr));
            
            // Set customer details from business
            bookingRequest.setCustomerEmail(businessUser.getEmail());
            bookingRequest.setCustomerName(businessUser.getBusinessName() != null ? 
                businessUser.getBusinessName() : businessUser.getFirstName() + " " + businessUser.getLastName());
            bookingRequest.setCustomerPhone(businessUser.getPhone());
            
            // Pickup details (can use saved address or new)
            @SuppressWarnings("unchecked")
            Map<String, String> pickup = (Map<String, String>) request.get("pickup");
            bookingRequest.setPickupAddress(pickup.get("address"));
            bookingRequest.setPickupCity(pickup.get("city"));
            bookingRequest.setPickupState(pickup.get("state"));
            bookingRequest.setPickupPostalCode(pickup.get("postalCode"));
            bookingRequest.setPickupContactName(pickup.getOrDefault("contactName", businessUser.getFirstName()));
            bookingRequest.setPickupContactPhone(pickup.getOrDefault("contactPhone", businessUser.getPhone()));
            
            // Delivery details
            @SuppressWarnings("unchecked")
            Map<String, String> delivery = (Map<String, String>) request.get("delivery");
            bookingRequest.setDeliveryAddress(delivery.get("address"));
            bookingRequest.setDeliveryCity(delivery.get("city"));
            bookingRequest.setDeliveryState(delivery.get("state"));
            bookingRequest.setDeliveryPostalCode(delivery.get("postalCode"));
            bookingRequest.setDeliveryContactName(delivery.get("contactName"));
            bookingRequest.setDeliveryContactPhone(delivery.get("contactPhone"));
            
            // Package details
            @SuppressWarnings("unchecked")
            Map<String, Object> packageInfo = (Map<String, Object>) request.get("package");
            bookingRequest.setWeight(Double.parseDouble(packageInfo.get("weight").toString()));
            bookingRequest.setDimensions((String) packageInfo.getOrDefault("dimensions", "30x20x15"));
            bookingRequest.setDescription((String) packageInfo.getOrDefault("description", "Business shipment"));
            
            // Create booking
            BookingResponse booking = bookingService.createBooking(bookingRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quick shipment created successfully",
                "data", Map.of(
                    "bookingNumber", booking.getBookingNumber(),
                    "trackingNumber", booking.getTrackingNumber(),
                    "status", booking.getStatus(),
                    "totalAmount", booking.getTotalAmount()
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
     * Get shipping statistics for business
     * GET /api/business/easy-shipping/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getShippingStatistics(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            List<Booking> bookings = bookingRepository
                .findByCustomerEmailOrderByCreatedAtDesc(businessUser.getEmail());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalShipments", bookings.size());
            stats.put("pending", bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count());
            stats.put("inTransit", bookings.stream().filter(b -> 
                b.getStatus() == BookingStatus.IN_TRANSIT || b.getStatus() == BookingStatus.OUT_FOR_DELIVERY).count());
            stats.put("delivered", bookings.stream().filter(b -> b.getStatus() == BookingStatus.DELIVERED).count());
            stats.put("totalSpent", bookings.stream()
                .map(Booking::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
            
            // Monthly statistics
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            Date oneMonthAgo = cal.getTime();
            long monthlyShipments = bookings.stream()
                .filter(b -> b.getCreatedAt().after(oneMonthAgo))
                .count();
            stats.put("monthlyShipments", monthlyShipments);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Helper method to get authenticated business user
     * Supports both API key authentication and session-based authentication
     */
    private User getAuthenticatedBusinessUser(Authentication authentication) {
        if (authentication == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String identifier = authentication.getName();
        if (identifier == null) {
            throw new SecurityException("Not authenticated");
        }
        
        // If authenticated via API key, identifier is the email
        User user = userRepository.findByEmail(identifier)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new SecurityException("This feature is only available for business accounts");
        }
        
        // Check verification status (optional - can be relaxed based on requirements)
        if (user.getBusinessVerificationStatus() != null && 
            !user.getBusinessVerificationStatus().isVerified()) {
            // Allow unverified businesses to use easy shipping, but warn them
            // They may need verification for credit terms
        }
        
        return user;
    }
}


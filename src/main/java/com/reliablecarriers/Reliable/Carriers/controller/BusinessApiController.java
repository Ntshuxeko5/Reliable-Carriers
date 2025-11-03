package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.BookingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.BookingResponse;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Business API Controller
 * Provides API endpoints for businesses to integrate with their systems
 */
@RestController
@RequestMapping("/api/business")
@CrossOrigin(origins = "*")
public class BusinessApiController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private com.reliablecarriers.Reliable.Carriers.repository.ShipmentTrackingRepository shipmentTrackingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get tracking information for a shipment
     * GET /api/business/tracking/{trackingNumber}
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Map<String, Object>> getTrackingInfo(
            @PathVariable String trackingNumber,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Find shipment by tracking number
            Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElse(null);
            
            if (shipment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify shipment belongs to this business (by email)
            if (!shipment.getSender().getEmail().equals(businessUser.getEmail())) {
                // Check if shipment customer email matches business
                Booking booking = bookingRepository.findByTrackingNumber(trackingNumber).orElse(null);
                if (booking == null || !booking.getCustomerEmail().equals(businessUser.getEmail())) {
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "You don't have access to this shipment"
                    ));
                }
            }
            
            // Build tracking response
            Map<String, Object> trackingInfo = new HashMap<>();
            trackingInfo.put("trackingNumber", shipment.getTrackingNumber());
            trackingInfo.put("status", shipment.getStatus().toString());
            trackingInfo.put("pickupAddress", shipment.getPickupAddress());
            trackingInfo.put("deliveryAddress", shipment.getDeliveryAddress());
            trackingInfo.put("estimatedDeliveryDate", shipment.getEstimatedDeliveryDate());
            
            // Get tracking history
            List<com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking> trackingEntries = 
                shipmentTrackingRepository.findByShipmentOrderByCreatedAtDesc(shipment);
            
            List<Map<String, Object>> trackingHistory = trackingEntries.stream()
                .map(t -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("status", t.getStatus() != null ? t.getStatus().toString() : "UNKNOWN");
                    entry.put("location", t.getLocation() != null ? t.getLocation() : "");
                    entry.put("timestamp", t.getCreatedAt() != null ? t.getCreatedAt() : new java.util.Date());
                    entry.put("notes", t.getNotes() != null ? t.getNotes() : "");
                    return entry;
                })
                .collect(Collectors.toList());
            
            trackingInfo.put("trackingHistory", trackingHistory);
            
            // Driver information if assigned
            if (shipment.getAssignedDriver() != null) {
                Map<String, Object> driverInfo = new HashMap<>();
                driverInfo.put("name", shipment.getAssignedDriver().getFirstName() + " " + 
                    shipment.getAssignedDriver().getLastName());
                driverInfo.put("phone", shipment.getAssignedDriver().getPhone());
                trackingInfo.put("driver", driverInfo);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", trackingInfo
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Create a new shipment/booking via API
     * POST /api/business/shipments
     */
    @PostMapping("/shipments")
    public ResponseEntity<Map<String, Object>> createShipment(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Ensure customer email matches business email
            request.setCustomerEmail(businessUser.getEmail());
            request.setCustomerName(businessUser.getBusinessName() != null ? 
                businessUser.getBusinessName() : businessUser.getFirstName() + " " + businessUser.getLastName());
            request.setCustomerPhone(businessUser.getPhone());
            
            // Create booking
            BookingResponse booking = bookingService.createBooking(request);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Shipment created successfully",
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
     * Get all shipments for the business
     * GET /api/business/shipments
     */
    @GetMapping("/shipments")
    public ResponseEntity<Map<String, Object>> getShipments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Get bookings for this business
            List<BookingResponse> bookings = bookingService.getBookingsByEmail(businessUser.getEmail());
            
            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                bookings = bookings.stream()
                    .filter(b -> b.getStatus().toString().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
            }
            
            // Paginate
            int start = page * size;
            int end = Math.min(start + size, bookings.size());
            List<BookingResponse> paginatedBookings = start < bookings.size() ?
                bookings.subList(start, end) : Collections.emptyList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paginatedBookings,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", bookings.size(),
                    "totalPages", (int) Math.ceil((double) bookings.size() / size)
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
     * Get shipment details by booking number or tracking number
     * GET /api/business/shipments/{identifier}
     */
    @GetMapping("/shipments/{identifier}")
    public ResponseEntity<Map<String, Object>> getShipmentDetails(
            @PathVariable String identifier,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Try to find by booking number or tracking number
            Booking booking = bookingRepository.findByBookingNumber(identifier)
                .orElse(bookingRepository.findByTrackingNumber(identifier).orElse(null));
            
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Verify ownership
            if (!booking.getCustomerEmail().equals(businessUser.getEmail())) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "You don't have access to this shipment"
                ));
            }
            
            BookingResponse bookingResponse = bookingService.getBookingById(booking.getId());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", bookingResponse
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get business account information
     * GET /api/business/account
     */
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> getAccountInfo(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            Map<String, Object> accountInfo = new HashMap<>();
            accountInfo.put("businessName", businessUser.getBusinessName());
            accountInfo.put("email", businessUser.getEmail());
            accountInfo.put("phone", businessUser.getPhone());
            accountInfo.put("verificationStatus", businessUser.getBusinessVerificationStatus());
            accountInfo.put("creditLimit", businessUser.getCreditLimit());
            accountInfo.put("currentBalance", businessUser.getCurrentBalance());
            accountInfo.put("paymentTerms", businessUser.getPaymentTerms());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", accountInfo
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
     */
    private User getAuthenticatedBusinessUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new SecurityException("API access is only available for business accounts");
        }
        
        if (user.getBusinessVerificationStatus() == null || 
            !user.getBusinessVerificationStatus().isVerified()) {
            throw new SecurityException("Business account must be verified to use API");
        }
        
        return user;
    }
}


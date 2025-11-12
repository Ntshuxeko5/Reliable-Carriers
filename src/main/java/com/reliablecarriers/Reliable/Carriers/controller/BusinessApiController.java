package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.BookingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.BookingResponse;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.dto.UnifiedPackageDTO;
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
    
    @Autowired
    private UnifiedPackageService unifiedPackageService;
    
    @Autowired
    private PaystackService paystackService;
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Track multiple packages at once
     * POST /api/business/shipments/track
     */
    @PostMapping("/shipments/track")
    public ResponseEntity<Map<String, Object>> trackMultiplePackages(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            @SuppressWarnings("unchecked")
            List<String> trackingNumbers = (List<String>) request.get("trackingNumbers");
            
            if (trackingNumbers == null || trackingNumbers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "trackingNumbers array is required"
                ));
            }
            
            if (trackingNumbers.size() > 100) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Maximum 100 tracking numbers allowed per request"
                ));
            }
            
            List<Map<String, Object>> results = new ArrayList<>();
            List<String> notFound = new ArrayList<>();
            List<String> accessDenied = new ArrayList<>();
            
            for (String trackingNumber : trackingNumbers) {
                try {
                    // Use unified package service to get package info
                    UnifiedPackageDTO packageInfo = 
                        unifiedPackageService.getPackageByTrackingNumber(trackingNumber);
                    
                    // Verify ownership
                    if (!packageInfo.getCustomerEmail().equals(businessUser.getEmail())) {
                        accessDenied.add(trackingNumber);
                        continue;
                    }
                    
                    // Build tracking response
                    Map<String, Object> trackingInfo = new HashMap<>();
                    trackingInfo.put("trackingNumber", packageInfo.getTrackingNumber());
                    trackingInfo.put("status", packageInfo.getUnifiedStatus() != null ? packageInfo.getUnifiedStatus() : 
                        (packageInfo.getFormattedStatus() != null ? packageInfo.getFormattedStatus() : "UNKNOWN"));
                    trackingInfo.put("pickupAddress", packageInfo.getPickupAddress());
                    trackingInfo.put("deliveryAddress", packageInfo.getDeliveryAddress());
                    trackingInfo.put("estimatedDeliveryDate", packageInfo.getEstimatedDeliveryDate());
                    trackingInfo.put("currentLocation", packageInfo.getCurrentLocation());
                    
                    // Add tracking history if available
                    if (packageInfo.getTrackingEvents() != null && !packageInfo.getTrackingEvents().isEmpty()) {
                        List<Map<String, Object>> history = new ArrayList<>();
                        for (UnifiedPackageDTO.TrackingEvent event : packageInfo.getTrackingEvents()) {
                            Map<String, Object> entry = new HashMap<>();
                            entry.put("status", event.getStatus());
                            entry.put("location", event.getLocation());
                            entry.put("timestamp", event.getTimestamp());
                            entry.put("notes", event.getNotes());
                            history.add(entry);
                        }
                        trackingInfo.put("trackingHistory", history);
                    }
                    
                    // Add driver info if assigned
                    if (packageInfo.getDriverName() != null) {
                        Map<String, Object> driverInfo = new HashMap<>();
                        driverInfo.put("name", packageInfo.getDriverName());
                        driverInfo.put("phone", packageInfo.getDriverPhone());
                        trackingInfo.put("driver", driverInfo);
                    }
                    
                    results.add(trackingInfo);
                    
                } catch (Exception e) {
                    notFound.add(trackingNumber);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            response.put("summary", Map.of(
                "total", trackingNumbers.size(),
                "found", results.size(),
                "notFound", notFound.size(),
                "accessDenied", accessDenied.size()
            ));
            
            if (!notFound.isEmpty()) {
                response.put("notFound", notFound);
            }
            
            if (!accessDenied.isEmpty()) {
                response.put("accessDenied", accessDenied);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
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
     * Supports payLater option for businesses with credit terms
     */
    @PostMapping("/shipments")
    public ResponseEntity<Map<String, Object>> createShipment(
            @Valid @RequestBody Map<String, Object> requestData,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Check if payLater is requested
            Boolean payLater = (Boolean) requestData.getOrDefault("payLater", false);
            
            // If payLater is requested, verify business has credit terms
            if (payLater) {
                if (businessUser.getBusinessVerificationStatus() == null || 
                    !businessUser.getBusinessVerificationStatus().isVerified()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Business account must be verified to use pay later option"
                    ));
                }
                
                // Check credit limit
                java.math.BigDecimal totalAmount = new java.math.BigDecimal(
                    requestData.getOrDefault("totalAmount", "0").toString());
                java.math.BigDecimal availableCredit = businessUser.getCreditLimit()
                    .subtract(businessUser.getCurrentBalance() != null ? businessUser.getCurrentBalance() : java.math.BigDecimal.ZERO);
                
                if (totalAmount.compareTo(availableCredit) > 0) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Insufficient credit limit. Available: R" + availableCredit + ", Required: R" + totalAmount
                    ));
                }
            }
            
            // Convert request data to BookingRequest
            BookingRequest request = convertToBookingRequest(requestData, businessUser);
            
            // Create booking
            BookingResponse booking = bookingService.createBooking(request);
            
            // If payLater, update booking status and business balance
            if (payLater) {
                // Update booking to PAYMENT_PENDING status
                Booking bookingEntity = bookingService.getBookingEntityById(booking.getId());
                if (bookingEntity != null) {
                    bookingEntity.setStatus(BookingStatus.PAYMENT_PENDING);
                    bookingEntity.setPaymentStatus(PaymentStatus.PENDING);
                    bookingRepository.save(bookingEntity);
                    
                    // Update business current balance
                    java.math.BigDecimal newBalance = (businessUser.getCurrentBalance() != null ? 
                        businessUser.getCurrentBalance() : java.math.BigDecimal.ZERO)
                        .add(booking.getTotalAmount());
                    businessUser.setCurrentBalance(newBalance);
                    userRepository.save(businessUser);
                }
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("bookingNumber", booking.getBookingNumber());
            responseData.put("trackingNumber", booking.getTrackingNumber());
            responseData.put("status", payLater ? "PAYMENT_PENDING" : booking.getStatus());
            responseData.put("totalAmount", booking.getTotalAmount());
            responseData.put("paymentDue", payLater ? booking.getTotalAmount() : null);
            if (payLater && businessUser.getPaymentTerms() != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_MONTH, businessUser.getPaymentTerms());
                responseData.put("paymentDueDate", cal.getTime());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", payLater ? "Shipment created successfully. Payment due within " + 
                    businessUser.getPaymentTerms() + " days." : "Shipment created successfully",
                "data", responseData
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Pay for an existing shipment/booking
     * POST /api/business/shipments/{trackingNumber}/pay
     */
    @PostMapping("/shipments/{trackingNumber}/pay")
    public ResponseEntity<Map<String, Object>> payForShipment(
            @PathVariable String trackingNumber,
            @RequestBody(required = false) Map<String, Object> paymentData,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Find booking by tracking number
            Booking booking = bookingRepository.findByTrackingNumber(trackingNumber)
                .orElse(null);
            
            if (booking == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Shipment not found"
                ));
            }
            
            // Verify ownership
            if (!booking.getCustomerEmail().equals(businessUser.getEmail())) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "You don't have access to this shipment"
                ));
            }
            
            // Check if payment is needed
            if (booking.getPaymentStatus() == PaymentStatus.COMPLETED) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Payment already completed for this shipment"
                ));
            }
            
            // If payment data includes payment reference (Paystack), verify payment
            if (paymentData != null && paymentData.containsKey("paymentReference")) {
                String paymentReference = (String) paymentData.get("paymentReference");
                
                // Verify payment with Paystack
                try {
                    PaystackResponse verificationResult = paystackService.verifyPayment(paymentReference);
                    
                    if (verificationResult.isStatus() && verificationResult.getData() != null && 
                        "success".equals(verificationResult.getData().getStatus())) {
                        
                        // Payment successful, confirm booking
                        BookingResponse confirmedBooking = bookingService.confirmBooking(booking.getId(), paymentReference);
                        
                        // Create payment record
                        Payment payment = new Payment();
                        payment.setAmount(booking.getTotalAmount());
                        payment.setPaymentMethod(PaymentMethod.PAYSTACK);
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setTransactionId(paymentReference);
                        payment.setReference(paymentReference);
                        payment.setPaymentDate(new java.util.Date());
                        payment.setBooking(booking);
                        payment.setUser(businessUser);
                        
                        paymentService.createPayment(payment);
                        
                        // Update business balance
                        java.math.BigDecimal newBalance = (businessUser.getCurrentBalance() != null ? 
                            businessUser.getCurrentBalance() : java.math.BigDecimal.ZERO)
                            .subtract(booking.getTotalAmount());
                        businessUser.setCurrentBalance(newBalance.max(java.math.BigDecimal.ZERO));
                        userRepository.save(businessUser);
                        
                        return ResponseEntity.ok(Map.of(
                            "success", true,
                            "message", "Payment successful! Your shipment has been confirmed.",
                            "data", confirmedBooking
                        ));
                    }
                } catch (Exception e) {
                    // Payment verification failed
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Payment verification failed: " + e.getMessage()
                    ));
                }
            }
            
            // If no payment reference, initialize payment
            PaystackRequest paymentRequest = new PaystackRequest();
            paymentRequest.setAmount(booking.getTotalAmount());
            paymentRequest.setEmail(businessUser.getEmail());
            paymentRequest.setReference("BOOKING_" + booking.getId() + "_" + System.currentTimeMillis());
            
            PaystackResponse paymentResponse = paystackService.initializePayment(paymentRequest);
            
            // Update booking payment reference
            bookingService.updateBookingPaymentReference(booking.getId(), paymentRequest.getReference());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment initialized. Please complete payment.",
                "payment", paymentResponse,
                "bookingId", booking.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Helper method to convert Map to BookingRequest
     */
    private BookingRequest convertToBookingRequest(Map<String, Object> data, User businessUser) {
        BookingRequest request = new BookingRequest();
        
        request.setCustomerEmail(businessUser.getEmail());
        request.setCustomerName(businessUser.getBusinessName() != null ? 
            businessUser.getBusinessName() : businessUser.getFirstName() + " " + businessUser.getLastName());
        request.setCustomerPhone(businessUser.getPhone());
        
        if (data.containsKey("pickupAddress")) request.setPickupAddress((String) data.get("pickupAddress"));
        if (data.containsKey("pickupCity")) request.setPickupCity((String) data.get("pickupCity"));
        if (data.containsKey("pickupState")) request.setPickupState((String) data.get("pickupState"));
        if (data.containsKey("pickupPostalCode")) {
            request.setPickupPostalCode((String) data.get("pickupPostalCode"));
        } else if (data.containsKey("pickupZipCode")) {
            request.setPickupPostalCode((String) data.get("pickupZipCode"));
        }
        
        if (data.containsKey("deliveryAddress")) request.setDeliveryAddress((String) data.get("deliveryAddress"));
        if (data.containsKey("deliveryCity")) request.setDeliveryCity((String) data.get("deliveryCity"));
        if (data.containsKey("deliveryState")) request.setDeliveryState((String) data.get("deliveryState"));
        if (data.containsKey("deliveryPostalCode")) {
            request.setDeliveryPostalCode((String) data.get("deliveryPostalCode"));
        } else if (data.containsKey("deliveryZipCode")) {
            request.setDeliveryPostalCode((String) data.get("deliveryZipCode"));
        }
        
        if (data.containsKey("weight")) {
            Object weight = data.get("weight");
            if (weight instanceof Double) {
                request.setWeight((Double) weight);
            } else if (weight instanceof String) {
                request.setWeight(Double.parseDouble((String) weight));
            }
        }
        
        if (data.containsKey("dimensions")) request.setDimensions((String) data.get("dimensions"));
        if (data.containsKey("description")) request.setDescription((String) data.get("description"));
        if (data.containsKey("serviceType")) {
            String serviceType = (String) data.get("serviceType");
            request.setServiceType(ServiceType.valueOf(serviceType.toUpperCase()));
        }
        
        // Set contact details (required fields)
        if (data.containsKey("pickupContactName")) {
            request.setPickupContactName((String) data.get("pickupContactName"));
        } else {
            request.setPickupContactName(businessUser.getFirstName() + " " + businessUser.getLastName());
        }
        
        if (data.containsKey("pickupContactPhone")) {
            request.setPickupContactPhone((String) data.get("pickupContactPhone"));
        } else {
            request.setPickupContactPhone(businessUser.getPhone());
        }
        
        if (data.containsKey("deliveryContactName")) {
            request.setDeliveryContactName((String) data.get("deliveryContactName"));
        } else {
            request.setDeliveryContactName((String) data.getOrDefault("recipientName", "Recipient"));
        }
        
        if (data.containsKey("deliveryContactPhone")) {
            request.setDeliveryContactPhone((String) data.get("deliveryContactPhone"));
        } else {
            request.setDeliveryContactPhone((String) data.getOrDefault("recipientPhone", businessUser.getPhone()));
        }
        
        return request;
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


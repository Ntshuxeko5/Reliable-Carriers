package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.BookingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.BookingResponse;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackRequest;
import com.reliablecarriers.Reliable.Carriers.dto.PaystackResponse;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.PaymentRepository;
import com.reliablecarriers.Reliable.Carriers.service.BookingService;
import com.reliablecarriers.Reliable.Carriers.service.PaystackService;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final PaystackService paystackService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    public BookingController(BookingService bookingService, PaystackService paystackService, PaymentService paymentService, PaymentRepository paymentRepository) {
        this.bookingService = bookingService;
        this.paystackService = paystackService;
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Create a new booking with payment integration
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody BookingRequest request) {
        try {
            BookingResponse booking = bookingService.createBooking(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("booking", booking);
            response.put("message", "Booking created successfully. Proceed to payment to confirm your booking.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to create booking: " + e.getMessage()
            ));
        }
    }

    /**
     * Initialize payment for a booking
     */
    @PostMapping("/{bookingId}/payment/initialize")
    public ResponseEntity<Map<String, Object>> initializePayment(@PathVariable Long bookingId) {
        try {
            BookingResponse booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if payment is already completed
            if (booking.getPaymentStatus() != null && 
                (booking.getPaymentStatus().equals("COMPLETED") || booking.getPaymentStatus().equals("PAID"))) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Payment already completed for this booking",
                    "duplicate", true,
                    "bookingId", bookingId
                ));
            }

            // Check if there's an existing completed payment for this booking
            Booking bookingEntity = bookingService.getBookingEntityById(bookingId);
            if (bookingEntity != null) {
                List<Payment> existingPayments = paymentRepository.findByBooking(bookingEntity);
                for (Payment payment : existingPayments) {
                    if (payment.getStatus() == PaymentStatus.COMPLETED) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "error", "A payment for this booking already exists and has been completed",
                            "duplicate", true,
                            "bookingId", bookingId,
                            "paymentId", payment.getId()
                        ));
                    }
                }
            }

            // Check if payment reference already exists and is being used
            if (booking.getPaymentReference() != null && !booking.getPaymentReference().isEmpty()) {
                // Check if this reference has a completed payment
                paymentRepository.findByReference(booking.getPaymentReference()).ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.COMPLETED) {
                        throw new RuntimeException("Payment already completed with reference: " + booking.getPaymentReference());
                    }
                });
            }

            // Create payment request
            PaystackRequest paymentRequest = new PaystackRequest();
            paymentRequest.setAmount(booking.getTotalAmount());
            paymentRequest.setEmail(booking.getCustomerEmail());
            paymentRequest.setReference("BOOKING_" + bookingId + "_" + System.currentTimeMillis());

            // Initialize payment with Paystack
            PaystackResponse paymentResponse = paystackService.initializePayment(paymentRequest);
            
            // Store payment reference in booking
            bookingService.updateBookingPaymentReference(bookingId, paymentRequest.getReference());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payment", paymentResponse);
            response.put("bookingId", bookingId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("already completed")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "duplicate", true
                ));
            }
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to initialize payment: " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to initialize payment: " + e.getMessage()
            ));
        }
    }

    /**
     * Verify payment and confirm booking
     */
    @PostMapping("/{bookingId}/payment/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @PathVariable Long bookingId,
            @RequestParam String reference) {
        try {
            // Verify payment with Paystack
            PaystackResponse verificationResult = paystackService.verifyPayment(reference);
            
            if (verificationResult.isStatus() && verificationResult.getData() != null && "success".equals(verificationResult.getData().getStatus())) {
                // Payment successful, confirm booking
                BookingResponse confirmedBooking = bookingService.confirmBooking(bookingId, reference);
                
                // Create payment record linked to booking
                Payment payment = new Payment();
                payment.setAmount(confirmedBooking.getTotalAmount());
                payment.setPaymentMethod(PaymentMethod.PAYSTACK);
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(reference);
                payment.setReference(reference);
                payment.setPaymentDate(new Date());
                
                // Link payment to booking
                Booking booking = bookingService.getBookingEntityById(bookingId);
                if (booking != null) {
                    payment.setBooking(booking);
                }
                
                paymentService.createPayment(payment);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("booking", confirmedBooking);
                response.put("payment", verificationResult);
                response.put("message", "Payment successful! Your booking has been confirmed.");
                
                return ResponseEntity.ok(response);
            } else {
                // Payment failed
                bookingService.updateBookingStatus(bookingId, BookingStatus.PAYMENT_FAILED);
                
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Payment verification failed",
                    "details", verificationResult
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Payment verification error: " + e.getMessage()
            ));
        }
    }

    /**
     * Get booking details
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> getBooking(@PathVariable Long bookingId) {
        try {
            BookingResponse booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "booking", booking
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get bookings by customer email
     */
    @GetMapping("/customer/{email}")
    public ResponseEntity<Map<String, Object>> getCustomerBookings(@PathVariable String email) {
        try {
            List<BookingResponse> bookings = bookingService.getBookingsByEmail(email);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "bookings", bookings,
                "count", bookings.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Cancel a booking
     */
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable Long bookingId) {
        try {
            boolean cancelled = bookingService.cancelBooking(bookingId);
            if (cancelled) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Booking cancelled successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Booking cannot be cancelled"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get booking status
     */
    @GetMapping("/{bookingId}/status")
    public ResponseEntity<Map<String, Object>> getBookingStatus(@PathVariable Long bookingId) {
        try {
            BookingStatus status = bookingService.getBookingStatus(bookingId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "status", status,
                "bookingId", bookingId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Update booking details (before payment)
     */
    @PutMapping("/{bookingId}")
    public ResponseEntity<Map<String, Object>> updateBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingRequest request) {
        try {
            BookingResponse updatedBooking = bookingService.updateBooking(bookingId, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "booking", updatedBooking,
                "message", "Booking updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get available service types
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getAvailableServices() {
        try {
            List<Map<String, Object>> services = bookingService.getAvailableServices();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "services", services
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Calculate booking price
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<Map<String, Object>> calculatePrice(@Valid @RequestBody BookingRequest request) {
        try {
            Map<String, Object> priceDetails = bookingService.calculatePrice(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "price", priceDetails
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "Booking Service is running"));
    }
}

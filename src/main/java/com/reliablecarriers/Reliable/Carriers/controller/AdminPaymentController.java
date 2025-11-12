package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Get all payments with optional filters
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPayments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) Long userId) {
        try {
            List<Payment> payments;

            if (status != null && !status.isEmpty()) {
                payments = paymentService.getPaymentsByStatus(PaymentStatus.valueOf(status.toUpperCase()));
            } else if (startDate != null && endDate != null) {
                payments = paymentService.getPaymentsByDateRange(startDate, endDate);
            } else {
                payments = paymentService.getAllPayments();
            }

            // Filter by user if specified
            if (userId != null) {
                payments = payments.stream()
                    .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                    .collect(Collectors.toList());
            }

            List<Map<String, Object>> paymentList = payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "payments", paymentList,
                "total", paymentList.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to load payments: " + e.getMessage()
            ));
        }
    }

    /**
     * Get payment statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            List<Payment> allPayments;
            
            if (startDate != null && endDate != null) {
                allPayments = paymentService.getPaymentsByDateRange(startDate, endDate);
            } else {
                allPayments = paymentService.getAllPayments();
            }

            Map<String, Object> stats = new HashMap<>();
            
            // Total payments
            stats.put("totalPayments", allPayments.size());
            
            // Status breakdown
            long completed = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .count();
            long pending = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .count();
            long failed = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .count();
            long refunded = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .count();
            
            stats.put("completedPayments", completed);
            stats.put("pendingPayments", pending);
            stats.put("failedPayments", failed);
            stats.put("refundedPayments", refunded);
            
            // Revenue calculations
            BigDecimal totalRevenue = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalRefunded = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            stats.put("totalRevenue", totalRevenue);
            stats.put("totalRefunded", totalRefunded);
            stats.put("netRevenue", totalRevenue.subtract(totalRefunded));
            
            // Average payment amount
            if (completed > 0) {
                BigDecimal avgAmount = totalRevenue.divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP);
                stats.put("averagePaymentAmount", avgAmount);
            } else {
                stats.put("averagePaymentAmount", BigDecimal.ZERO);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Failed to load statistics: " + e.getMessage()
            ));
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentById(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "payment", convertToDTO(payment)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Payment not found: " + e.getMessage()
            ));
        }
    }

    /**
     * Process a pending payment
     */
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<Map<String, Object>> processPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.processPayment(paymentId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment processed successfully",
                "payment", convertToDTO(payment)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "Failed to process payment: " + e.getMessage()
            ));
        }
    }

    /**
     * Refund a payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> request) {
        try {
            String reason = request.getOrDefault("reason", "Admin refund");
            Payment payment = paymentService.refundPayment(paymentId, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment refunded successfully",
                "payment", convertToDTO(payment)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "Failed to refund payment: " + e.getMessage()
            ));
        }
    }

    /**
     * Update payment status
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody Map<String, String> request) {
        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(request.get("status").toUpperCase());
            Payment payment = paymentService.getPaymentById(paymentId);
            payment.setStatus(newStatus);
            Payment updatedPayment = paymentService.updatePayment(paymentId, payment);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment status updated successfully",
                "payment", convertToDTO(updatedPayment)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "Failed to update payment status: " + e.getMessage()
            ));
        }
    }

    /**
     * Convert Payment to DTO
     */
    private Map<String, Object> convertToDTO(Payment payment) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", payment.getId());
        dto.put("transactionId", payment.getTransactionId());
        dto.put("reference", payment.getReference());
        dto.put("amount", payment.getAmount());
        dto.put("status", payment.getStatus() != null ? payment.getStatus().toString() : null);
        dto.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : null);
        dto.put("notes", payment.getNotes());
        dto.put("paymentDate", payment.getPaymentDate());
        dto.put("createdAt", payment.getCreatedAt());
        dto.put("updatedAt", payment.getUpdatedAt());
        
        if (payment.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", payment.getUser().getId());
            userInfo.put("email", payment.getUser().getEmail());
            userInfo.put("firstName", payment.getUser().getFirstName());
            userInfo.put("lastName", payment.getUser().getLastName());
            dto.put("user", userInfo);
        }
        
        if (payment.getShipment() != null) {
            Map<String, Object> shipmentInfo = new HashMap<>();
            shipmentInfo.put("id", payment.getShipment().getId());
            shipmentInfo.put("trackingNumber", payment.getShipment().getTrackingNumber());
            dto.put("shipment", shipmentInfo);
        }
        
        if (payment.getBooking() != null) {
            Map<String, Object> bookingInfo = new HashMap<>();
            bookingInfo.put("id", payment.getBooking().getId());
            bookingInfo.put("trackingNumber", payment.getBooking().getTrackingNumber());
            dto.put("booking", bookingInfo);
        }
        
        return dto;
    }
}


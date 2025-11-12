package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.InvoiceService;
import com.reliablecarriers.Reliable.Carriers.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/payments")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerPaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private InvoiceService invoiceService;

    /**
     * Get payment history for the current customer
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getPaymentHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            List<Payment> payments = paymentService.getPaymentsByUser(currentUser);
            
            // Apply filters
            if (startDate != null && endDate != null) {
                payments = payments.stream()
                    .filter(p -> p.getPaymentDate() != null && 
                                p.getPaymentDate().after(startDate) && 
                                p.getPaymentDate().before(endDate))
                    .collect(Collectors.toList());
            }
            
            if (status != null) {
                payments = payments.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
            }

            // Sort by payment date (most recent first)
            payments.sort((a, b) -> {
                Date dateA = a.getPaymentDate() != null ? a.getPaymentDate() : a.getCreatedAt();
                Date dateB = b.getPaymentDate() != null ? b.getPaymentDate() : b.getCreatedAt();
                return dateB.compareTo(dateA);
            });

            // Pagination
            int total = payments.size();
            int start = page * size;
            int end = Math.min(start + size, total);
            List<Payment> paginatedPayments = start < total ? payments.subList(start, end) : new ArrayList<>();

            // Calculate statistics
            BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPending = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRefunded = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Convert to DTOs
            List<Map<String, Object>> paymentDTOs = paginatedPayments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payments", paymentDTOs);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("totalPages", (int) Math.ceil((double) total / size));
            response.put("statistics", Map.of(
                "totalPaid", totalPaid,
                "totalPending", totalPending,
                "totalRefunded", totalRefunded,
                "totalPayments", total
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching payment history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch payment history: " + e.getMessage()));
        }
    }

    /**
     * Get payment statistics for the current customer
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate) {
        
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            List<Payment> payments = paymentService.getPaymentsByUser(currentUser);
            
            if (startDate != null && endDate != null) {
                payments = payments.stream()
                    .filter(p -> p.getPaymentDate() != null && 
                                p.getPaymentDate().after(startDate) && 
                                p.getPaymentDate().before(endDate))
                    .collect(Collectors.toList());
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPayments", payments.size());
            stats.put("completedPayments", payments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count());
            stats.put("pendingPayments", payments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count());
            stats.put("failedPayments", payments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count());
            stats.put("refundedPayments", payments.stream().filter(p -> p.getStatus() == PaymentStatus.REFUNDED).count());
            
            stats.put("totalPaid", payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
            
            stats.put("totalPending", payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

            return ResponseEntity.ok(Map.of("success", true, "statistics", stats));
        } catch (Exception e) {
            logger.error("Error fetching payment statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch payment statistics: " + e.getMessage()));
        }
    }

    /**
     * Get a specific payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable Long paymentId) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            Payment payment = paymentService.getPaymentById(paymentId);
            
            // Verify payment belongs to current user
            if (payment.getUser() == null || !payment.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Payment not found or access denied"));
            }

            return ResponseEntity.ok(Map.of("success", true, "payment", convertToDTO(payment)));
        } catch (Exception e) {
            logger.error("Error fetching payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch payment: " + e.getMessage()));
        }
    }

    /**
     * Download payment receipt/invoice as PDF
     */
    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long paymentId) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Payment payment = paymentService.getPaymentById(paymentId);
            
            // Verify payment belongs to current user
            if (payment.getUser() == null || !payment.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Generate PDF receipt/invoice
            byte[] pdfBytes = invoiceService.generateReceiptPDF(payment);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "receipt_" + payment.getTransactionId() + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error generating receipt", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download invoice as PDF
     */
    @GetMapping("/{paymentId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long paymentId) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Payment payment = paymentService.getPaymentById(paymentId);
            
            // Verify payment belongs to current user
            if (payment.getUser() == null || !payment.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Generate PDF invoice
            byte[] pdfBytes = invoiceService.generateInvoicePDF(payment);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String invoiceNumber = invoiceService.generateInvoiceNumber(paymentId);
            String filename = invoiceNumber + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error generating invoice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert Payment entity to DTO
     */
    private Map<String, Object> convertToDTO(Payment payment) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", payment.getId());
        dto.put("transactionId", payment.getTransactionId());
        dto.put("reference", payment.getReference());
        dto.put("invoiceNumber", invoiceService.generateInvoiceNumber(payment.getId()));
        dto.put("amount", payment.getAmount());
        dto.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null);
        dto.put("status", payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.put("notes", payment.getNotes());
        dto.put("paymentDate", payment.getPaymentDate());
        dto.put("createdAt", payment.getCreatedAt());
        
        // Add related package info
        if (payment.getShipment() != null) {
            Map<String, Object> shipmentInfo = new HashMap<>();
            shipmentInfo.put("trackingNumber", payment.getShipment().getTrackingNumber());
            shipmentInfo.put("id", payment.getShipment().getId());
            dto.put("shipment", shipmentInfo);
        }
        
        if (payment.getBooking() != null) {
            Map<String, Object> bookingInfo = new HashMap<>();
            bookingInfo.put("bookingNumber", payment.getBooking().getBookingNumber());
            bookingInfo.put("id", payment.getBooking().getId());
            dto.put("booking", bookingInfo);
        }
        
        return dto;
    }

    /**
     * Generate receipt text (kept for backward compatibility, but now using PDF)
     */
    @SuppressWarnings("unused")
    private String generateReceiptText(Payment payment) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("========================================\n");
        receipt.append("     RELIABLE CARRIERS\n");
        receipt.append("         PAYMENT RECEIPT\n");
        receipt.append("========================================\n\n");
        receipt.append("Transaction ID: ").append(payment.getTransactionId()).append("\n");
        if (payment.getReference() != null) {
            receipt.append("Reference: ").append(payment.getReference()).append("\n");
        }
        receipt.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
            payment.getPaymentDate() != null ? payment.getPaymentDate() : payment.getCreatedAt())).append("\n");
        receipt.append("Amount: R").append(payment.getAmount()).append("\n");
        receipt.append("Payment Method: ").append(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "N/A").append("\n");
        receipt.append("Status: ").append(payment.getStatus() != null ? payment.getStatus().name() : "N/A").append("\n");
        
        if (payment.getShipment() != null) {
            receipt.append("Tracking Number: ").append(payment.getShipment().getTrackingNumber()).append("\n");
        }
        
        if (payment.getBooking() != null) {
            receipt.append("Booking Number: ").append(payment.getBooking().getBookingNumber()).append("\n");
        }
        
        receipt.append("\n========================================\n");
        receipt.append("Thank you for your business!\n");
        receipt.append("========================================\n");
        
        return receipt.toString();
    }
}


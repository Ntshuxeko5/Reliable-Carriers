package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/business/payments")
@CrossOrigin(origins = "*")
public class BusinessPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessPaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get payment history for the authenticated business user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPaymentHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);

            List<Payment> payments = paymentService.getPaymentsByUser(businessUser);
            
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
            List<Map<String, Object>> paymentsData = paginatedPayments.stream()
                .map(payment -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", payment.getId());
                    dto.put("transactionId", payment.getTransactionId());
                    dto.put("reference", payment.getReference());
                    dto.put("amount", payment.getAmount());
                    dto.put("status", payment.getStatus() != null ? payment.getStatus().toString() : "PENDING");
                    dto.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "N/A");
                    dto.put("paymentDate", payment.getPaymentDate());
                    dto.put("createdAt", payment.getCreatedAt());
                    dto.put("notes", payment.getNotes());
                    dto.put("invoiceNumber", invoiceService.generateInvoiceNumber(payment.getId()));
                    
                    // Add related package info
                    if (payment.getShipment() != null) {
                        dto.put("trackingNumber", payment.getShipment().getTrackingNumber());
                        dto.put("packageType", "SHIPMENT");
                    } else if (payment.getBooking() != null) {
                        dto.put("trackingNumber", payment.getBooking().getTrackingNumber());
                        dto.put("packageType", "BOOKING");
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", paymentsData,
                "statistics", Map.of(
                    "totalPaid", totalPaid,
                    "totalPending", totalPending,
                    "totalRefunded", totalRefunded,
                    "totalTransactions", total
                ),
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
                )
            ));
        } catch (Exception e) {
            logger.error("Error fetching payment history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // Verify ownership
            if (payment.getUser() == null || !payment.getUser().getId().equals(businessUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Payment not found or access denied"));
            }
            
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", payment.getId());
            dto.put("transactionId", payment.getTransactionId());
            dto.put("reference", payment.getReference());
            dto.put("amount", payment.getAmount());
            dto.put("status", payment.getStatus() != null ? payment.getStatus().toString() : "PENDING");
            dto.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "N/A");
            dto.put("paymentDate", payment.getPaymentDate());
            dto.put("createdAt", payment.getCreatedAt());
            dto.put("notes", payment.getNotes());
            dto.put("invoiceNumber", invoiceService.generateInvoiceNumber(payment.getId()));
            
            // Add related package info
            if (payment.getShipment() != null) {
                dto.put("trackingNumber", payment.getShipment().getTrackingNumber());
                dto.put("packageType", "SHIPMENT");
            } else if (payment.getBooking() != null) {
                dto.put("trackingNumber", payment.getBooking().getTrackingNumber());
                dto.put("packageType", "BOOKING");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dto
            ));
        } catch (Exception e) {
            logger.error("Error fetching payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Download payment receipt PDF
     */
    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // Verify ownership
            if (payment.getUser() == null || !payment.getUser().getId().equals(businessUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            byte[] pdfBytes = invoiceService.generateReceiptPDF(payment);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "receipt-" + payment.getTransactionId() + ".pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error generating receipt PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download payment invoice PDF
     */
    @GetMapping("/{paymentId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long paymentId,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // Verify ownership
            if (payment.getUser() == null || !payment.getUser().getId().equals(businessUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            byte[] pdfBytes = invoiceService.generateInvoicePDF(payment);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "invoice-" + invoiceService.generateInvoiceNumber(payment.getId()) + ".pdf");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
        } catch (Exception e) {
            logger.error("Error generating invoice PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private User getAuthenticatedBusinessUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new SecurityException("This endpoint is only available for business accounts");
        }
        
        return user;
    }
}


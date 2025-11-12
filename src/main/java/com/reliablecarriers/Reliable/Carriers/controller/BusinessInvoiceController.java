package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Payment;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/business/invoices")
@CrossOrigin(origins = "*")
public class BusinessInvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(BusinessInvoiceController.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all invoices for the authenticated business user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInvoices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            // Get all payments for this business user
            List<Payment> payments = paymentService.getPaymentsByUser(businessUser);
            
            // Filter by date range if provided
            if (startDate != null && endDate != null) {
                payments = payments.stream()
                    .filter(p -> p.getPaymentDate() != null && 
                                p.getPaymentDate().after(startDate) && 
                                p.getPaymentDate().before(endDate))
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
            
            // Convert to DTOs
            List<Map<String, Object>> invoicesData = paginatedPayments.stream()
                .map(payment -> {
                    Map<String, Object> invoice = new HashMap<>();
                    invoice.put("id", payment.getId());
                    invoice.put("invoiceNumber", invoiceService.generateInvoiceNumber(payment.getId()));
                    invoice.put("transactionId", payment.getTransactionId());
                    invoice.put("amount", payment.getAmount());
                    invoice.put("status", payment.getStatus() != null ? payment.getStatus().toString() : "PENDING");
                    invoice.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "N/A");
                    invoice.put("paymentDate", payment.getPaymentDate());
                    invoice.put("createdAt", payment.getCreatedAt());
                    
                    // Add related package info
                    if (payment.getShipment() != null) {
                        invoice.put("trackingNumber", payment.getShipment().getTrackingNumber());
                        invoice.put("packageType", "SHIPMENT");
                    } else if (payment.getBooking() != null) {
                        invoice.put("trackingNumber", payment.getBooking().getTrackingNumber());
                        invoice.put("packageType", "BOOKING");
                    }
                    
                    return invoice;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", invoicesData,
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
                )
            ));
        } catch (Exception e) {
            logger.error("Error fetching invoices", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Get invoice by ID
     */
    @GetMapping("/{invoiceId}")
    public ResponseEntity<Map<String, Object>> getInvoice(
            @PathVariable Long invoiceId,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            Payment payment = paymentService.getPaymentById(invoiceId);
            
            // Verify ownership
            if (payment.getUser() == null || !payment.getUser().getId().equals(businessUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Invoice not found or access denied"));
            }
            
            Map<String, Object> invoice = new HashMap<>();
            invoice.put("id", payment.getId());
            invoice.put("invoiceNumber", invoiceService.generateInvoiceNumber(payment.getId()));
            invoice.put("transactionId", payment.getTransactionId());
            invoice.put("reference", payment.getReference());
            invoice.put("amount", payment.getAmount());
            invoice.put("status", payment.getStatus() != null ? payment.getStatus().toString() : "PENDING");
            invoice.put("paymentMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : "N/A");
            invoice.put("paymentDate", payment.getPaymentDate());
            invoice.put("createdAt", payment.getCreatedAt());
            invoice.put("notes", payment.getNotes());
            
            // Add related package info
            if (payment.getShipment() != null) {
                invoice.put("trackingNumber", payment.getShipment().getTrackingNumber());
                invoice.put("packageType", "SHIPMENT");
            } else if (payment.getBooking() != null) {
                invoice.put("trackingNumber", payment.getBooking().getTrackingNumber());
                invoice.put("packageType", "BOOKING");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", invoice
            ));
        } catch (Exception e) {
            logger.error("Error fetching invoice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Download invoice PDF
     */
    @GetMapping("/{invoiceId}/download")
    public ResponseEntity<byte[]> downloadInvoice(
            @PathVariable Long invoiceId,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            Payment payment = paymentService.getPaymentById(invoiceId);
            
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

    /**
     * Get invoice statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            List<Payment> payments = paymentService.getPaymentsByUser(businessUser);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalInvoices", payments.size());
            stats.put("totalAmount", payments.stream()
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
            stats.put("paidAmount", payments.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().toString().equals("COMPLETED"))
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
            stats.put("pendingAmount", payments.stream()
                .filter(p -> p.getStatus() != null && p.getStatus().toString().equals("PENDING"))
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            logger.error("Error fetching invoice statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
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


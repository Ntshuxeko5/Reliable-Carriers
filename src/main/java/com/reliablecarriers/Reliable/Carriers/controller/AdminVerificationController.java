package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.BusinessDocumentService;
import com.reliablecarriers.Reliable.Carriers.service.DriverDocumentService;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin controller for verification management
 * Handles verification of both drivers and businesses
 */
@RestController
@RequestMapping("/api/admin/verification")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminVerificationController {
    
    @Autowired
    private DriverDocumentService driverDocumentService;
    
    @Autowired
    private BusinessDocumentService businessDocumentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EmailService emailService;
    
    // ==================== DRIVER VERIFICATION ====================
    
    /**
     * Get all pending driver documents for review
     * GET /api/admin/verification/drivers/pending-documents
     */
    @GetMapping("/drivers/pending-documents")
    public ResponseEntity<Map<String, Object>> getPendingDriverDocuments() {
        try {
            List<com.reliablecarriers.Reliable.Carriers.model.DriverDocument> pendingDocs = 
                driverDocumentService.getPendingDocuments();
            
            List<Map<String, Object>> docsData = pendingDocs.stream()
                .map(doc -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", doc.getId());
                    data.put("driverId", doc.getDriver().getId());
                    data.put("driverName", doc.getDriver().getFirstName() + " " + doc.getDriver().getLastName());
                    data.put("driverEmail", doc.getDriver().getEmail());
                    data.put("driverPhone", doc.getDriver().getPhone());
                    data.put("documentType", doc.getDocumentType().toString());
                    data.put("documentTypeName", doc.getDocumentType().getDisplayName());
                    data.put("fileName", doc.getFileName());
                    data.put("fileSize", doc.getFileSize());
                    data.put("isCertified", doc.getIsCertified());
                    data.put("certifiedBy", doc.getCertifiedBy());
                    data.put("certificationDate", doc.getCertificationDate());
                    data.put("expiresAt", doc.getExpiresAt());
                    data.put("verificationStatus", doc.getVerificationStatus().toString());
                    data.put("createdAt", doc.getCreatedAt());
                    data.put("driverStatus", doc.getDriver().getDriverVerificationStatus());
                    return data;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", docsData.size(),
                "documents", docsData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Verify driver document
     * POST /api/admin/verification/drivers/documents/{documentId}/verify
     */
    @PostMapping("/drivers/documents/{documentId}/verify")
    public ResponseEntity<Map<String, Object>> verifyDriverDocument(
            @PathVariable Long documentId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long adminId = getAdminId(authentication);
            Boolean approved = (Boolean) request.get("approved");
            String notes = (String) request.get("notes");
            
            if (approved == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Approved status is required"
                ));
            }
            
            boolean result = driverDocumentService.verifyDocument(documentId, adminId, approved, notes);
            
            if (result) {
                // Get document to send notification
                com.reliablecarriers.Reliable.Carriers.model.DriverDocument doc = 
                    driverDocumentService.getDocumentById(documentId);
                User driver = doc.getDriver();
                
                // Send SMS notification
                String message = approved 
                    ? String.format("Your %s has been verified and approved.", doc.getDocumentType().getDisplayName())
                    : String.format("Your %s verification was rejected. Reason: %s", 
                        doc.getDocumentType().getDisplayName(), notes != null ? notes : "Please check with admin");
                
                notificationService.sendCustomSmsNotification(driver.getPhone(), message);
                
                // Send email notification
                try {
                    String driverName = (driver.getFirstName() != null ? driver.getFirstName() : "") + 
                        " " + (driver.getLastName() != null ? driver.getLastName() : "").trim();
                    if (driverName.trim().isEmpty()) driverName = driver.getEmail();
                    emailService.sendDriverVerificationStatus(
                        driver.getEmail(),
                        driverName,
                        doc.getDocumentType().getDisplayName(),
                        approved,
                        notes
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send verification email: " + e.getMessage());
                }
                
                // Check if driver is now fully approved
                if (approved && driver.getDriverVerificationStatus() == DriverVerificationStatus.APPROVED) {
                    notificationService.sendCustomSmsNotification(driver.getPhone(), 
                        "Congratulations! All your documents have been verified. Your driver account is now approved and active.");
                    try {
                        emailService.sendSimpleEmail(driver.getEmail(), 
                            "Driver Account Approved - Reliable Carriers",
                            "Congratulations! All your documents have been verified. Your driver account is now approved and active. You can now log in and start accepting deliveries.");
                    } catch (Exception e) {
                        System.err.println("Failed to send approval email: " + e.getMessage());
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "Document approved" : "Document rejected"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get driver verification summary
     * GET /api/admin/verification/drivers/summary
     */
    @GetMapping("/drivers/summary")
    public ResponseEntity<Map<String, Object>> getDriverVerificationSummary() {
        try {
            List<User> drivers = userRepository.findByRole(UserRole.DRIVER);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDrivers", drivers.size());
            summary.put("pending", drivers.stream()
                .filter(d -> d.getDriverVerificationStatus() == null || 
                    d.getDriverVerificationStatus().isPending())
                .count());
            summary.put("approved", drivers.stream()
                .filter(d -> d.getDriverVerificationStatus() == DriverVerificationStatus.APPROVED)
                .count());
            summary.put("rejected", drivers.stream()
                .filter(d -> d.getDriverVerificationStatus() == DriverVerificationStatus.REJECTED)
                .count());
            
            // Get pending documents count
            List<com.reliablecarriers.Reliable.Carriers.model.DriverDocument> pendingDocs = 
                driverDocumentService.getPendingDocuments();
            summary.put("pendingDocuments", pendingDocs.size());
            
            return ResponseEntity.ok(Map.of("success", true, "summary", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    // ==================== BUSINESS VERIFICATION ====================
    
    /**
     * Get all pending business documents for review
     * GET /api/admin/verification/businesses/pending-documents
     */
    @GetMapping("/businesses/pending-documents")
    public ResponseEntity<Map<String, Object>> getPendingBusinessDocuments() {
        try {
            List<BusinessDocument> pendingDocs = businessDocumentService.getPendingDocuments();
            
            List<Map<String, Object>> docsData = pendingDocs.stream()
                .map(doc -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", doc.getId());
                    data.put("businessId", doc.getBusiness().getId());
                    data.put("businessName", doc.getBusiness().getBusinessName());
                    data.put("businessEmail", doc.getBusiness().getEmail());
                    data.put("businessPhone", doc.getBusiness().getPhone());
                    data.put("registrationNumber", doc.getBusiness().getRegistrationNumber());
                    data.put("taxId", doc.getBusiness().getTaxId());
                    data.put("documentType", doc.getDocumentType().toString());
                    data.put("documentTypeName", doc.getDocumentType().getDisplayName());
                    data.put("fileName", doc.getFileName());
                    data.put("fileSize", doc.getFileSize());
                    data.put("isCertified", doc.getIsCertified());
                    data.put("certifiedBy", doc.getCertifiedBy());
                    data.put("certificationDate", doc.getCertificationDate());
                    data.put("expiresAt", doc.getExpiresAt());
                    data.put("verificationStatus", doc.getVerificationStatus().toString());
                    data.put("createdAt", doc.getCreatedAt());
                    data.put("businessStatus", doc.getBusiness().getBusinessVerificationStatus());
                    return data;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", docsData.size(),
                "documents", docsData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Verify business document
     * POST /api/admin/verification/businesses/documents/{documentId}/verify
     */
    @PostMapping("/businesses/documents/{documentId}/verify")
    public ResponseEntity<Map<String, Object>> verifyBusinessDocument(
            @PathVariable Long documentId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long adminId = getAdminId(authentication);
            Boolean approved = (Boolean) request.get("approved");
            String notes = (String) request.get("notes");
            
            if (approved == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Approved status is required"
                ));
            }
            
            boolean result = businessDocumentService.verifyDocument(documentId, adminId, approved, notes);
            
            if (result) {
                // Get document to send notification
                BusinessDocument doc = businessDocumentService.getDocumentById(documentId);
                User business = doc.getBusiness();
                
                // Send SMS notification
                String message = approved 
                    ? String.format("Your %s has been verified and approved.", doc.getDocumentType().getDisplayName())
                    : String.format("Your %s verification was rejected. Reason: %s", 
                        doc.getDocumentType().getDisplayName(), notes != null ? notes : "Please check with admin");
                
                notificationService.sendCustomSmsNotification(business.getPhone(), message);
                
                // Send email notification
                try {
                    emailService.sendBusinessDocumentVerificationStatus(
                        business.getEmail(),
                        business.getBusinessName() != null ? business.getBusinessName() : business.getEmail(),
                        doc.getDocumentType().getDisplayName(),
                        approved,
                        notes
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send business document verification email: " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "Document approved" : "Document rejected"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Approve or reject business verification
     * POST /api/admin/verification/businesses/{businessId}/verify
     */
    @PostMapping("/businesses/{businessId}/verify")
    public ResponseEntity<Map<String, Object>> verifyBusiness(
            @PathVariable Long businessId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long adminId = getAdminId(authentication);
            Boolean approved = (Boolean) request.get("approved");
            String notes = (String) request.get("notes");
            java.math.BigDecimal creditLimit = request.get("creditLimit") != null 
                ? new java.math.BigDecimal(request.get("creditLimit").toString()) 
                : null;
            Integer paymentTerms = request.get("paymentTerms") != null 
                ? Integer.valueOf(request.get("paymentTerms").toString()) 
                : null;
            
            if (approved == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Approved status is required"
                ));
            }
            
            User business = userRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            if (business.getIsBusiness() == null || !business.getIsBusiness()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User is not a business account"
                ));
            }
            
            if (approved) {
                // Check if all required documents are verified
                if (!businessDocumentService.hasAllRequiredDocuments(business)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Cannot approve business. Not all required documents have been verified."
                    ));
                }
                
                business.setBusinessVerificationStatus(BusinessVerificationStatus.APPROVED);
                business.setVerifiedBy(adminId);
                business.setVerifiedAt(new Date());
                
                // Set credit terms if provided
                if (creditLimit != null) {
                    business.setCreditLimit(creditLimit);
                } else {
                    // Default credit limit for approved businesses
                    business.setCreditLimit(java.math.BigDecimal.valueOf(50000)); // R50,000 default
                }
                
                if (paymentTerms != null) {
                    business.setPaymentTerms(paymentTerms);
                } else {
                    business.setPaymentTerms(30); // Net 30 days default
                }
                
                business.setVerificationNotes(notes);
                
                // Send approval notification
                notificationService.sendCustomSmsNotification(business.getPhone(),
                    "Congratulations! Your business account has been verified and approved. You can now access all business features.");
                
                // Send email notification
                try {
                    emailService.sendBusinessAccountVerificationStatus(
                        business.getEmail(),
                        business.getBusinessName() != null ? business.getBusinessName() : business.getEmail(),
                        true,
                        notes,
                        creditLimit != null ? creditLimit.toString() : "50000",
                        paymentTerms != null ? paymentTerms.toString() : "30"
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send business verification email: " + e.getMessage());
                }
                
            } else {
                business.setBusinessVerificationStatus(BusinessVerificationStatus.REJECTED);
                business.setVerifiedBy(adminId);
                business.setVerifiedAt(new Date());
                business.setVerificationNotes(notes);
                
                // Send rejection notification
                notificationService.sendCustomSmsNotification(business.getPhone(),
                    String.format("Your business verification was rejected. Reason: %s. Please contact support for assistance.",
                        notes != null ? notes : "Please check with admin"));
                
                // Send email notification
                try {
                    emailService.sendBusinessAccountVerificationStatus(
                        business.getEmail(),
                        business.getBusinessName() != null ? business.getBusinessName() : business.getEmail(),
                        false,
                        notes,
                        null,
                        null
                    );
                } catch (Exception e) {
                    System.err.println("Failed to send business rejection email: " + e.getMessage());
                }
            }
            
            userRepository.save(business);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", approved ? "Business approved" : "Business rejected",
                "verificationStatus", business.getBusinessVerificationStatus().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get business verification summary
     * GET /api/admin/verification/businesses/summary
     */
    @GetMapping("/businesses/summary")
    public ResponseEntity<Map<String, Object>> getBusinessVerificationSummary() {
        try {
            List<User> businesses = userRepository.findAll().stream()
                .filter(u -> u.getIsBusiness() != null && u.getIsBusiness())
                .collect(Collectors.toList());
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalBusinesses", businesses.size());
            summary.put("pending", businesses.stream()
                .filter(b -> b.getBusinessVerificationStatus() == null || 
                    b.getBusinessVerificationStatus() == BusinessVerificationStatus.PENDING)
                .count());
            summary.put("underReview", businesses.stream()
                .filter(b -> b.getBusinessVerificationStatus() == BusinessVerificationStatus.UNDER_REVIEW)
                .count());
            summary.put("approved", businesses.stream()
                .filter(b -> b.getBusinessVerificationStatus() != null && 
                    b.getBusinessVerificationStatus().isVerified())
                .count());
            summary.put("rejected", businesses.stream()
                .filter(b -> b.getBusinessVerificationStatus() == BusinessVerificationStatus.REJECTED)
                .count());
            
            // Get pending documents count
            List<BusinessDocument> pendingDocs = businessDocumentService.getPendingDocuments();
            summary.put("pendingDocuments", pendingDocs.size());
            
            return ResponseEntity.ok(Map.of("success", true, "summary", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get all pending verifications (combined dashboard)
     * GET /api/admin/verification/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getAllPendingVerifications() {
        try {
            Map<String, Object> pending = new HashMap<>();
            
            // Driver documents
            List<com.reliablecarriers.Reliable.Carriers.model.DriverDocument> driverDocs = 
                driverDocumentService.getPendingDocuments();
            pending.put("driverDocuments", driverDocs.size());
            
            // Business documents
            List<BusinessDocument> businessDocs = businessDocumentService.getPendingDocuments();
            pending.put("businessDocuments", businessDocs.size());
            
            // Pending drivers
            long pendingDrivers = userRepository.findByRole(UserRole.DRIVER).stream()
                .filter(d -> d.getDriverVerificationStatus() == null || 
                    d.getDriverVerificationStatus().isPending())
                .count();
            pending.put("pendingDrivers", pendingDrivers);
            
            // Pending businesses
            long pendingBusinesses = userRepository.findAll().stream()
                .filter(u -> u.getIsBusiness() != null && u.getIsBusiness())
                .filter(b -> b.getBusinessVerificationStatus() == null || 
                    b.getBusinessVerificationStatus() == BusinessVerificationStatus.PENDING ||
                    b.getBusinessVerificationStatus() == BusinessVerificationStatus.UNDER_REVIEW)
                .count();
            pending.put("pendingBusinesses", pendingBusinesses);
            
            return ResponseEntity.ok(Map.of("success", true, "pending", pending));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get business details with documents for verification
     * GET /api/admin/verification/businesses/{businessId}/details
     */
    @GetMapping("/businesses/{businessId}/details")
    public ResponseEntity<Map<String, Object>> getBusinessVerificationDetails(@PathVariable Long businessId) {
        try {
            User business = userRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            if (business.getIsBusiness() == null || !business.getIsBusiness()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User is not a business account"
                ));
            }
            
            List<BusinessDocument> documents = businessDocumentService.getBusinessDocuments(business);
            
            Map<String, Object> businessData = new HashMap<>();
            businessData.put("id", business.getId());
            businessData.put("businessName", business.getBusinessName());
            businessData.put("email", business.getEmail());
            businessData.put("phone", business.getPhone());
            businessData.put("registrationNumber", business.getRegistrationNumber());
            businessData.put("taxId", business.getTaxId());
            businessData.put("address", business.getAddress());
            businessData.put("city", business.getCity());
            businessData.put("state", business.getState());
            businessData.put("zipCode", business.getZipCode());
            businessData.put("country", business.getCountry());
            businessData.put("verificationStatus", business.getBusinessVerificationStatus());
            businessData.put("verificationNotes", business.getVerificationNotes());
            businessData.put("verifiedBy", business.getVerifiedBy());
            businessData.put("verifiedAt", business.getVerifiedAt());
            businessData.put("creditLimit", business.getCreditLimit());
            businessData.put("paymentTerms", business.getPaymentTerms());
            businessData.put("createdAt", business.getCreatedAt());
            
            List<Map<String, Object>> docsData = documents.stream()
                .map(doc -> {
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("id", doc.getId());
                    docData.put("documentType", doc.getDocumentType().toString());
                    docData.put("documentTypeName", doc.getDocumentType().getDisplayName());
                    docData.put("fileName", doc.getFileName());
                    docData.put("fileSize", doc.getFileSize());
                    docData.put("isCertified", doc.getIsCertified());
                    docData.put("certifiedBy", doc.getCertifiedBy());
                    docData.put("certificationDate", doc.getCertificationDate());
                    docData.put("expiresAt", doc.getExpiresAt());
                    docData.put("verificationStatus", doc.getVerificationStatus().toString());
                    docData.put("rejectionReason", doc.getRejectionReason());
                    docData.put("createdAt", doc.getCreatedAt());
                    return docData;
                })
                .collect(Collectors.toList());
            
            businessData.put("documents", docsData);
            businessData.put("hasAllRequiredDocuments", businessDocumentService.hasAllRequiredDocuments(business));
            
            return ResponseEntity.ok(Map.of("success", true, "business", businessData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get driver details with documents for verification
     * GET /api/admin/verification/drivers/{driverId}/details
     */
    @GetMapping("/drivers/{driverId}/details")
    public ResponseEntity<Map<String, Object>> getDriverVerificationDetails(@PathVariable Long driverId) {
        try {
            User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));
            
            if (driver.getRole() != UserRole.DRIVER) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User is not a driver"
                ));
            }
            
            List<com.reliablecarriers.Reliable.Carriers.model.DriverDocument> documents = 
                driverDocumentService.getDriverDocuments(driver);
            
            Map<String, Object> driverData = new HashMap<>();
            driverData.put("id", driver.getId());
            driverData.put("firstName", driver.getFirstName());
            driverData.put("lastName", driver.getLastName());
            driverData.put("email", driver.getEmail());
            driverData.put("phone", driver.getPhone());
            driverData.put("address", driver.getAddress());
            driverData.put("city", driver.getCity());
            driverData.put("state", driver.getState());
            driverData.put("zipCode", driver.getZipCode());
            driverData.put("country", driver.getCountry());
            driverData.put("verificationStatus", driver.getDriverVerificationStatus());
            driverData.put("verificationNotes", driver.getDriverVerificationNotes());
            driverData.put("verifiedBy", driver.getDriverVerifiedBy());
            driverData.put("verifiedAt", driver.getDriverVerifiedAt());
            driverData.put("createdAt", driver.getCreatedAt());
            
            List<Map<String, Object>> docsData = documents.stream()
                .map(doc -> {
                    Map<String, Object> docData = new HashMap<>();
                    docData.put("id", doc.getId());
                    docData.put("documentType", doc.getDocumentType().toString());
                    docData.put("documentTypeName", doc.getDocumentType().getDisplayName());
                    docData.put("fileName", doc.getFileName());
                    docData.put("fileSize", doc.getFileSize());
                    docData.put("isCertified", doc.getIsCertified());
                    docData.put("certifiedBy", doc.getCertifiedBy());
                    docData.put("certificationDate", doc.getCertificationDate());
                    docData.put("expiresAt", doc.getExpiresAt());
                    docData.put("verificationStatus", doc.getVerificationStatus().toString());
                    docData.put("rejectionReason", doc.getRejectionReason());
                    docData.put("createdAt", doc.getCreatedAt());
                    return docData;
                })
                .collect(Collectors.toList());
            
            driverData.put("documents", docsData);
            driverData.put("hasAllRequiredDocuments", driverDocumentService.hasAllRequiredDocuments(driver));
            
            return ResponseEntity.ok(Map.of("success", true, "driver", driverData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Download/view driver document (admin only)
     * GET /api/admin/verification/drivers/documents/{documentId}/download
     */
    @GetMapping("/drivers/documents/{documentId}/download")
    public ResponseEntity<?> downloadDriverDocument(@PathVariable Long documentId) {
        try {
            com.reliablecarriers.Reliable.Carriers.model.DriverDocument document = 
                driverDocumentService.getDocumentById(documentId);
            
            java.nio.file.Path filePath = java.nio.file.Paths.get(document.getFilePath());
            if (!java.nio.file.Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = java.nio.file.Files.readAllBytes(filePath);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + document.getFileName() + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                    document.getMimeType() != null ? document.getMimeType() : "application/octet-stream"))
                .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to download document: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Download/view business document (admin only)
     * GET /api/admin/verification/businesses/documents/{documentId}/download
     */
    @GetMapping("/businesses/documents/{documentId}/download")
    public ResponseEntity<?> downloadBusinessDocument(@PathVariable Long documentId) {
        try {
            BusinessDocument document = businessDocumentService.getDocumentById(documentId);
            
            java.nio.file.Path filePath = java.nio.file.Paths.get(document.getFilePath());
            if (!java.nio.file.Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = java.nio.file.Files.readAllBytes(filePath);
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + document.getFileName() + "\"")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                    document.getMimeType() != null ? document.getMimeType() : "application/octet-stream"))
                .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Failed to download document: " + e.getMessage()
            ));
        }
    }
    
    private Long getAdminId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String email = authentication.getName();
        User admin = userRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("Admin user not found"));
        
        return admin.getId();
    }
}


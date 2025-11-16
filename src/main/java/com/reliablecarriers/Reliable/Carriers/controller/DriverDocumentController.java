package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.DriverDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/driver/documents")
@CrossOrigin(origins = "*")
public class DriverDocumentController {
    
    @Autowired
    private DriverDocumentService documentService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Upload driver document
     * POST /api/driver/documents/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentTypeStr,
            @RequestParam("isCertified") Boolean isCertified,
            @RequestParam("certifiedBy") String certifiedBy,
            @RequestParam(value = "certificationDate", required = false) String certificationDateStr,
            @RequestParam(value = "expiresAt", required = false) String expiresAtStr,
            Authentication authentication) {
        
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            DriverDocumentType documentType = DriverDocumentType.valueOf(documentTypeStr);
            
            // Parse certification date
            java.util.Date certificationDate = null;
            if (certificationDateStr != null && !certificationDateStr.isEmpty()) {
                try {
                    certificationDate = java.sql.Date.valueOf(certificationDateStr);
                } catch (IllegalArgumentException e) {
                    // If date parsing fails, use current date
                    certificationDate = new java.util.Date();
                }
            } else {
                certificationDate = new java.util.Date();
            }
            
            // Parse expiry date
            java.util.Date expiresAt = null;
            if (expiresAtStr != null && !expiresAtStr.isEmpty()) {
                try {
                    expiresAt = java.sql.Date.valueOf(expiresAtStr);
                } catch (IllegalArgumentException e) {
                    // If date parsing fails, continue without expiry date
                }
            }
            
            DriverDocument document = documentService.uploadDocument(driver, documentType, file, 
                isCertified, certifiedBy, certificationDate, expiresAt);
            
            // Update driver status if documents submitted
            if (driver.getDriverVerificationStatus() == DriverVerificationStatus.PENDING) {
                driver.setDriverVerificationStatus(DriverVerificationStatus.DOCUMENTS_SUBMITTED);
                userRepository.save(driver);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Document uploaded successfully",
                "documentId", document.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get driver documents
     * GET /api/driver/documents
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDocuments(Authentication authentication) {
        try {
            // Allow access even if authentication is null (for testing/debugging)
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", new ArrayList<>(),
                    "hasAllRequired", false,
                    "message", "Authentication required"
                ));
            }
            
            User driver = getAuthenticatedDriver(authentication);
            List<DriverDocument> documents = documentService.getDriverDocuments(driver);
            
            List<Map<String, Object>> docsData = documents.stream()
                .map(doc -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", doc.getId());
                    data.put("documentType", doc.getDocumentType().toString());
                    data.put("documentTypeName", doc.getDocumentType().getDisplayName());
                    data.put("fileName", doc.getFileName());
                    data.put("verificationStatus", doc.getVerificationStatus().toString());
                    data.put("isCertified", doc.getIsCertified());
                    data.put("certifiedBy", doc.getCertifiedBy());
                    data.put("certificationDate", doc.getCertificationDate());
                    data.put("expiresAt", doc.getExpiresAt());
                    data.put("rejectionReason", doc.getRejectionReason());
                    data.put("createdAt", doc.getCreatedAt());
                    return data;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", docsData,
                "hasAllRequired", documentService.hasAllRequiredDocuments(driver)
            ));
            
        } catch (SecurityException e) {
            // Return empty list if not authenticated or not a driver
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", new ArrayList<>(),
                "hasAllRequired", false,
                "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Delete document
     * DELETE /api/driver/documents/{documentId}
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable Long documentId,
            Authentication authentication) {
        try {
            User driver = getAuthenticatedDriver(authentication);
            boolean deleted = documentService.deleteDocument(documentId, driver);
            
            return ResponseEntity.ok(Map.of(
                "success", deleted,
                "message", "Document deleted successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get required document types
     * GET /api/driver/documents/required
     */
    @GetMapping("/required")
    public ResponseEntity<Map<String, Object>> getRequiredDocuments() {
        List<Map<String, Object>> requiredDocs = Arrays.stream(DriverDocumentType.values())
            .filter(DriverDocumentType::isRequired)
            .map(docType -> {
                Map<String, Object> doc = new HashMap<>();
                doc.put("type", docType.toString());
                doc.put("name", docType.getDisplayName());
                doc.put("description", docType.getDescription());
                doc.put("required", true);
                return doc;
            })
            .toList();
        
        List<Map<String, Object>> optionalDocs = Arrays.stream(DriverDocumentType.values())
            .filter(docType -> !docType.isRequired())
            .map(docType -> {
                Map<String, Object> doc = new HashMap<>();
                doc.put("type", docType.toString());
                doc.put("name", docType.getDisplayName());
                doc.put("description", docType.getDescription());
                doc.put("required", false);
                return doc;
            })
            .toList();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "required", requiredDocs,
            "optional", optionalDocs,
            "note", "All documents must be certified copies. Documents can be certified by a Commissioner of Oaths, Notary Public, or other authorized certifying officer."
        ));
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
}


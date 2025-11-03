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
            @RequestParam(value = "expiresAt", required = false) String expiresAtStr,
            Authentication authentication) {
        
        try {
            User driver = getAuthenticatedDriver(authentication);
            
            DriverDocumentType documentType = DriverDocumentType.valueOf(documentTypeStr);
            java.util.Date expiresAt = null;
            if (expiresAtStr != null && !expiresAtStr.isEmpty()) {
                try {
                    expiresAt = java.sql.Date.valueOf(expiresAtStr);
                } catch (IllegalArgumentException e) {
                    // If date parsing fails, continue without expiry date
                }
            }
            
            DriverDocument document = documentService.uploadDocument(driver, documentType, file, expiresAt);
            
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
            User driver = getAuthenticatedDriver(authentication);
            List<DriverDocument> documents = documentService.getDriverDocuments(driver);
            
            List<Map<String, Object>> docsData = documents.stream()
                .map(doc -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", doc.getId());
                    data.put("documentType", doc.getDocumentType().toString());
                    data.put("fileName", doc.getFileName());
                    data.put("verificationStatus", doc.getVerificationStatus().toString());
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


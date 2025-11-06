package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.DriverDocument;
import com.reliablecarriers.Reliable.Carriers.model.DriverDocumentType;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface DriverDocumentService {
    
    /**
     * Upload driver document (must be certified)
     */
    DriverDocument uploadDocument(User driver, DriverDocumentType documentType, MultipartFile file, 
                                 Boolean isCertified, String certifiedBy, Date certificationDate, Date expiresAt);
    
    /**
     * Get all documents for a driver
     */
    List<DriverDocument> getDriverDocuments(User driver);
    
    /**
     * Get document by ID
     */
    DriverDocument getDocumentById(Long documentId);
    
    /**
     * Verify document (admin action)
     */
    boolean verifyDocument(Long documentId, Long adminUserId, boolean approved, String notes);
    
    /**
     * Delete document
     */
    boolean deleteDocument(Long documentId, User driver);
    
    /**
     * Check if driver has all required documents
     */
    boolean hasAllRequiredDocuments(User driver);
    
    /**
     * Get pending documents for admin review
     */
    List<DriverDocument> getPendingDocuments();
}


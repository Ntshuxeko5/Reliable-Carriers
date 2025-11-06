package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.BusinessDocument;
import com.reliablecarriers.Reliable.Carriers.model.BusinessDocumentType;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

public interface BusinessDocumentService {
    
    /**
     * Upload business document (must be certified)
     */
    BusinessDocument uploadDocument(User business, BusinessDocumentType documentType, MultipartFile file, 
                                   Boolean isCertified, String certifiedBy, Date certificationDate, Date expiresAt);
    
    /**
     * Get all documents for a business
     */
    List<BusinessDocument> getBusinessDocuments(User business);
    
    /**
     * Get document by ID
     */
    BusinessDocument getDocumentById(Long documentId);
    
    /**
     * Verify document (admin action)
     */
    boolean verifyDocument(Long documentId, Long adminUserId, boolean approved, String notes);
    
    /**
     * Delete document
     */
    boolean deleteDocument(Long documentId, User business);
    
    /**
     * Check if business has all required documents
     */
    boolean hasAllRequiredDocuments(User business);
    
    /**
     * Get pending documents for admin review
     */
    List<BusinessDocument> getPendingDocuments();
}



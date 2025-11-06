package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.BusinessDocument;
import com.reliablecarriers.Reliable.Carriers.model.DocumentVerificationStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessDocumentRepository extends JpaRepository<BusinessDocument, Long> {
    
    /**
     * Find all documents for a business
     */
    List<BusinessDocument> findByBusinessOrderByCreatedAtDesc(User business);
    
    /**
     * Find documents by verification status
     */
    List<BusinessDocument> findByVerificationStatusOrderByCreatedAtDesc(DocumentVerificationStatus status);
    
    /**
     * Find documents by business and document type
     */
    List<BusinessDocument> findByBusinessAndDocumentType(User business, com.reliablecarriers.Reliable.Carriers.model.BusinessDocumentType documentType);
}


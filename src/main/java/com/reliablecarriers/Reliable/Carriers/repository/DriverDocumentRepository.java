package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.DriverDocument;
import com.reliablecarriers.Reliable.Carriers.model.DriverDocumentType;
import com.reliablecarriers.Reliable.Carriers.model.DocumentVerificationStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
    
    List<DriverDocument> findByDriver(User driver);
    
    List<DriverDocument> findByDriverAndDocumentType(User driver, DriverDocumentType documentType);
    
    Optional<DriverDocument> findByDriverAndDocumentTypeAndVerificationStatus(
        User driver, DriverDocumentType documentType, DocumentVerificationStatus status);
    
    List<DriverDocument> findByVerificationStatus(DocumentVerificationStatus status);
    
    List<DriverDocument> findByDriverOrderByCreatedAtDesc(User driver);
}






package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.DriverDocumentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.DriverDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class DriverDocumentServiceImpl implements DriverDocumentService {
    
    @Autowired
    private DriverDocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${app.upload.dir:uploads/driver-documents}")
    private String uploadDirectory;
    
    @Override
    @Transactional
    public DriverDocument uploadDocument(User driver, DriverDocumentType documentType, MultipartFile file,
                                        Boolean isCertified, String certifiedBy, Date certificationDate, Date expiresAt) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Validate it's a certified copy (required for all documents)
            if (isCertified == null || !isCertified) {
                throw new IllegalArgumentException("All driver documents must be certified copies. Please provide certification details.");
            }
            
            if (certifiedBy == null || certifiedBy.trim().isEmpty()) {
                throw new IllegalArgumentException("Certified by name is required (e.g., Commissioner of Oaths, Notary Public, etc.)");
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create document record
            DriverDocument document = new DriverDocument();
            document.setDriver(driver);
            document.setDocumentType(documentType);
            document.setFilePath(filePath.toString());
            document.setFileName(originalFilename);
            document.setFileSize(file.getSize());
            document.setMimeType(file.getContentType());
            document.setIsCertified(isCertified);
            document.setCertifiedBy(certifiedBy);
            document.setCertificationDate(certificationDate != null ? certificationDate : new Date());
            document.setVerificationStatus(DocumentVerificationStatus.PENDING);
            document.setExpiresAt(expiresAt);
            
            return documentRepository.save(document);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document", e);
        }
    }
    
    @Override
    public List<DriverDocument> getDriverDocuments(User driver) {
        return documentRepository.findByDriverOrderByCreatedAtDesc(driver);
    }
    
    @Override
    public DriverDocument getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }
    
    @Override
    @Transactional
    public boolean verifyDocument(Long documentId, Long adminUserId, boolean approved, String notes) {
        DriverDocument document = getDocumentById(documentId);
        
        if (approved) {
            document.setVerificationStatus(DocumentVerificationStatus.VERIFIED);
            document.setVerifiedAt(new Date());
        } else {
            document.setVerificationStatus(DocumentVerificationStatus.REJECTED);
            document.setRejectionReason(notes);
        }
        
        document.setVerifiedBy(adminUserId);
        documentRepository.save(document);
        
        // Check if all required documents are verified
        User driver = document.getDriver();
        if (approved && hasAllRequiredDocuments(driver)) {
            // Update driver verification status
            driver.setDriverVerificationStatus(DriverVerificationStatus.APPROVED);
            driver.setDriverVerifiedBy(adminUserId);
            driver.setDriverVerifiedAt(new Date());
            userRepository.save(driver);
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean deleteDocument(Long documentId, User driver) {
        DriverDocument document = getDocumentById(documentId);
        
        if (!document.getDriver().getId().equals(driver.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete this document");
        }
        
        // Delete file
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            logger.warn("Failed to delete file: " + document.getFilePath(), e);
        }
        
        documentRepository.delete(document);
        return true;
    }
    
    @Override
    public boolean hasAllRequiredDocuments(User driver) {
        List<DriverDocument> documents = documentRepository.findByDriver(driver);
        
        // Check for all required document types
        for (DriverDocumentType docType : DriverDocumentType.values()) {
            if (docType.isRequired()) {
                boolean hasVerified = documents.stream()
                    .anyMatch(d -> d.getDocumentType() == docType && 
                           d.getVerificationStatus() == DocumentVerificationStatus.VERIFIED);
                if (!hasVerified) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public List<DriverDocument> getPendingDocuments() {
        return documentRepository.findByVerificationStatus(DocumentVerificationStatus.PENDING);
    }
    
    private static final Logger logger = LoggerFactory.getLogger(DriverDocumentServiceImpl.class);
}


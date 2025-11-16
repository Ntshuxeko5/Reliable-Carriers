package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.BusinessDocumentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.BusinessDocumentService;
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
public class BusinessDocumentServiceImpl implements BusinessDocumentService {
    
    private static final Logger logger = LoggerFactory.getLogger(BusinessDocumentServiceImpl.class);
    
    @Autowired
    private BusinessDocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${app.upload.dir:uploads/documents/business}")
    private String uploadDir;
    
    @Override
    @Transactional
    public BusinessDocument uploadDocument(User business, BusinessDocumentType documentType, MultipartFile file,
                                          Boolean isCertified, String certifiedBy, Date certificationDate, Date expiresAt) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException("Invalid file name");
            }
            String contentType = file.getContentType() != null ? file.getContentType() : "";
            long size = file.getSize();
            if (size <= 0) {
                throw new IllegalArgumentException("Invalid file size");
            }
            if (size > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("File exceeds 10MB limit");
            }
            boolean allowedType = contentType.equalsIgnoreCase("application/pdf") ||
                                  contentType.equalsIgnoreCase("image/png") ||
                                  contentType.equalsIgnoreCase("image/jpeg");
            if (!allowedType) {
                throw new IllegalArgumentException("Unsupported file type");
            }
            
            // Validate it's a certified copy (required for all documents)
            if (isCertified == null || !isCertified) {
                throw new IllegalArgumentException("All business documents must be certified copies. Please provide certification details.");
            }
            
            if (certifiedBy == null || certifiedBy.trim().isEmpty()) {
                throw new IllegalArgumentException("Certified by name is required");
            }
            
            Path uploadPath = resolveUploadPath(uploadDir, "business-documents");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Create document record
            BusinessDocument document = new BusinessDocument();
            document.setBusiness(business);
            document.setDocumentType(documentType);
            document.setFilePath(filePath.toString());
            document.setFileName(originalFilename);
            document.setFileSize(file.getSize());
            document.setMimeType(file.getContentType());
            document.setIsCertified(isCertified);
            document.setCertifiedBy(certifiedBy);
            document.setCertificationDate(certificationDate != null ? certificationDate : new Date());
            document.setExpiresAt(expiresAt);
            document.setVerificationStatus(DocumentVerificationStatus.PENDING);
            
            return documentRepository.save(document);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload document: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<BusinessDocument> getBusinessDocuments(User business) {
        return documentRepository.findByBusinessOrderByCreatedAtDesc(business);
    }
    
    @Override
    public BusinessDocument getDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found"));
    }
    
    @Override
    @Transactional
    public boolean verifyDocument(Long documentId, Long adminUserId, boolean approved, String notes) {
        BusinessDocument document = getDocumentById(documentId);
        
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
        User business = document.getBusiness();
        if (approved && hasAllRequiredDocuments(business)) {
            // Update business verification status to UNDER_REVIEW (admin will manually approve)
            if (business.getBusinessVerificationStatus() == BusinessVerificationStatus.PENDING) {
                business.setBusinessVerificationStatus(BusinessVerificationStatus.UNDER_REVIEW);
                userRepository.save(business);
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean deleteDocument(Long documentId, User business) {
        BusinessDocument document = getDocumentById(documentId);
        
        if (!document.getBusiness().getId().equals(business.getId())) {
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
    public boolean hasAllRequiredDocuments(User business) {
        List<BusinessDocument> documents = documentRepository.findByBusinessOrderByCreatedAtDesc(business);
        
        // Check for all required document types
        for (BusinessDocumentType docType : BusinessDocumentType.values()) {
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
    public List<BusinessDocument> getPendingDocuments() {
        return documentRepository.findByVerificationStatusOrderByCreatedAtDesc(DocumentVerificationStatus.PENDING);
    }
    
    private Path resolveUploadPath(String configured, String subfolder) {
        try {
            if (configured == null || configured.isBlank()) {
                configured = System.getProperty("java.io.tmpdir");
            }
            Path base = Paths.get(configured);
            String lower = configured.toLowerCase();
            boolean alreadySpecific = lower.contains(subfolder) || lower.contains("documents/business");
            Path path = alreadySpecific ? base : base.resolve(subfolder);
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException ignored) {
                    Path tmp = Paths.get(System.getProperty("java.io.tmpdir")).resolve(subfolder);
                    if (!Files.exists(tmp)) {
                        Files.createDirectories(tmp);
                    }
                    return tmp;
                }
            }
            return path;
        } catch (Exception ex) {
            Path tmp = Paths.get(System.getProperty("java.io.tmpdir")).resolve(subfolder);
            try {
                if (!Files.exists(tmp)) Files.createDirectories(tmp);
            } catch (IOException ignored) {}
            return tmp;
        }
    }
}



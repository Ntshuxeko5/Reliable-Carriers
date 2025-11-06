package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.DocumentVerificationStatus;
import com.reliablecarriers.Reliable.Carriers.model.DriverDocument;
import com.reliablecarriers.Reliable.Carriers.model.BusinessDocument;
import com.reliablecarriers.Reliable.Carriers.repository.DriverDocumentRepository;
import com.reliablecarriers.Reliable.Carriers.repository.BusinessDocumentRepository;
import com.reliablecarriers.Reliable.Carriers.service.DocumentExpiryService;
import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service for managing document expiry alerts and status updates
 */
@Service
public class DocumentExpiryServiceImpl implements DocumentExpiryService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentExpiryServiceImpl.class);
    
    @Autowired
    private DriverDocumentRepository driverDocumentRepository;
    
    @Autowired
    private BusinessDocumentRepository businessDocumentRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Check for expiring documents and send alerts
     * Alerts sent 30 days, 14 days, and 7 days before expiry
     */
    @Override
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    public void checkExpiringDocuments() {
        logger.info("Starting document expiry check...");
        
        LocalDate today = LocalDate.now();
        LocalDate days30 = today.plusDays(30);
        LocalDate days14 = today.plusDays(14);
        LocalDate days7 = today.plusDays(7);
        
        // Check driver documents
        List<DriverDocument> driverDocs = driverDocumentRepository.findAll();
        for (DriverDocument doc : driverDocs) {
            if (doc.getExpiresAt() != null && doc.getVerificationStatus() == DocumentVerificationStatus.VERIFIED) {
                Date expiryDate = doc.getExpiresAt();
                LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                
                // Check if expiring within 30, 14, or 7 days
                if (expiryLocalDate.isEqual(days30) || expiryLocalDate.isEqual(days14) || expiryLocalDate.isEqual(days7)) {
                    String daysUntilExpiry = "";
                    if (expiryLocalDate.isEqual(days30)) daysUntilExpiry = "30";
                    else if (expiryLocalDate.isEqual(days14)) daysUntilExpiry = "14";
                    else if (expiryLocalDate.isEqual(days7)) daysUntilExpiry = "7";
                    
                    if (doc.getDriver() != null && doc.getDriver().getEmail() != null && doc.getDocumentType() != null) {
                        try {
                            emailService.sendDocumentExpiryWarning(
                                doc.getDriver().getEmail(),
                                doc.getDriver().getFirstName() + " " + doc.getDriver().getLastName(),
                                doc.getDocumentType().toString(),
                                expiryLocalDate.toString()
                            );
                            logger.info("Sent expiry warning email for driver document ID: {} (expires in {} days)", 
                                doc.getId(), daysUntilExpiry);
                        } catch (Exception e) {
                            logger.error("Failed to send expiry warning email for driver document ID: {}", doc.getId(), e);
                        }
                    }
                }
            }
        }
        
        // Check business documents
        List<BusinessDocument> businessDocs = businessDocumentRepository.findAll();
        for (BusinessDocument doc : businessDocs) {
            if (doc.getExpiresAt() != null && doc.getVerificationStatus() == DocumentVerificationStatus.VERIFIED) {
                Date expiryDate = doc.getExpiresAt();
                LocalDate expiryLocalDate = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                
                // Check if expiring within 30, 14, or 7 days
                if (expiryLocalDate.isEqual(days30) || expiryLocalDate.isEqual(days14) || expiryLocalDate.isEqual(days7)) {
                    String daysUntilExpiry = "";
                    if (expiryLocalDate.isEqual(days30)) daysUntilExpiry = "30";
                    else if (expiryLocalDate.isEqual(days14)) daysUntilExpiry = "14";
                    else if (expiryLocalDate.isEqual(days7)) daysUntilExpiry = "7";
                    
                    if (doc.getBusiness() != null && doc.getBusiness().getEmail() != null && doc.getDocumentType() != null) {
                        try {
                            emailService.sendDocumentExpiryWarning(
                                doc.getBusiness().getEmail(),
                                doc.getBusiness().getBusinessName() != null ? doc.getBusiness().getBusinessName() : 
                                    doc.getBusiness().getFirstName() + " " + doc.getBusiness().getLastName(),
                                doc.getDocumentType().toString(),
                                expiryLocalDate.toString()
                            );
                            logger.info("Sent expiry warning email for business document ID: {} (expires in {} days)", 
                                doc.getId(), daysUntilExpiry);
                        } catch (Exception e) {
                            logger.error("Failed to send expiry warning email for business document ID: {}", doc.getId(), e);
                        }
                    }
                }
            }
        }
        
        logger.info("Document expiry check completed.");
    }
    
    /**
     * Mark expired documents as EXPIRED
     */
    @Override
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    public void markExpiredDocuments() {
        logger.info("Starting expired document marking...");
        
        Date todayDate = new Date();
        int updatedCount = 0;
        
        // Mark expired driver documents
        List<DriverDocument> driverDocs = driverDocumentRepository.findAll();
        for (DriverDocument doc : driverDocs) {
            if (doc.getExpiresAt() != null 
                && doc.getExpiresAt().before(todayDate)
                && doc.getVerificationStatus() != DocumentVerificationStatus.EXPIRED) {
                doc.setVerificationStatus(DocumentVerificationStatus.EXPIRED);
                driverDocumentRepository.save(doc);
                updatedCount++;
                logger.info("Marked driver document ID: {} as EXPIRED", doc.getId());
            }
        }
        
        // Mark expired business documents
        List<BusinessDocument> businessDocs = businessDocumentRepository.findAll();
        for (BusinessDocument doc : businessDocs) {
            if (doc.getExpiresAt() != null 
                && doc.getExpiresAt().before(todayDate)
                && doc.getVerificationStatus() != DocumentVerificationStatus.EXPIRED) {
                doc.setVerificationStatus(DocumentVerificationStatus.EXPIRED);
                businessDocumentRepository.save(doc);
                updatedCount++;
                logger.info("Marked business document ID: {} as EXPIRED", doc.getId());
            }
        }
        
        logger.info("Expired document marking completed. Updated {} documents.", updatedCount);
    }
}

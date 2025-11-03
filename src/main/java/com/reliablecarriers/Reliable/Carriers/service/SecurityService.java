package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.PhotoVerification;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SecurityService {
    
    /**
     * Enable biometric authentication for user
     */
    boolean enableBiometricAuth(User user, String biometricData);
    
    /**
     * Verify biometric authentication
     */
    boolean verifyBiometricAuth(User user, String biometricData);
    
    /**
     * Upload and verify photo
     */
    PhotoVerification uploadPhotoVerification(
        Long shipmentId, 
        Long driverId, 
        String verificationType,
        MultipartFile photo,
        Double latitude,
        Double longitude,
        String address
    );
    
    /**
     * Verify uploaded photo
     */
    boolean verifyPhoto(PhotoVerification photoVerification, String verificationNotes);
    
    /**
     * Get photo verifications for shipment
     */
    List<PhotoVerification> getPhotoVerifications(Long shipmentId);
    
    /**
     * Capture digital signature
     */
 String captureDigitalSignature(
        Long shipmentId,
        String signatureData,
        String signerName,
        String signerEmail
    );
    
    /**
     * Verify digital signature
     */
    boolean verifyDigitalSignature(String signatureId);
    
    /**
     * Get security features available for user
     */
    List<String> getAvailableSecurityFeatures(User user);
    
    /**
     * Check if user has security feature enabled
     */
    boolean hasSecurityFeature(User user, String feature);
    
    /**
     * Generate security report
     */
    Map<String, Object> generateSecurityReport(User user);
    
    /**
     * Enable fraud detection for user
     */
    boolean enableFraudDetection(User user);
    
    /**
     * Check for suspicious activity
     */
    Map<String, Object> checkSuspiciousActivity(User user, String activity);
}


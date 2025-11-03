package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.PhotoVerification;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.PhotoVerificationRepository;
import com.reliablecarriers.Reliable.Carriers.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private PhotoVerificationRepository photoVerificationRepository;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/verifications/";

    @Override
    public boolean enableBiometricAuth(User user, String biometricData) {
        try {
            // In a real implementation, this would store encrypted biometric data
            // For now, we'll simulate the process
            user.setTotpEnabled(true); // Using TOTP as a placeholder for biometric
            return true;
        } catch (Exception e) {
            System.err.println("Failed to enable biometric auth: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean verifyBiometricAuth(User user, String biometricData) {
        try {
            // In a real implementation, this would verify against stored biometric data
            // For now, we'll simulate verification
            return user.getTotpEnabled() != null && user.getTotpEnabled();
        } catch (Exception e) {
            System.err.println("Failed to verify biometric auth: " + e.getMessage());
            return false;
        }
    }

    @Override
    public PhotoVerification uploadPhotoVerification(
            Long shipmentId, 
            Long driverId, 
            String verificationType,
            MultipartFile photo,
            Double latitude,
            Double longitude,
            String address) {
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = photo.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = "verification_" + System.currentTimeMillis() + fileExtension;
            
            // Save photo
            Path filePath = uploadPath.resolve(filename);
            Files.copy(photo.getInputStream(), filePath);
            
            // Create thumbnail (simplified - in production, use image processing library)
            String thumbnailFilename = "thumb_" + filename;
            Path thumbnailPath = uploadPath.resolve(thumbnailFilename);
            Files.copy(photo.getInputStream(), thumbnailPath);
            
            // Create photo verification record
            PhotoVerification verification = new PhotoVerification();
            verification.setShipmentId(shipmentId);
            verification.setDriverId(driverId);
            verification.setVerificationType(verificationType);
            verification.setPhotoUrl("/uploads/verifications/" + filename);
            verification.setThumbnailUrl("/uploads/verifications/" + thumbnailFilename);
            verification.setLatitude(latitude);
            verification.setLongitude(longitude);
            verification.setAddress(address);
            verification.setVerified(false);
            verification.setCreatedAt(LocalDateTime.now());
            
            return photoVerificationRepository.save(verification);
            
        } catch (IOException e) {
            System.err.println("Failed to upload photo verification: " + e.getMessage());
            throw new RuntimeException("Failed to upload photo", e);
        }
    }

    @Override
    public boolean verifyPhoto(PhotoVerification photoVerification, String verificationNotes) {
        try {
            photoVerification.setVerified(true);
            photoVerification.setVerificationNotes(verificationNotes);
            photoVerification.setVerifiedAt(LocalDateTime.now());
            photoVerification.setVerifiedBy(1L); // Admin user ID
            
            photoVerificationRepository.save(photoVerification);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to verify photo: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<PhotoVerification> getPhotoVerifications(Long shipmentId) {
        return photoVerificationRepository.findByShipmentId(shipmentId);
    }

    @Override
    public String captureDigitalSignature(Long shipmentId, String signatureData, String signerName, String signerEmail) {
        try {
            // In a real implementation, this would:
            // 1. Validate signature data
            // 2. Store signature securely
            // 3. Generate signature ID
            // 4. Send confirmation email
            
            String signatureId = "SIG_" + System.currentTimeMillis();
            
            // Store signature data (simplified)
            System.out.println("Digital signature captured for shipment " + shipmentId);
            System.out.println("Signer: " + signerName + " (" + signerEmail + ")");
            System.out.println("Signature ID: " + signatureId);
            
            return signatureId;
        } catch (Exception e) {
            System.err.println("Failed to capture digital signature: " + e.getMessage());
            throw new RuntimeException("Failed to capture signature", e);
        }
    }

    @Override
    public boolean verifyDigitalSignature(String signatureId) {
        try {
            // In a real implementation, this would verify the signature against stored data
            // For now, we'll simulate verification
            return signatureId != null && signatureId.startsWith("SIG_");
        } catch (Exception e) {
            System.err.println("Failed to verify digital signature: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getAvailableSecurityFeatures(User user) {
        List<String> features = new ArrayList<>();
        
        // Basic features for all users
        features.add("TWO_FACTOR_AUTH");
        features.add("SECURE_DOCUMENTS");
        
        // Premium features for business+ users
        if (user.getCustomerTier() != null && 
            (user.getCustomerTier().name().equals("BUSINESS") || 
             user.getCustomerTier().name().equals("ENTERPRISE") || 
             user.getCustomerTier().name().equals("PREMIUM"))) {
            features.add("BIOMETRIC_AUTH");
            features.add("PHOTO_VERIFICATION");
            features.add("DIGITAL_SIGNATURE");
            features.add("FRAUD_DETECTION");
        }
        
        return features;
    }

    @Override
    public boolean hasSecurityFeature(User user, String feature) {
        List<String> availableFeatures = getAvailableSecurityFeatures(user);
        return availableFeatures.contains(feature);
    }

    @Override
    public Map<String, Object> generateSecurityReport(User user) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("userId", user.getId());
        report.put("userName", user.getFirstName() + " " + user.getLastName());
        report.put("tier", user.getCustomerTier().name());
        report.put("availableFeatures", getAvailableSecurityFeatures(user));
        report.put("twoFactorEnabled", user.getTotpEnabled());
        report.put("biometricEnabled", hasSecurityFeature(user, "BIOMETRIC_AUTH"));
        report.put("photoVerificationEnabled", hasSecurityFeature(user, "PHOTO_VERIFICATION"));
        report.put("digitalSignatureEnabled", hasSecurityFeature(user, "DIGITAL_SIGNATURE"));
        report.put("fraudDetectionEnabled", hasSecurityFeature(user, "FRAUD_DETECTION"));
        report.put("generatedAt", LocalDateTime.now());
        
        return report;
    }

    @Override
    public boolean enableFraudDetection(User user) {
        try {
            // In a real implementation, this would enable AI-powered fraud detection
            // For now, we'll simulate enabling the feature
            System.out.println("Fraud detection enabled for user: " + user.getEmail());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to enable fraud detection: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> checkSuspiciousActivity(User user, String activity) {
        Map<String, Object> result = new HashMap<>();
        
        // Simplified fraud detection logic
        boolean isSuspicious = false;
        List<String> riskFactors = new ArrayList<>();
        
        // Check for suspicious patterns
        if (activity.contains("multiple_failed_logins")) {
            isSuspicious = true;
            riskFactors.add("Multiple failed login attempts");
        }
        
        if (activity.contains("unusual_location")) {
            isSuspicious = true;
            riskFactors.add("Login from unusual location");
        }
        
        if (activity.contains("rapid_shipments")) {
            isSuspicious = true;
            riskFactors.add("Unusually high number of shipments");
        }
        
        result.put("isSuspicious", isSuspicious);
        result.put("riskFactors", riskFactors);
        result.put("riskScore", isSuspicious ? 75 : 25);
        result.put("recommendation", isSuspicious ? 
            "Review account activity and consider additional verification" : 
            "Account activity appears normal");
        
        return result;
    }
}


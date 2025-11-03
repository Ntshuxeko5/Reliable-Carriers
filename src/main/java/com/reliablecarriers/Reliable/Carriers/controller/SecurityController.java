package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.PhotoVerification;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/security")
public class SecurityController {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AuthService authService;

    /**
     * Enable biometric authentication
     */
    @PostMapping("/biometric/enable")
    public ResponseEntity<Map<String, Object>> enableBiometricAuth(@RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            String biometricData = request.get("biometricData");
            if (biometricData == null || biometricData.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Biometric data is required"
                ));
            }

            boolean success = securityService.enableBiometricAuth(currentUser, biometricData);
            
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("success", true);
                response.put("message", "Biometric authentication enabled successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to enable biometric authentication");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to enable biometric authentication: " + e.getMessage()
            ));
        }
    }

    /**
     * Verify biometric authentication
     */
    @PostMapping("/biometric/verify")
    public ResponseEntity<Map<String, Object>> verifyBiometricAuth(@RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            String biometricData = request.get("biometricData");
            if (biometricData == null || biometricData.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Biometric data is required"
                ));
            }

            boolean verified = securityService.verifyBiometricAuth(currentUser, biometricData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("verified", verified);
            response.put("message", verified ? "Biometric authentication successful" : "Biometric authentication failed");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to verify biometric authentication: " + e.getMessage()
            ));
        }
    }

    /**
     * Upload photo verification
     */
    @PostMapping("/photo/upload")
    public ResponseEntity<Map<String, Object>> uploadPhotoVerification(
            @RequestParam("shipmentId") Long shipmentId,
            @RequestParam("driverId") Long driverId,
            @RequestParam("verificationType") String verificationType,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "address", required = false) String address) {
        
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!securityService.hasSecurityFeature(currentUser, "PHOTO_VERIFICATION")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Photo verification not available for your tier"
                ));
            }

            if (photo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Photo is required"
                ));
            }

            PhotoVerification verification = securityService.uploadPhotoVerification(
                shipmentId, driverId, verificationType, photo, latitude, longitude, address
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("verificationId", verification.getId());
            response.put("photoUrl", verification.getPhotoUrl());
            response.put("thumbnailUrl", verification.getThumbnailUrl());
            response.put("message", "Photo uploaded successfully for verification");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to upload photo: " + e.getMessage()
            ));
        }
    }

    /**
     * Verify uploaded photo
     */
    @PostMapping("/photo/verify/{verificationId}")
    public ResponseEntity<Map<String, Object>> verifyPhoto(
            @PathVariable Long verificationId,
            @RequestBody Map<String, String> request) {
        
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            String verificationNotes = request.get("verificationNotes");
            
            // Get photo verification (simplified - in production, would fetch from DB)
            PhotoVerification verification = new PhotoVerification();
            verification.setId(verificationId);
            
            boolean success = securityService.verifyPhoto(verification, verificationNotes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Photo verified successfully" : "Failed to verify photo");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to verify photo: " + e.getMessage()
            ));
        }
    }

    /**
     * Capture digital signature
     */
    @PostMapping("/signature/capture")
    public ResponseEntity<Map<String, Object>> captureDigitalSignature(@RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!securityService.hasSecurityFeature(currentUser, "DIGITAL_SIGNATURE")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Digital signature not available for your tier"
                ));
            }

            Long shipmentId = Long.valueOf(request.get("shipmentId"));
            String signatureData = request.get("signatureData");
            String signerName = request.get("signerName");
            String signerEmail = request.get("signerEmail");
            
            if (signatureData == null || signatureData.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Signature data is required"
                ));
            }

            String signatureId = securityService.captureDigitalSignature(
                shipmentId, signatureData, signerName, signerEmail
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("signatureId", signatureId);
            response.put("message", "Digital signature captured successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to capture digital signature: " + e.getMessage()
            ));
        }
    }

    /**
     * Get available security features
     */
    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getSecurityFeatures() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            List<String> features = securityService.getAvailableSecurityFeatures(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("features", features);
            response.put("tier", currentUser.getCustomerTier().name());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get security features: " + e.getMessage()
            ));
        }
    }

    /**
     * Get security report
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> getSecurityReport() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            Map<String, Object> report = securityService.generateSecurityReport(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("report", report);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to generate security report: " + e.getMessage()
            ));
        }
    }

    /**
     * Enable fraud detection
     */
    @PostMapping("/fraud-detection/enable")
    public ResponseEntity<Map<String, Object>> enableFraudDetection() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            if (!securityService.hasSecurityFeature(currentUser, "FRAUD_DETECTION")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Fraud detection not available for your tier"
                ));
            }

            boolean success = securityService.enableFraudDetection(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Fraud detection enabled successfully" : "Failed to enable fraud detection");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to enable fraud detection: " + e.getMessage()
            ));
        }
    }

    /**
     * Check suspicious activity
     */
    @PostMapping("/fraud-detection/check")
    public ResponseEntity<Map<String, Object>> checkSuspiciousActivity(@RequestBody Map<String, String> request) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "User not authenticated"
                ));
            }

            String activity = request.get("activity");
            if (activity == null || activity.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Activity description is required"
                ));
            }

            Map<String, Object> result = securityService.checkSuspiciousActivity(currentUser, activity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("result", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to check suspicious activity: " + e.getMessage()
            ));
        }
    }
}
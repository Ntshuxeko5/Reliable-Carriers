package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/profile")
public class CustomerProfileController {

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User email not found"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Return profile data without sensitive information
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("email", user.getEmail());
            profile.put("phone", user.getPhone());
            profile.put("address", user.getAddress());
            profile.put("city", user.getCity());
            profile.put("state", user.getState());
            profile.put("zipCode", user.getZipCode());
            profile.put("country", user.getCountry());
            profile.put("insurancePreference", user.getInsurancePreference());
            profile.put("profilePicture", user.getProfilePicture());
            profile.put("createdAt", user.getCreatedAt());
            profile.put("updatedAt", user.getUpdatedAt());
            
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> profileData, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User email not found"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Update allowed fields
            if (profileData.containsKey("firstName")) {
                user.setFirstName((String) profileData.get("firstName"));
            }
            if (profileData.containsKey("lastName")) {
                user.setLastName((String) profileData.get("lastName"));
            }
            if (profileData.containsKey("phone")) {
                user.setPhone((String) profileData.get("phone"));
            }
            if (profileData.containsKey("address")) {
                user.setAddress((String) profileData.get("address"));
            }
            if (profileData.containsKey("city")) {
                user.setCity((String) profileData.get("city"));
            }
            if (profileData.containsKey("state")) {
                user.setState((String) profileData.get("state"));
            }
            if (profileData.containsKey("zipCode")) {
                user.setZipCode((String) profileData.get("zipCode"));
            }
            if (profileData.containsKey("country")) {
                user.setCountry((String) profileData.get("country"));
            }
            if (profileData.containsKey("insurancePreference")) {
                user.setInsurancePreference((String) profileData.get("insurancePreference"));
            }
            
            // Save updated user
            User updatedUser = userService.updateUser(user.getId(), user);
            
            // Return updated profile
            Map<String, Object> updatedProfile = new HashMap<>();
            updatedProfile.put("id", updatedUser.getId());
            updatedProfile.put("firstName", updatedUser.getFirstName());
            updatedProfile.put("lastName", updatedUser.getLastName());
            updatedProfile.put("email", updatedUser.getEmail());
            updatedProfile.put("phone", updatedUser.getPhone());
            updatedProfile.put("address", updatedUser.getAddress());
            updatedProfile.put("city", updatedUser.getCity());
            updatedProfile.put("state", updatedUser.getState());
            updatedProfile.put("zipCode", updatedUser.getZipCode());
            updatedProfile.put("country", updatedUser.getCountry());
            updatedProfile.put("insurancePreference", updatedUser.getInsurancePreference());
            updatedProfile.put("createdAt", updatedUser.getCreatedAt());
            updatedProfile.put("updatedAt", updatedUser.getUpdatedAt());
            
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User email not found"));
            }
            
            String currentPassword = passwordData.get("currentPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password and new password are required"));
            }
            
            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters long"));
            }
            
            // Verify current password and update
            boolean success = userService.changePassword(email, currentPassword, newPassword);
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("profilePicture") MultipartFile file, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User email not found"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please select a file"));
            }
            
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please upload an image file"));
            }
            
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
                return ResponseEntity.badRequest().body(Map.of("error", "File size must be less than 5MB"));
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
            String filename = "profile_" + user.getId() + "_" + UUID.randomUUID().toString() + fileExtension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user profile picture URL
            String profilePictureUrl = "/uploads/" + filename;
            user.setProfilePicture(profilePictureUrl);
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Profile picture uploaded successfully",
                "profilePictureUrl", profilePictureUrl
            ));
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Debug endpoint to test profile functionality
    @GetMapping("/debug")
    public ResponseEntity<?> debugProfile(Authentication authentication) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            if (authentication == null) {
                debugInfo.put("authentication", "null");
            } else {
                debugInfo.put("authentication", "present");
                debugInfo.put("authenticated", authentication.isAuthenticated());
                debugInfo.put("name", authentication.getName());
                debugInfo.put("authorities", authentication.getAuthorities().toString());
            }
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Simple test endpoint without profilePicture field
    @GetMapping("/test")
    public ResponseEntity<?> testProfile(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User email not found"));
            }
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // Return basic profile data without profilePicture
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("email", user.getEmail());
            profile.put("phone", user.getPhone());
            profile.put("address", user.getAddress());
            profile.put("city", user.getCity());
            profile.put("state", user.getState());
            profile.put("zipCode", user.getZipCode());
            profile.put("country", user.getCountry());
            profile.put("insurancePreference", user.getInsurancePreference());
            // Skip profilePicture for now to test if that's the issue
            profile.put("createdAt", user.getCreatedAt());
            profile.put("updatedAt", user.getUpdatedAt());
            
            return ResponseEntity.ok(profile);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

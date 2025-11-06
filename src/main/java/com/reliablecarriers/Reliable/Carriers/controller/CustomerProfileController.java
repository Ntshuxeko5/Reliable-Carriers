package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.CustomerAddress;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.CustomerAddressRepository;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
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
    
    @Autowired
    private CustomerAddressRepository customerAddressRepository;

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
            
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
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
    
    // ========== Saved Addresses Endpoints ==========
    
    @GetMapping("/addresses")
    public ResponseEntity<?> getSavedAddresses(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<CustomerAddress> addresses = customerAddressRepository
                    .findByCustomerAndIsActiveTrueOrderByIsDefaultDescCreatedAtDesc(user);
            
            List<Map<String, Object>> addressList = addresses.stream().map(addr -> {
                Map<String, Object> addrMap = new HashMap<>();
                addrMap.put("id", addr.getId());
                addrMap.put("label", addr.getLabel());
                addrMap.put("addressLine1", addr.getAddressLine1());
                addrMap.put("addressLine2", addr.getAddressLine2());
                addrMap.put("city", addr.getCity());
                addrMap.put("state", addr.getState());
                addrMap.put("zipCode", addr.getZipCode());
                addrMap.put("country", addr.getCountry());
                addrMap.put("contactPhone", addr.getContactPhone());
                addrMap.put("contactName", addr.getContactName());
                addrMap.put("latitude", addr.getLatitude());
                addrMap.put("longitude", addr.getLongitude());
                addrMap.put("placeId", addr.getPlaceId());
                addrMap.put("isDefault", addr.getIsDefault());
                addrMap.put("fullAddress", addr.getFullAddress());
                return addrMap;
            }).collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(addressList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/addresses")
    public ResponseEntity<?> saveAddress(@RequestBody Map<String, Object> addressData, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CustomerAddress address = new CustomerAddress();
            address.setCustomer(user);
            address.setLabel((String) addressData.getOrDefault("label", "Address"));
            address.setAddressLine1((String) addressData.get("addressLine1"));
            address.setAddressLine2((String) addressData.get("addressLine2"));
            address.setCity((String) addressData.get("city"));
            address.setState((String) addressData.get("state"));
            address.setZipCode((String) addressData.get("zipCode"));
            address.setCountry((String) addressData.getOrDefault("country", "South Africa"));
            address.setContactPhone((String) addressData.get("contactPhone"));
            address.setContactName((String) addressData.get("contactName"));
            
            if (addressData.get("latitude") != null) {
                address.setLatitude(Double.parseDouble(addressData.get("latitude").toString()));
            }
            if (addressData.get("longitude") != null) {
                address.setLongitude(Double.parseDouble(addressData.get("longitude").toString()));
            }
            address.setPlaceId((String) addressData.get("placeId"));
            
            // If this is set as default, unset other defaults
            Boolean isDefault = (Boolean) addressData.getOrDefault("isDefault", false);
            if (isDefault) {
                customerAddressRepository.findByCustomerAndIsDefaultTrueAndIsActiveTrue(user)
                        .ifPresent(addr -> {
                            addr.setIsDefault(false);
                            customerAddressRepository.save(addr);
                        });
            }
            address.setIsDefault(isDefault);
            
            CustomerAddress savedAddress = customerAddressRepository.save(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedAddress.getId());
            response.put("label", savedAddress.getLabel());
            response.put("fullAddress", savedAddress.getFullAddress());
            response.put("message", "Address saved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/addresses/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody Map<String, Object> addressData, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CustomerAddress address = customerAddressRepository.findByCustomerAndIdAndIsActiveTrue(user, id)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            
            if (addressData.containsKey("label")) {
                address.setLabel((String) addressData.get("label"));
            }
            if (addressData.containsKey("addressLine1")) {
                address.setAddressLine1((String) addressData.get("addressLine1"));
            }
            if (addressData.containsKey("addressLine2")) {
                address.setAddressLine2((String) addressData.get("addressLine2"));
            }
            if (addressData.containsKey("city")) {
                address.setCity((String) addressData.get("city"));
            }
            if (addressData.containsKey("state")) {
                address.setState((String) addressData.get("state"));
            }
            if (addressData.containsKey("zipCode")) {
                address.setZipCode((String) addressData.get("zipCode"));
            }
            if (addressData.containsKey("country")) {
                address.setCountry((String) addressData.get("country"));
            }
            if (addressData.containsKey("contactPhone")) {
                address.setContactPhone((String) addressData.get("contactPhone"));
            }
            if (addressData.containsKey("contactName")) {
                address.setContactName((String) addressData.get("contactName"));
            }
            if (addressData.containsKey("latitude")) {
                address.setLatitude(Double.parseDouble(addressData.get("latitude").toString()));
            }
            if (addressData.containsKey("longitude")) {
                address.setLongitude(Double.parseDouble(addressData.get("longitude").toString()));
            }
            if (addressData.containsKey("placeId")) {
                address.setPlaceId((String) addressData.get("placeId"));
            }
            if (addressData.containsKey("isDefault")) {
                Boolean isDefault = (Boolean) addressData.get("isDefault");
                if (isDefault) {
                    customerAddressRepository.findByCustomerAndIsDefaultTrueAndIsActiveTrue(user)
                            .ifPresent(addr -> {
                                if (!addr.getId().equals(id)) {
                                    addr.setIsDefault(false);
                                    customerAddressRepository.save(addr);
                                }
                            });
                }
                address.setIsDefault(isDefault);
            }
            
            CustomerAddress updatedAddress = customerAddressRepository.save(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedAddress.getId());
            response.put("label", updatedAddress.getLabel());
            response.put("fullAddress", updatedAddress.getFullAddress());
            response.put("message", "Address updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            CustomerAddress address = customerAddressRepository.findByCustomerAndIdAndIsActiveTrue(user, id)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Soft delete
            address.setIsActive(false);
            customerAddressRepository.save(address);
            
            return ResponseEntity.ok(Map.of("message", "Address deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

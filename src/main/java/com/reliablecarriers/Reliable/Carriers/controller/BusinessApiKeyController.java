package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.ApiKey;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business API Key Management Controller
 * Allows businesses to generate and manage their API keys
 */
@RestController
@RequestMapping("/api/business/keys")
@CrossOrigin(origins = "*")
public class BusinessApiKeyController {
    
    @Autowired
    private ApiKeyService apiKeyService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Generate a new API key
     * POST /api/business/keys
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> generateApiKey(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            String keyName = request.get("keyName");
            String description = request.get("description");
            Integer rateLimit = request.containsKey("rateLimit") ? 
                Integer.parseInt(request.get("rateLimit")) : 1000;
            
            ApiKeyService.ApiKeyResponse response = apiKeyService.generateApiKey(
                businessUser, keyName, description, rateLimit
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "API key generated successfully. Save this key - it will not be shown again!",
                "data", Map.of(
                    "apiKey", response.getApiKey(),
                    "apiKeyId", response.getApiKeyId(),
                    "keyName", response.getKeyName(),
                    "expiresAt", response.getExpiresAt()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get all API keys for the business
     * GET /api/business/keys
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getApiKeys(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            List<ApiKey> apiKeys = apiKeyService.getUserApiKeys(businessUser);
            
            List<Map<String, Object>> keysData = apiKeys.stream()
                .map(key -> {
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("id", key.getId());
                    keyData.put("keyName", key.getKeyName() != null ? key.getKeyName() : "Unnamed");
                    keyData.put("description", key.getDescription() != null ? key.getDescription() : "");
                    keyData.put("status", key.getStatus().toString());
                    keyData.put("lastUsedAt", key.getLastUsedAt() != null ? key.getLastUsedAt() : "");
                    keyData.put("expiresAt", key.getExpiresAt() != null ? key.getExpiresAt() : "");
                    keyData.put("rateLimit", key.getRateLimit());
                    keyData.put("requestsCount", key.getRequestsCount());
                    keyData.put("createdAt", key.getCreatedAt());
                    return keyData;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", keysData
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Revoke an API key
     * DELETE /api/business/keys/{keyId}
     */
    @DeleteMapping("/{keyId}")
    public ResponseEntity<Map<String, Object>> revokeApiKey(
            @PathVariable Long keyId,
            Authentication authentication) {
        
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            boolean revoked = apiKeyService.revokeApiKey(keyId, businessUser);
            
            if (revoked) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "API key revoked successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to revoke API key"
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Helper method to get authenticated business user
     */
    private User getAuthenticatedBusinessUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new SecurityException("API key management is only available for business accounts");
        }
        
        return user;
    }
}


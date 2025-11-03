package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.ApiKey;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business API Key Management Web Controller
 * Handles web UI for API key management (uses session authentication, not API key)
 */
@Controller
@RequestMapping("/customer/api-keys")
public class BusinessApiKeyWebController {
    
    @Autowired
    private ApiKeyService apiKeyService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Generate API key via web interface
     * POST /customer/api-keys/generate
     */
    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateApiKey(
            @RequestParam(required = false) String keyName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "1000") Integer rateLimit,
            HttpSession session) {
        
        try {
            String email = (String) session.getAttribute("userEmail");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Not authenticated"
                ));
            }
            
            User businessUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("User not found"));
            
            if (businessUser.getIsBusiness() == null || !businessUser.getIsBusiness()) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "API keys are only available for business accounts"
                ));
            }
            
            if (businessUser.getBusinessVerificationStatus() == null || 
                !businessUser.getBusinessVerificationStatus().isVerified()) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "Your business account must be verified before generating API keys"
                ));
            }
            
            ApiKeyService.ApiKeyResponse response = apiKeyService.generateApiKey(
                businessUser, keyName, description, rateLimit
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "API key generated successfully. Save this key - it will not be shown again!",
                "apiKey", response.getApiKey(),
                "apiKeyId", response.getApiKeyId(),
                "keyName", response.getKeyName(),
                "expiresAt", response.getExpiresAt()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get API keys via web interface
     * GET /customer/api-keys/list
     */
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApiKeys(HttpSession session) {
        try {
            String email = (String) session.getAttribute("userEmail");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Not authenticated"
                ));
            }
            
            User businessUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("User not found"));
            
            List<ApiKey> apiKeys = apiKeyService.getUserApiKeys(businessUser);
            
            List<Map<String, Object>> keysData = apiKeys.stream()
                .map(key -> {
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("id", key.getId());
                    keyData.put("keyName", key.getKeyName() != null ? key.getKeyName() : "Unnamed");
                    keyData.put("description", key.getDescription() != null ? key.getDescription() : "");
                    keyData.put("status", key.getStatus().toString());
                    keyData.put("lastUsedAt", key.getLastUsedAt());
                    keyData.put("expiresAt", key.getExpiresAt());
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
     * Revoke API key via web interface
     * DELETE /customer/api-keys/{keyId}
     */
    @DeleteMapping("/{keyId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> revokeApiKey(
            @PathVariable Long keyId,
            HttpSession session) {
        
        try {
            String email = (String) session.getAttribute("userEmail");
            if (email == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Not authenticated"
                ));
            }
            
            User businessUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("User not found"));
            
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
}


package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.ApiKey;
import com.reliablecarriers.Reliable.Carriers.model.User;
import java.util.Date;
import java.util.List;

public interface ApiKeyService {
    
    /**
     * Generate a new API key for a business user
     */
    ApiKeyResponse generateApiKey(User user, String keyName, String description, Integer rateLimit);
    
    /**
     * Revoke an API key
     */
    boolean revokeApiKey(Long apiKeyId, User user);
    
    /**
     * Get all API keys for a user
     */
    List<ApiKey> getUserApiKeys(User user);
    
    /**
     * Validate API key and return the associated user
     */
    User validateApiKey(String apiKey);
    
    /**
     * Update API key usage statistics
     */
    void recordApiKeyUsage(String apiKeyHash);
    
    /**
     * Check if API key has exceeded rate limit
     */
    boolean isRateLimitExceeded(String apiKeyHash);
    
    /**
     * Record detailed API key usage for enhanced rate limiting
     */
    void recordApiKeyUsageDetailed(String apiKeyHash, String endpoint, String method, String ipAddress, Integer responseStatus, Long responseTimeMs);
    
    /**
     * API Key Response DTO
     */
    class ApiKeyResponse {
        private String apiKey;
        private Long apiKeyId;
        private String keyName;
        private Date expiresAt;
        
        public ApiKeyResponse(String apiKey, Long apiKeyId, String keyName, Date expiresAt) {
            this.apiKey = apiKey;
            this.apiKeyId = apiKeyId;
            this.keyName = keyName;
            this.expiresAt = expiresAt;
        }
        
        // Getters
        public String getApiKey() { return apiKey; }
        public Long getApiKeyId() { return apiKeyId; }
        public String getKeyName() { return keyName; }
        public Date getExpiresAt() { return expiresAt; }
    }
}


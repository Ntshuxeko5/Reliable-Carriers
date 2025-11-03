package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.ApiKey;
import com.reliablecarriers.Reliable.Carriers.model.ApiKeyStatus;
import com.reliablecarriers.Reliable.Carriers.model.ApiKeyUsageLog;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.ApiKeyRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ApiKeyUsageLogRepository;
import com.reliablecarriers.Reliable.Carriers.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private ApiKeyUsageLogRepository usageLogRepository;
    
    private static final SecureRandom random = new SecureRandom();
    private static final int API_KEY_LENGTH = 32; // Bytes
    
    @Override
    @Transactional
    public ApiKeyResponse generateApiKey(User user, String keyName, String description, Integer rateLimit) {
        // Check if user is a business account
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new IllegalArgumentException("API keys can only be generated for business accounts");
        }
        
        // Check if business is verified
        if (user.getBusinessVerificationStatus() == null || 
            !user.getBusinessVerificationStatus().isVerified()) {
            throw new IllegalArgumentException("Business must be verified before generating API keys");
        }
        
        // Generate API key
        byte[] keyBytes = new byte[API_KEY_LENGTH];
        random.nextBytes(keyBytes);
        String apiKey = "rc_" + Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
        
        // Hash the API key for storage (SHA-256)
        String apiKeyHash = hashApiKey(apiKey);
        
        // Check for duplicates (extremely unlikely but safe)
        if (apiKeyRepository.existsByApiKeyHash(apiKeyHash)) {
            // Retry once
            random.nextBytes(keyBytes);
            apiKey = "rc_" + Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
            apiKeyHash = hashApiKey(apiKey);
        }
        
        // Set expiration (1 year from now)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date expiresAt = cal.getTime();
        
        // Create and save API key
        ApiKey apiKeyEntity = new ApiKey();
        apiKeyEntity.setApiKey(apiKey); // Store plain text only for initial response
        apiKeyEntity.setApiKeyHash(apiKeyHash);
        apiKeyEntity.setUser(user);
        apiKeyEntity.setKeyName(keyName != null ? keyName : "API Key " + new Date());
        apiKeyEntity.setDescription(description);
        apiKeyEntity.setStatus(ApiKeyStatus.ACTIVE);
        apiKeyEntity.setRateLimit(rateLimit != null ? rateLimit : 1000);
        apiKeyEntity.setExpiresAt(expiresAt);
        
        ApiKey saved = apiKeyRepository.save(apiKeyEntity);
        
        // Return response with plain text key (only shown once)
        return new ApiKeyResponse(apiKey, saved.getId(), saved.getKeyName(), expiresAt);
    }
    
    @Override
    @Transactional
    public boolean revokeApiKey(Long apiKeyId, User user) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
            .orElseThrow(() -> new IllegalArgumentException("API key not found"));
        
        // Verify ownership
        if (!apiKey.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to revoke this API key");
        }
        
        apiKey.setStatus(ApiKeyStatus.REVOKED);
        apiKeyRepository.save(apiKey);
        return true;
    }
    
    @Override
    public List<ApiKey> getUserApiKeys(User user) {
        return apiKeyRepository.findByUser(user);
    }
    
    @Override
    public User validateApiKey(String apiKey) {
        if (apiKey == null || !apiKey.startsWith("rc_")) {
            return null;
        }
        
        String apiKeyHash = hashApiKey(apiKey);
        ApiKey keyEntity = apiKeyRepository.findByApiKeyHash(apiKeyHash)
            .orElse(null);
        
        if (keyEntity == null) {
            return null;
        }
        
        // Check if key is active
        if (keyEntity.getStatus() != ApiKeyStatus.ACTIVE) {
            return null;
        }
        
        // Check if key is expired
        if (keyEntity.getExpiresAt() != null && keyEntity.getExpiresAt().before(new Date())) {
            keyEntity.setStatus(ApiKeyStatus.EXPIRED);
            apiKeyRepository.save(keyEntity);
            return null;
        }
        
        // Check rate limit
        if (isRateLimitExceeded(apiKeyHash)) {
            return null;
        }
        
        // Record usage
        recordApiKeyUsage(apiKeyHash);
        
        return keyEntity.getUser();
    }
    
    @Override
    @Transactional
    public void recordApiKeyUsage(String apiKeyHash) {
        ApiKey apiKey = apiKeyRepository.findByApiKeyHash(apiKeyHash).orElse(null);
        if (apiKey != null) {
            apiKey.setRequestsCount(apiKey.getRequestsCount() + 1);
            apiKey.setLastUsedAt(new Date());
            apiKeyRepository.save(apiKey);
        }
    }
    
    /**
     * Record detailed API key usage for enhanced rate limiting
     */
    @Transactional
    public void recordApiKeyUsageDetailed(String apiKeyHash, String endpoint, String method, String ipAddress, Integer responseStatus, Long responseTimeMs) {
        // Update API key statistics
        recordApiKeyUsage(apiKeyHash);
        
        // Create usage log entry
        ApiKeyUsageLog log = new ApiKeyUsageLog();
        log.setApiKeyHash(apiKeyHash);
        log.setEndpoint(endpoint);
        log.setMethod(method);
        log.setIpAddress(ipAddress);
        log.setResponseStatus(responseStatus);
        log.setResponseTimeMs(responseTimeMs);
        usageLogRepository.save(log);
    }
    
    @Override
    public boolean isRateLimitExceeded(String apiKeyHash) {
        ApiKey apiKey = apiKeyRepository.findByApiKeyHash(apiKeyHash).orElse(null);
        if (apiKey == null) {
            return true;
        }
        
        // Enhanced rate limiting: check requests in the last hour using usage logs
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        Date oneHourAgo = cal.getTime();
        
        long requestsInLastHour = usageLogRepository.countByApiKeyHashSince(apiKeyHash, oneHourAgo);
        
        // Fallback to simple count if usage logs not available
        if (requestsInLastHour == 0) {
            // Use simple rate limiting as fallback
            return apiKey.getRequestsCount() >= apiKey.getRateLimit();
        }
        
        return requestsInLastHour >= apiKey.getRateLimit();
    }
    
    /**
     * Hash API key using SHA-256
     */
    private String hashApiKey(String apiKey) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }
}


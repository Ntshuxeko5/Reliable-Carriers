package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * API Key model for business API access
 * Allows businesses to integrate with their own systems
 */
@Entity
@Table(name = "api_keys")
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "api_key", unique = true, nullable = false, length = 64)
    private String apiKey; // Hashed API key
    
    @Column(name = "api_key_hash", unique = true, nullable = false, length = 128)
    private String apiKeyHash; // SHA-256 hash of the API key
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Business user
    
    @Column(name = "key_name", length = 100)
    private String keyName; // Friendly name for the key
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApiKeyStatus status = ApiKeyStatus.ACTIVE;
    
    @Column(name = "last_used_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsedAt;
    
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;
    
    @Column(name = "rate_limit", nullable = false)
    private Integer rateLimit = 1000; // Requests per hour
    
    @Column(name = "requests_count", nullable = false)
    private Long requestsCount = 0L;
    
    @Column(name = "allowed_ips", length = 1000)
    private String allowedIps; // Comma-separated list of allowed IPs (optional)
    
    @Column(name = "permissions", length = 500)
    private String permissions; // JSON string of allowed permissions
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiKeyHash() {
        return apiKeyHash;
    }
    
    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getKeyName() {
        return keyName;
    }
    
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ApiKeyStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApiKeyStatus status) {
        this.status = status;
    }
    
    public Date getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(Date lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public Date getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Integer getRateLimit() {
        return rateLimit;
    }
    
    public void setRateLimit(Integer rateLimit) {
        this.rateLimit = rateLimit;
    }
    
    public Long getRequestsCount() {
        return requestsCount;
    }
    
    public void setRequestsCount(Long requestsCount) {
        this.requestsCount = requestsCount;
    }
    
    public String getAllowedIps() {
        return allowedIps;
    }
    
    public void setAllowedIps(String allowedIps) {
        this.allowedIps = allowedIps;
    }
    
    public String getPermissions() {
        return permissions;
    }
    
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}


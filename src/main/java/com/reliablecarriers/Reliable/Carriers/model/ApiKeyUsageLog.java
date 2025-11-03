package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * API Key Usage Log for enhanced rate limiting
 * Tracks individual API requests with timestamps
 */
@Entity
@Table(name = "api_key_usage_logs", indexes = {
    @Index(name = "idx_api_key_hash_created", columnList = "api_key_hash,created_at")
})
public class ApiKeyUsageLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "api_key_hash", nullable = false, length = 128)
    private String apiKeyHash;
    
    @Column(name = "endpoint", length = 255)
    private String endpoint;
    
    @Column(name = "method", length = 10)
    private String method;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getApiKeyHash() { return apiKeyHash; }
    public void setApiKeyHash(String apiKeyHash) { this.apiKeyHash = apiKeyHash; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}






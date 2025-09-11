package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "integration_webhooks")
public class IntegrationWebhook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String webhookName;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, length = 500)
    private String webhookUrl;
    
    @Column(nullable = false, length = 20)
    private String integrationType; // POS, ECOMMERCE, CRM, etc.
    
    @Column(length = 100)
    private String platformName; // Toast, Lightspeed, Square, Shopify, etc.
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column(nullable = false)
    private Boolean isSecure; // Uses HTTPS
    
    @Column(length = 100)
    private String apiKey; // For authentication
    
    @Column(length = 100)
    private String secretKey; // For webhook signature verification
    
    @Column(length = 20)
    private String httpMethod; // POST, PUT, PATCH
    
    @Column(length = 20)
    private String contentType; // application/json, application/xml
    
    @Column(nullable = false)
    private Integer timeoutSeconds; // Request timeout
    
    @Column(nullable = false)
    private Integer retryAttempts; // Number of retry attempts
    
    @Column(nullable = false)
    private Integer retryDelaySeconds; // Delay between retries
    
    @Column(nullable = false)
    private Integer retryCount; // Current retry count
    
    @Column(nullable = false)
    private Integer maxRetries; // Maximum number of retries
    
    @Column(length = 20)
    private String webhookType; // Type of webhook (SHIPMENT, PAYMENT, etc.)
    
    @Column
    private Integer lastResponseCode; // Last HTTP response code
    
    @Column(length = 1000)
    private String eventTypes; // Comma-separated list of events to send
    
    @Column(length = 1000)
    private String customHeaders; // JSON string of custom headers
    
    @Column(length = 1000)
    private String payloadTemplate; // JSON template for payload
    
    @Column
    private Date lastTriggered;
    
    @Column
    private Date lastSuccessful;
    
    @Column
    private Date lastFailed;
    
    @Column(length = 500)
    private String lastError;
    
    @Column(nullable = false)
    private Long totalTriggers;
    
    @Column(nullable = false)
    private Long successfulTriggers;
    
    @Column(nullable = false)
    private Long failedTriggers;
    
    @Column(nullable = false)
    private Date createdAt;
    
    @Column(nullable = false)
    private Date updatedAt;
    
    // Constructor
    public IntegrationWebhook() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.isSecure = true;
        this.httpMethod = "POST";
        this.contentType = "application/json";
        this.timeoutSeconds = 30;
        this.retryAttempts = 3;
        this.retryDelaySeconds = 60;
        this.retryCount = 0;
        this.maxRetries = 3;
        this.webhookType = "GENERAL";
        this.totalTriggers = 0L;
        this.successfulTriggers = 0L;
        this.failedTriggers = 0L;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWebhookName() {
        return webhookName;
    }

    public void setWebhookName(String webhookName) {
        this.webhookName = webhookName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getIntegrationType() {
        return integrationType;
    }

    public void setIntegrationType(String integrationType) {
        this.integrationType = integrationType;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSecure() {
        return isSecure;
    }

    public void setIsSecure(Boolean isSecure) {
        this.isSecure = isSecure;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public Integer getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(Integer retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getWebhookType() {
        return webhookType;
    }

    public void setWebhookType(String webhookType) {
        this.webhookType = webhookType;
    }

    public Integer getLastResponseCode() {
        return lastResponseCode;
    }

    public void setLastResponseCode(Integer lastResponseCode) {
        this.lastResponseCode = lastResponseCode;
    }

    public String getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(String eventTypes) {
        this.eventTypes = eventTypes;
    }

    public String getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(String customHeaders) {
        this.customHeaders = customHeaders;
    }

    public String getPayloadTemplate() {
        return payloadTemplate;
    }

    public void setPayloadTemplate(String payloadTemplate) {
        this.payloadTemplate = payloadTemplate;
    }

    public Date getLastTriggered() {
        return lastTriggered;
    }

    public void setLastTriggered(Date lastTriggered) {
        this.lastTriggered = lastTriggered;
    }

    public Date getLastSuccessful() {
        return lastSuccessful;
    }

    public void setLastSuccessful(Date lastSuccessful) {
        this.lastSuccessful = lastSuccessful;
    }

    public Date getLastFailed() {
        return lastFailed;
    }

    public void setLastFailed(Date lastFailed) {
        this.lastFailed = lastFailed;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Long getTotalTriggers() {
        return totalTriggers;
    }

    public void setTotalTriggers(Long totalTriggers) {
        this.totalTriggers = totalTriggers;
    }

    public Long getSuccessfulTriggers() {
        return successfulTriggers;
    }

    public void setSuccessfulTriggers(Long successfulTriggers) {
        this.successfulTriggers = successfulTriggers;
    }

    public Long getFailedTriggers() {
        return failedTriggers;
    }

    public void setFailedTriggers(Long failedTriggers) {
        this.failedTriggers = failedTriggers;
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
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
    
    // Helper method to calculate success rate
    public Double getSuccessRate() {
        if (totalTriggers == 0) {
            return 0.0;
        }
        return (double) successfulTriggers / totalTriggers * 100;
    }
    
    // Helper method to check if webhook should be triggered for an event
    public boolean shouldTriggerForEvent(String eventType) {
        if (eventTypes == null || eventTypes.isEmpty()) {
            return true; // If no specific events listed, trigger for all
        }
        
        String[] events = eventTypes.split(",");
        for (String event : events) {
            if (event.trim().equalsIgnoreCase(eventType)) {
                return true;
            }
        }
        return false;
    }
}

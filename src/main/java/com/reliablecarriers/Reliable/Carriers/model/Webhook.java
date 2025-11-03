package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Webhook model for business real-time notifications
 */
@Entity
@Table(name = "webhooks")
public class Webhook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Business user
    
    @Column(name = "url", nullable = false, length = 500)
    private String url;
    
    @Column(name = "secret", length = 128)
    private String secret; // For webhook signature verification
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WebhookStatus status = WebhookStatus.ACTIVE;
    
    @Column(name = "events", length = 1000)
    private String events; // JSON array of event types to subscribe to
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "last_triggered_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastTriggeredAt;
    
    @Column(name = "success_count", nullable = false)
    private Long successCount = 0L;
    
    @Column(name = "failure_count", nullable = false)
    private Long failureCount = 0L;
    
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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public WebhookStatus getStatus() { return status; }
    public void setStatus(WebhookStatus status) { this.status = status; }
    
    public String getEvents() { return events; }
    public void setEvents(String events) { this.events = events; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Date getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(Date lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
    
    public Long getSuccessCount() { return successCount; }
    public void setSuccessCount(Long successCount) { this.successCount = successCount; }
    
    public Long getFailureCount() { return failureCount; }
    public void setFailureCount(Long failureCount) { this.failureCount = failureCount; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}






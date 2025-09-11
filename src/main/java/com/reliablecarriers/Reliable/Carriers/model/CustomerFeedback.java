package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "customer_feedback")
public class CustomerFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @Column(length = 100)
    private String customerEmail;
    
    @Column(length = 15)
    private String customerPhone;
    
    @Column(nullable = false)
    private Integer overallRating; // 1-5 stars
    
    @Column(nullable = false)
    private Integer deliverySpeedRating; // 1-5 stars
    
    @Column(nullable = false)
    private Integer driverCourtesyRating; // 1-5 stars
    
    @Column(nullable = false)
    private Integer packageConditionRating; // 1-5 stars
    
    @Column(nullable = false)
    private Integer communicationRating; // 1-5 stars
    
    @Column(length = 1000)
    private String comments;
    
    @Column(length = 20)
    private String feedbackType; // DELIVERY, PICKUP, CUSTOMER_SERVICE, etc.
    
    @Column(length = 20)
    private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
    
    @Column
    private Boolean wouldRecommend; // Would recommend to others
    
    @Column(length = 500)
    private String improvementSuggestions;
    
    @Column(length = 20)
    private String responseStatus; // PENDING, RESPONDED, RESOLVED
    
    @Column(length = 1000)
    private String adminResponse;
    
    @Column
    private Date responseDate;
    
    @Column(length = 100)
    private String respondedBy; // Admin who responded
    
    @Column
    private Long customerId; // Customer ID
    
    @Column
    private Long driverId; // Driver ID
    
    @Column
    private Integer rating; // Overall rating (1-5)
    
    @Column
    private Boolean isResolved; // Whether the feedback has been resolved
    
    @Column(nullable = false)
    private Date createdAt;
    
    @Column(nullable = false)
    private Date updatedAt;
    
    // Constructor
    public CustomerFeedback() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.responseStatus = "PENDING";
        this.isResolved = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Integer getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Integer overallRating) {
        this.overallRating = overallRating;
    }

    public Integer getDeliverySpeedRating() {
        return deliverySpeedRating;
    }

    public void setDeliverySpeedRating(Integer deliverySpeedRating) {
        this.deliverySpeedRating = deliverySpeedRating;
    }

    public Integer getDriverCourtesyRating() {
        return driverCourtesyRating;
    }

    public void setDriverCourtesyRating(Integer driverCourtesyRating) {
        this.driverCourtesyRating = driverCourtesyRating;
    }

    public Integer getPackageConditionRating() {
        return packageConditionRating;
    }

    public void setPackageConditionRating(Integer packageConditionRating) {
        this.packageConditionRating = packageConditionRating;
    }

    public Integer getCommunicationRating() {
        return communicationRating;
    }

    public void setCommunicationRating(Integer communicationRating) {
        this.communicationRating = communicationRating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public Boolean getWouldRecommend() {
        return wouldRecommend;
    }

    public void setWouldRecommend(Boolean wouldRecommend) {
        this.wouldRecommend = wouldRecommend;
    }

    public String getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(String improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getAdminResponse() {
        return adminResponse;
    }

    public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    public Date getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }

    public String getRespondedBy() {
        return respondedBy;
    }

    public void setRespondedBy(String respondedBy) {
        this.respondedBy = respondedBy;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Boolean getResolved() {
        return isResolved;
    }

    public void setResolved(Boolean resolved) {
        isResolved = resolved;
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
    
    // Helper method to calculate average rating
    public Double getAverageRating() {
        if (overallRating == null || deliverySpeedRating == null || 
            driverCourtesyRating == null || packageConditionRating == null || 
            communicationRating == null) {
            return null;
        }
        
        return (overallRating + deliverySpeedRating + driverCourtesyRating + 
                packageConditionRating + communicationRating) / 5.0;
    }
}

package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "proof_of_delivery")
public class ProofOfDelivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;
    
    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;
    
    @Column(nullable = false)
    private Date deliveryDate;
    
    @Column(nullable = false)
    private String deliveryLocation;
    
    @Column(length = 500)
    private String recipientSignature; // Base64 encoded signature
    
    @Column(length = 1000)
    private String deliveryPhotoUrl; // URL to stored delivery photo
    
    @Column(length = 1000)
    private String packagePhotoUrl; // URL to package condition photo
    
    @Column(length = 500)
    private String recipientName;
    
    @Column(length = 15)
    private String recipientPhone;
    
    @Column(length = 100)
    private String recipientIdNumber; // ID verification
    
    @Column(length = 500)
    private String deliveryNotes;
    
    @Column(length = 20)
    private String deliveryMethod; // HAND_TO_RECIPIENT, LEAVE_AT_DOOR, etc.
    
    @Column(nullable = false)
    private Boolean signatureRequired;
    
    @Column(nullable = false)
    private Boolean photoRequired;
    
    @Column(nullable = false)
    private Boolean idVerificationRequired;
    
    @Column(length = 20)
    private String deliveryStatus; // COMPLETED, FAILED, PARTIAL, etc.
    
    @Column(length = 500)
    private String failureReason; // If delivery failed
    
    @Column(nullable = false)
    private Date createdAt;
    
    @Column(nullable = false)
    private Date updatedAt;
    
    // Constructor
    public ProofOfDelivery() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
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

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getRecipientSignature() {
        return recipientSignature;
    }

    public void setRecipientSignature(String recipientSignature) {
        this.recipientSignature = recipientSignature;
    }

    public String getDeliveryPhotoUrl() {
        return deliveryPhotoUrl;
    }

    public void setDeliveryPhotoUrl(String deliveryPhotoUrl) {
        this.deliveryPhotoUrl = deliveryPhotoUrl;
    }

    public String getPackagePhotoUrl() {
        return packagePhotoUrl;
    }

    public void setPackagePhotoUrl(String packagePhotoUrl) {
        this.packagePhotoUrl = packagePhotoUrl;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientIdNumber() {
        return recipientIdNumber;
    }

    public void setRecipientIdNumber(String recipientIdNumber) {
        this.recipientIdNumber = recipientIdNumber;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public Boolean getSignatureRequired() {
        return signatureRequired;
    }

    public void setSignatureRequired(Boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }

    public Boolean getPhotoRequired() {
        return photoRequired;
    }

    public void setPhotoRequired(Boolean photoRequired) {
        this.photoRequired = photoRequired;
    }

    public Boolean getIdVerificationRequired() {
        return idVerificationRequired;
    }

    public void setIdVerificationRequired(Boolean idVerificationRequired) {
        this.idVerificationRequired = idVerificationRequired;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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
}

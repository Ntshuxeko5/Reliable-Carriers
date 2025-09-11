package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;

@Entity
@Table(name = "shipments")
public class Shipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String trackingNumber;
    
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @NotNull
    private User sender;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String recipientName;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String recipientEmail;
    
    @Column(length = 15)
    @Size(max = 15)
    private String recipientPhone;
    
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String pickupAddress;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String pickupCity;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String pickupState;
    
    @Column(nullable = false, length = 10)
    @NotBlank
    @Size(max = 10)
    private String pickupZipCode;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String pickupCountry;
    
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String deliveryAddress;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String deliveryCity;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String deliveryState;
    
    @Column(nullable = false, length = 10)
    @NotBlank
    @Size(max = 10)
    private String deliveryZipCode;
    
    @Column(nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String deliveryCountry;
    
    @Column(nullable = false)
    @NotNull
    @Positive
    private Double weight;
    
    @Column(length = 20)
    @Size(max = 20)
    private String dimensions;
    
    @Column(length = 500)
    @Size(max = 500)
    private String description;
    
    @Column(nullable = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal shippingCost;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;
    
    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date estimatedDeliveryDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date actualDeliveryDate;
    
    @ManyToOne
    @JoinColumn(name = "assigned_driver_id")
    private User assignedDriver;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
        if (status == null) {
            status = ShipmentStatus.PENDING;
        }
        if (serviceType == null) {
            serviceType = ServiceType.ECONOMY; // Default to economy service
        }
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

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getPickupCity() {
        return pickupCity;
    }

    public void setPickupCity(String pickupCity) {
        this.pickupCity = pickupCity;
    }

    public String getPickupState() {
        return pickupState;
    }

    public void setPickupState(String pickupState) {
        this.pickupState = pickupState;
    }

    public String getPickupZipCode() {
        return pickupZipCode;
    }

    public void setPickupZipCode(String pickupZipCode) {
        this.pickupZipCode = pickupZipCode;
    }

    public String getPickupCountry() {
        return pickupCountry;
    }

    public void setPickupCountry(String pickupCountry) {
        this.pickupCountry = pickupCountry;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryCity() {
        return deliveryCity;
    }

    public void setDeliveryCity(String deliveryCity) {
        this.deliveryCity = deliveryCity;
    }

    public String getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(String deliveryState) {
        this.deliveryState = deliveryState;
    }

    public String getDeliveryZipCode() {
        return deliveryZipCode;
    }

    public void setDeliveryZipCode(String deliveryZipCode) {
        this.deliveryZipCode = deliveryZipCode;
    }

    public String getDeliveryCountry() {
        return deliveryCountry;
    }

    public void setDeliveryCountry(String deliveryCountry) {
        this.deliveryCountry = deliveryCountry;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public Date getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public Date getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(Date actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public User getAssignedDriver() {
        return assignedDriver;
    }

    public void setAssignedDriver(User assignedDriver) {
        this.assignedDriver = assignedDriver;
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
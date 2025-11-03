package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "quotes")
public class Quote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String quoteId;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    @Email
    @Size(max = 100)
    private String customerEmail;
    
    @Column(nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String pickupAddress;
    
    @Column(nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String deliveryAddress;
    
    @Column(nullable = false)
    @NotNull
    @Positive
    private Double weight;
    
    @Column(length = 100)
    @Size(max = 100)
    private String dimensions;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    @Positive
    private BigDecimal totalCost;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ServiceType serviceType;
    
    @Column(length = 100)
    @Size(max = 100)
    private String estimatedDeliveryTime;
    
    @Column
    private Date estimatedDeliveryDate;
    
    @Column(nullable = false)
    @NotNull
    private Date expiryDate;
    
    @Column(nullable = false)
    @NotNull
    private Boolean isActive = true;
    
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
    
    // Additional address fields for better data storage
    @Column(length = 50)
    @Size(max = 50)
    private String pickupCity;
    
    @Column(length = 50)
    @Size(max = 50)
    private String pickupState;
    
    @Column(length = 10)
    @Size(max = 10)
    private String pickupZipCode;
    
    @Column(length = 50)
    @Size(max = 50)
    private String pickupCountry;
    
    @Column(length = 50)
    @Size(max = 50)
    private String deliveryCity;
    
    @Column(length = 50)
    @Size(max = 50)
    private String deliveryState;
    
    @Column(length = 10)
    @Size(max = 10)
    private String deliveryZipCode;
    
    @Column(length = 50)
    @Size(max = 50)
    private String deliveryCountry;
    
    // Cost breakdown fields
    @Column(precision = 10, scale = 2)
    private BigDecimal baseCost;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal serviceFee;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal insuranceFee;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal fuelSurcharge;
    
    @Column
    private Double distanceKm;
    
    // Constructors
    public Quote() {}
    
    public Quote(String quoteId, String customerEmail, String pickupAddress, String deliveryAddress, 
                Double weight, BigDecimal totalCost, ServiceType serviceType, Date expiryDate) {
        this.quoteId = quoteId;
        this.customerEmail = customerEmail;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.weight = weight;
        this.totalCost = totalCost;
        this.serviceType = serviceType;
        this.expiryDate = expiryDate;
        this.isActive = true;
        this.createdAt = new Date();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getQuoteId() {
        return quoteId;
    }
    
    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getPickupAddress() {
        return pickupAddress;
    }
    
    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }
    
    public String getDeliveryAddress() {
        return deliveryAddress;
    }
    
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }
    
    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }
    
    public Date getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
    
    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
    
    public BigDecimal getBaseCost() {
        return baseCost;
    }
    
    public void setBaseCost(BigDecimal baseCost) {
        this.baseCost = baseCost;
    }
    
    public BigDecimal getServiceFee() {
        return serviceFee;
    }
    
    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }
    
    public BigDecimal getInsuranceFee() {
        return insuranceFee;
    }
    
    public void setInsuranceFee(BigDecimal insuranceFee) {
        this.insuranceFee = insuranceFee;
    }
    
    public BigDecimal getFuelSurcharge() {
        return fuelSurcharge;
    }
    
    public void setFuelSurcharge(BigDecimal fuelSurcharge) {
        this.fuelSurcharge = fuelSurcharge;
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}

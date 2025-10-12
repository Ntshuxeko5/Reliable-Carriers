package com.reliablecarriers.Reliable.Carriers.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CustomerPackageRequest {
    
    // Sender Information (can be anonymous for quote requests)
    @NotBlank(message = "Sender name is required")
    @Size(max = 100, message = "Sender name must be less than 100 characters")
    private String senderName;
    
    @NotBlank(message = "Sender email is required")
    @Email(message = "Invalid sender email format")
    @Size(max = 100, message = "Sender email must be less than 100 characters")
    private String senderEmail;
    
    @Size(max = 15, message = "Sender phone must be less than 15 characters")
    private String senderPhone;
    
    // Pickup Address
    @NotBlank(message = "Pickup address is required")
    @Size(max = 200, message = "Pickup address must be less than 200 characters")
    private String pickupAddress;
    
    @NotBlank(message = "Pickup city is required")
    @Size(max = 50, message = "Pickup city must be less than 50 characters")
    private String pickupCity;
    
    @NotBlank(message = "Pickup state is required")
    @Size(max = 50, message = "Pickup state must be less than 50 characters")
    private String pickupState;
    
    @NotBlank(message = "Pickup zip code is required")
    @Size(max = 10, message = "Pickup zip code must be less than 10 characters")
    private String pickupZipCode;
    
    @NotBlank(message = "Pickup country is required")
    @Size(max = 50, message = "Pickup country must be less than 50 characters")
    private String pickupCountry;
    
    // Recipient Information
    @NotBlank(message = "Recipient name is required")
    @Size(max = 100, message = "Recipient name must be less than 100 characters")
    private String recipientName;
    
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid recipient email format")
    @Size(max = 100, message = "Recipient email must be less than 100 characters")
    private String recipientEmail;
    
    @Size(max = 15, message = "Recipient phone must be less than 15 characters")
    private String recipientPhone;
    
    // Delivery Address
    @NotBlank(message = "Delivery address is required")
    @Size(max = 200, message = "Delivery address must be less than 200 characters")
    private String deliveryAddress;
    
    @NotBlank(message = "Delivery city is required")
    @Size(max = 50, message = "Delivery city must be less than 50 characters")
    private String deliveryCity;
    
    @NotBlank(message = "Delivery state is required")
    @Size(max = 50, message = "Delivery state must be less than 50 characters")
    private String deliveryState;
    
    @NotBlank(message = "Delivery zip code is required")
    @Size(max = 10, message = "Delivery zip code must be less than 10 characters")
    private String deliveryZipCode;
    
    @NotBlank(message = "Delivery country is required")
    @Size(max = 50, message = "Delivery country must be less than 50 characters")
    private String deliveryCountry;
    
    // Package Information
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    @Size(max = 20, message = "Dimensions must be less than 20 characters")
    private String dimensions;
    
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;
    
    // Service Information
    private String serviceType;
    
    // Optional: For existing customers with accounts
    private Long customerId;
    
    // Optional: For store/business customers
    private String businessName;
    private String businessId;
    
    // Notification preferences
    private Boolean emailNotifications = true;
    private Boolean smsNotifications = true;

    // Optional: coordinates for distance calculation (Johannesburg/Gauteng context)
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    
    // Getters and Setters
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getSmsNotifications() {
        return smsNotifications;
    }

    public void setSmsNotifications(Boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(Double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(Double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(Double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(Double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }
}

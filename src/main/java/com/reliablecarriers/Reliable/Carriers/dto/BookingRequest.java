package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;

public class BookingRequest {
    
    @NotNull(message = "Service type is required")
    private ServiceType serviceType;
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;
    
    @NotBlank(message = "Customer phone is required")
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    private String customerPhone;
    
    @NotBlank(message = "Pickup address is required")
    @Size(max = 200, message = "Pickup address must not exceed 200 characters")
    private String pickupAddress;
    
    @NotBlank(message = "Pickup city is required")
    @Size(max = 50, message = "Pickup city must not exceed 50 characters")
    private String pickupCity;
    
    @NotBlank(message = "Pickup state is required")
    @Size(max = 50, message = "Pickup state must not exceed 50 characters")
    private String pickupState;
    
    @NotBlank(message = "Pickup postal code is required")
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String pickupPostalCode;
    
    @NotBlank(message = "Delivery address is required")
    @Size(max = 200, message = "Delivery address must not exceed 200 characters")
    private String deliveryAddress;
    
    @NotBlank(message = "Delivery city is required")
    @Size(max = 50, message = "Delivery city must not exceed 50 characters")
    private String deliveryCity;
    
    @NotBlank(message = "Delivery state is required")
    @Size(max = 50, message = "Delivery state must not exceed 50 characters")
    private String deliveryState;
    
    @NotBlank(message = "Delivery postal code is required")
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String deliveryPostalCode;
    
    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;
    
    @Size(max = 20, message = "Dimensions must not exceed 20 characters")
    private String dimensions;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Future(message = "Pickup date must be in the future")
    private Date pickupDate;
    
    @Future(message = "Delivery date must be in the future")
    private Date deliveryDate;
    
    // Additional services
    private boolean insurance = false;
    private boolean packing = false;
    private boolean saturdayDelivery = false;
    private boolean signatureRequired = false;
    
    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;
    
    // Price calculation fields
    private BigDecimal basePrice;
    private BigDecimal serviceFee;
    private BigDecimal insuranceFee;
    private BigDecimal fuelSurcharge;
    private BigDecimal totalAmount;
    
    // Pickup and delivery contact details
    @NotBlank(message = "Pickup contact name is required")
    @Size(max = 100, message = "Pickup contact name must not exceed 100 characters")
    private String pickupContactName;
    
    @NotBlank(message = "Pickup contact phone is required")
    @Size(max = 15, message = "Pickup contact phone must not exceed 15 characters")
    private String pickupContactPhone;
    
    @NotBlank(message = "Delivery contact name is required")
    @Size(max = 100, message = "Delivery contact name must not exceed 100 characters")
    private String deliveryContactName;
    
    @NotBlank(message = "Delivery contact phone is required")
    @Size(max = 15, message = "Delivery contact phone must not exceed 15 characters")
    private String deliveryContactPhone;
    
    // Constructors
    public BookingRequest() {}
    
    // Getters and Setters
    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getPickupPostalCode() {
        return pickupPostalCode;
    }

    public void setPickupPostalCode(String pickupPostalCode) {
        this.pickupPostalCode = pickupPostalCode;
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

    public String getDeliveryPostalCode() {
        return deliveryPostalCode;
    }

    public void setDeliveryPostalCode(String deliveryPostalCode) {
        this.deliveryPostalCode = deliveryPostalCode;
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

    public Date getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(Date pickupDate) {
        this.pickupDate = pickupDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public boolean isInsurance() {
        return insurance;
    }

    public void setInsurance(boolean insurance) {
        this.insurance = insurance;
    }

    public boolean isPacking() {
        return packing;
    }

    public void setPacking(boolean packing) {
        this.packing = packing;
    }

    public boolean isSaturdayDelivery() {
        return saturdayDelivery;
    }

    public void setSaturdayDelivery(boolean saturdayDelivery) {
        this.saturdayDelivery = saturdayDelivery;
    }

    public boolean isSignatureRequired() {
        return signatureRequired;
    }

    public void setSignatureRequired(boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPickupContactName() {
        return pickupContactName;
    }

    public void setPickupContactName(String pickupContactName) {
        this.pickupContactName = pickupContactName;
    }

    public String getPickupContactPhone() {
        return pickupContactPhone;
    }

    public void setPickupContactPhone(String pickupContactPhone) {
        this.pickupContactPhone = pickupContactPhone;
    }

    public String getDeliveryContactName() {
        return deliveryContactName;
    }

    public void setDeliveryContactName(String deliveryContactName) {
        this.deliveryContactName = deliveryContactName;
    }

    public String getDeliveryContactPhone() {
        return deliveryContactPhone;
    }

    public void setDeliveryContactPhone(String deliveryContactPhone) {
        this.deliveryContactPhone = deliveryContactPhone;
    }
}

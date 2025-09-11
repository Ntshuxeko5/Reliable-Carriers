package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class QuoteResponse {
    private String quoteId;
    private String trackingNumber;
    private BigDecimal totalCost;
    private BigDecimal baseCost;
    private BigDecimal serviceFee;
    private BigDecimal insuranceFee;
    private BigDecimal fuelSurcharge;
    private ServiceType serviceType;
    private String estimatedDeliveryTime;
    private Date estimatedDeliveryDate;
    private List<ServiceOption> availableServices;
    private String pickupAddress;
    private String deliveryAddress;
    private Double weight;
    private String dimensions;
    private String description;
    private Date quoteExpiryDate;
    private boolean isActive;
    
    public static class ServiceOption {
        private ServiceType serviceType;
        private BigDecimal cost;
        private String estimatedDeliveryTime;
        private String description;
        private boolean isRecommended;
        
        public ServiceOption(ServiceType serviceType, BigDecimal cost, String estimatedDeliveryTime, String description, boolean isRecommended) {
            this.serviceType = serviceType;
            this.cost = cost;
            this.estimatedDeliveryTime = estimatedDeliveryTime;
            this.description = description;
            this.isRecommended = isRecommended;
        }
        
        // Getters and Setters
        public ServiceType getServiceType() {
            return serviceType;
        }

        public void setServiceType(ServiceType serviceType) {
            this.serviceType = serviceType;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public String getEstimatedDeliveryTime() {
            return estimatedDeliveryTime;
        }

        public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
            this.estimatedDeliveryTime = estimatedDeliveryTime;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isRecommended() {
            return isRecommended;
        }

        public void setRecommended(boolean recommended) {
            isRecommended = recommended;
        }
    }
    
    // Getters and Setters
    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
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

    public List<ServiceOption> getAvailableServices() {
        return availableServices;
    }

    public void setAvailableServices(List<ServiceOption> availableServices) {
        this.availableServices = availableServices;
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

    public Date getQuoteExpiryDate() {
        return quoteExpiryDate;
    }

    public void setQuoteExpiryDate(Date quoteExpiryDate) {
        this.quoteExpiryDate = quoteExpiryDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

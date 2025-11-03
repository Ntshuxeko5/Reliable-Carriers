package com.reliablecarriers.Reliable.Carriers.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class QuoteResponse {
    private boolean success;
    private String message;
    private BigDecimal totalCost;
    private String currency;
    private String serviceType;
    private String serviceDescription;
    private BigDecimal basePrice;
    private BigDecimal weightCharge;
    private BigDecimal distanceCharge;
    private BigDecimal packageTypeMultiplier;
    private BigDecimal volumeWeight;
    private BigDecimal actualWeight;
    private BigDecimal distance;
    private String estimatedDelivery;
    private List<Map<String, Object>> serviceOptions;
    private Map<String, Object> breakdown;
    
    // Additional fields for compatibility with existing code
    private String quoteId;
    private String trackingNumber;
    private BigDecimal baseCost;
    private BigDecimal serviceFee;
    private BigDecimal insuranceFee;
    private BigDecimal fuelSurcharge;
    private double distanceKm;
    private String pickupAddress;
    private String deliveryAddress;
    private Double weight;
    private String dimensions;
    private String description;
    private Date quoteExpiryDate;
    private boolean active;
    private List<ServiceOption> serviceOptionsList;

    // Constructors
    public QuoteResponse() {}

    public QuoteResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public QuoteResponse(boolean success, BigDecimal totalCost, String serviceType, 
                        String estimatedDelivery) {
        this.success = success;
        this.totalCost = totalCost;
        this.serviceType = serviceType;
        this.estimatedDelivery = estimatedDelivery;
        this.currency = "ZAR";
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getWeightCharge() {
        return weightCharge;
    }

    public void setWeightCharge(BigDecimal weightCharge) {
        this.weightCharge = weightCharge;
    }

    public BigDecimal getDistanceCharge() {
        return distanceCharge;
    }

    public void setDistanceCharge(BigDecimal distanceCharge) {
        this.distanceCharge = distanceCharge;
    }

    public BigDecimal getPackageTypeMultiplier() {
        return packageTypeMultiplier;
    }

    public void setPackageTypeMultiplier(BigDecimal packageTypeMultiplier) {
        this.packageTypeMultiplier = packageTypeMultiplier;
    }

    public BigDecimal getVolumeWeight() {
        return volumeWeight;
    }

    public void setVolumeWeight(BigDecimal volumeWeight) {
        this.volumeWeight = volumeWeight;
    }

    public BigDecimal getActualWeight() {
        return actualWeight;
    }

    public void setActualWeight(BigDecimal actualWeight) {
        this.actualWeight = actualWeight;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public List<Map<String, Object>> getServiceOptions() {
        return serviceOptions;
    }

    public void setServiceOptions(List<Map<String, Object>> serviceOptions) {
        this.serviceOptions = serviceOptions;
    }

    public Map<String, Object> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(Map<String, Object> breakdown) {
        this.breakdown = breakdown;
    }

    // Additional getters and setters for compatibility
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

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
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
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<ServiceOption> getServiceOptionsList() {
        return serviceOptionsList;
    }

    public void setServiceOptionsList(List<ServiceOption> serviceOptionsList) {
        this.serviceOptionsList = serviceOptionsList;
    }

    // Additional method for compatibility
    public void setAvailableServices(List<ServiceOption> availableServices) {
        this.serviceOptionsList = availableServices;
    }

    // Additional methods for compatibility
    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDelivery = estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.quoteExpiryDate = estimatedDeliveryDate;
    }

    // Inner class for ServiceOption
    public static class ServiceOption {
        private String serviceType;
        private String serviceName;
        private BigDecimal cost;
        private String description;
        private int deliveryDays;
        private boolean recommended;
        private boolean selected;

        // Constructors
        public ServiceOption() {}

        public ServiceOption(String serviceType, String serviceName, BigDecimal cost, String description, int deliveryDays) {
            this.serviceType = serviceType;
            this.serviceName = serviceName;
            this.cost = cost;
            this.description = description;
            this.deliveryDays = deliveryDays;
        }

        // Additional constructor for compatibility
        public ServiceOption(String serviceType, BigDecimal cost, String description, String deliveryTime, boolean recommended) {
            this.serviceType = serviceType;
            this.serviceName = serviceType;
            this.cost = cost;
            this.description = description;
            this.deliveryDays = 1; // Default
            this.recommended = recommended;
        }

        // Getters and Setters
        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getDeliveryDays() {
            return deliveryDays;
        }

        public void setDeliveryDays(int deliveryDays) {
            this.deliveryDays = deliveryDays;
        }

        public boolean isRecommended() {
            return recommended;
        }

        public void setRecommended(boolean recommended) {
            this.recommended = recommended;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        // Additional methods for compatibility
        public String getEstimatedDeliveryTime() {
            return description; // Use description as delivery time for compatibility
        }

        public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
            this.description = estimatedDeliveryTime;
        }
    }
}
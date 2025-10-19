package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
// removed unused import

public class MovingServiceResponse {
    
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String driverName;
    private ServiceType serviceType;
    private String pickupAddress;
    private String deliveryAddress;
    private Double distanceKm;
    private BigDecimal basePrice;
    private BigDecimal totalPrice;
    private BigDecimal pricePerKm;
    private Integer maxFreeDistanceKm;
    private BigDecimal maxFreePrice;
    private String description;
    private Double weightKg;
    private Integer numberOfItems;
    private ShipmentStatus status;
    private String requestedDate;
    private String scheduledDate;
    private String completedDate;
    private String specialInstructions;
    private String createdAt;
    private String updatedAt;
    
    // Pricing breakdown
    private BigDecimal distanceCharge;
    private BigDecimal extraDistanceCharge;
    private String pricingBreakdown;
    
    public MovingServiceResponse(MovingService movingService) {
        this.id = movingService.getId();
        this.customerName = movingService.getCustomer() != null ? 
            movingService.getCustomer().getFirstName() + " " + movingService.getCustomer().getLastName() : "";
        this.customerEmail = movingService.getCustomer() != null ? 
            movingService.getCustomer().getEmail() : "";
        this.customerPhone = movingService.getCustomer() != null ? 
            movingService.getCustomer().getPhone() : "";
        this.driverName = movingService.getDriver() != null ? 
            movingService.getDriver().getFirstName() + " " + movingService.getDriver().getLastName() : "";
        this.serviceType = movingService.getServiceType();
        this.pickupAddress = movingService.getPickupAddress();
        this.deliveryAddress = movingService.getDeliveryAddress();
        this.distanceKm = movingService.getDistanceKm();
        this.basePrice = movingService.getBasePrice();
        this.totalPrice = movingService.getTotalPrice();
        this.pricePerKm = movingService.getPricePerKm();
        this.maxFreeDistanceKm = movingService.getMaxFreeDistanceKm();
        this.maxFreePrice = movingService.getMaxFreePrice();
        this.description = movingService.getDescription();
        this.weightKg = movingService.getWeightKg();
        this.numberOfItems = movingService.getNumberOfItems();
        this.status = movingService.getStatus();
        this.specialInstructions = movingService.getSpecialInstructions();
        
        // Format dates
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.requestedDate = movingService.getRequestedDate() != null ? 
            sdf.format(movingService.getRequestedDate()) : "";
        this.scheduledDate = movingService.getScheduledDate() != null ? 
            sdf.format(movingService.getScheduledDate()) : "";
        this.completedDate = movingService.getCompletedDate() != null ? 
            sdf.format(movingService.getCompletedDate()) : "";
        this.createdAt = movingService.getCreatedAt() != null ? 
            sdf.format(movingService.getCreatedAt()) : "";
        this.updatedAt = movingService.getUpdatedAt() != null ? 
            sdf.format(movingService.getUpdatedAt()) : "";
        
        // Calculate pricing breakdown
        calculatePricingBreakdown();
    }
    
    private void calculatePricingBreakdown() {
        if (distanceKm <= maxFreeDistanceKm) {
            this.distanceCharge = this.maxFreePrice;
            this.extraDistanceCharge = BigDecimal.ZERO;
            this.pricingBreakdown = String.format(
                "Base price for %d km: R%s", 
                maxFreeDistanceKm, 
                maxFreePrice.toString()
            );
        } else {
            this.distanceCharge = this.maxFreePrice;
            double extraDistance = distanceKm - maxFreeDistanceKm;
            this.extraDistanceCharge = this.pricePerKm.multiply(BigDecimal.valueOf(extraDistance));
            this.pricingBreakdown = String.format(
                "Base price for %d km: R%s + Extra %s km × R%s/km = R%s",
                maxFreeDistanceKm,
                maxFreePrice.toString(),
                String.format("%.1f", extraDistance),
                pricePerKm.toString(),
                extraDistanceCharge.toString()
            );
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getDriverName() {
        return driverName;
    }
    
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
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
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }
    
    public void setPricePerKm(BigDecimal pricePerKm) {
        this.pricePerKm = pricePerKm;
    }
    
    public Integer getMaxFreeDistanceKm() {
        return maxFreeDistanceKm;
    }
    
    public void setMaxFreeDistanceKm(Integer maxFreeDistanceKm) {
        this.maxFreeDistanceKm = maxFreeDistanceKm;
    }
    
    public BigDecimal getMaxFreePrice() {
        return maxFreePrice;
    }
    
    public void setMaxFreePrice(BigDecimal maxFreePrice) {
        this.maxFreePrice = maxFreePrice;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Double getWeightKg() {
        return weightKg;
    }
    
    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }
    
    public Integer getNumberOfItems() {
        return numberOfItems;
    }
    
    public void setNumberOfItems(Integer numberOfItems) {
        this.numberOfItems = numberOfItems;
    }
    
    public ShipmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }
    
    public String getRequestedDate() {
        return requestedDate;
    }
    
    public void setRequestedDate(String requestedDate) {
        this.requestedDate = requestedDate;
    }
    
    public String getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public String getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public BigDecimal getDistanceCharge() {
        return distanceCharge;
    }
    
    public void setDistanceCharge(BigDecimal distanceCharge) {
        this.distanceCharge = distanceCharge;
    }
    
    public BigDecimal getExtraDistanceCharge() {
        return extraDistanceCharge;
    }
    
    public void setExtraDistanceCharge(BigDecimal extraDistanceCharge) {
        this.extraDistanceCharge = extraDistanceCharge;
    }
    
    public String getPricingBreakdown() {
        return pricingBreakdown;
    }
    
    public void setPricingBreakdown(String pricingBreakdown) {
        this.pricingBreakdown = pricingBreakdown;
    }
}

package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Date;

public class MovingServiceRequest {
    
    @NotNull(message = "Service type is required")
    private ServiceType serviceType;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;
    
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
    
    @Positive(message = "Distance must be positive")
    private Double distanceKm;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @Positive(message = "Weight must be positive")
    private Double weightKg;
    
    @Positive(message = "Number of items must be positive")
    private Integer numberOfItems;
    
    private Date requestedDate;
    
    private Date scheduledDate;
    
    private String specialInstructions;
    
    // Optional: coordinates for distance calculation
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    
    // Constructor
    public MovingServiceRequest() {
        this.requestedDate = new Date();
    }
    
    // Calculate distance using Haversine formula if coordinates are provided
    public Double calculateDistance() {
        if (pickupLatitude != null && pickupLongitude != null && 
            deliveryLatitude != null && deliveryLongitude != null) {
            return calculateHaversineDistance(
                pickupLatitude, pickupLongitude,
                deliveryLatitude, deliveryLongitude
            );
        }
        return distanceKm;
    }
    
    private Double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    // Calculate price based on distance
    public BigDecimal calculatePrice() {
        double distance = calculateDistance();
        BigDecimal maxFreePrice = new BigDecimal("550.00"); // R550
        BigDecimal pricePerKm = new BigDecimal("25.00"); // R25 per km
        int maxFreeDistance = 20; // 20km
        
        if (distance <= maxFreeDistance) {
            return maxFreePrice;
        } else {
            double extraDistance = distance - maxFreeDistance;
            BigDecimal extraCost = pricePerKm.multiply(BigDecimal.valueOf(extraDistance));
            return maxFreePrice.add(extraCost);
        }
    }
    
    // Getters and Setters
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
    
    public Date getRequestedDate() {
        return requestedDate;
    }
    
    public void setRequestedDate(Date requestedDate) {
        this.requestedDate = requestedDate;
    }
    
    public Date getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public String getSpecialInstructions() {
        return specialInstructions;
    }
    
    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
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

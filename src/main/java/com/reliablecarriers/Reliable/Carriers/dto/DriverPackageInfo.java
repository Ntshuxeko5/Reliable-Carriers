package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;

import java.util.Date;

public class DriverPackageInfo {
    private Long id;
    private String trackingNumber;
    private String recipientName;
    private String recipientPhone;
    private String pickupAddress;
    private String pickupCity;
    private String pickupState;
    private String deliveryAddress;
    private String deliveryCity;
    private String deliveryState;
    private Double weight;
    private String description;
    private ShipmentStatus status;
    private Date estimatedDeliveryDate;
    private Date actualDeliveryDate;
    private String formattedStatus;
    private String formattedEstimatedDelivery;
    
    // Route optimization fields
    private Integer deliveryPriority; // 1 = highest priority
    private Double distanceFromCurrentLocation; // in km
    private Integer estimatedTimeMinutes;
    private String suggestedRoute;
    private Boolean isCurrentlyCarrying; // true if driver has this package in vehicle
    
    public DriverPackageInfo() {}
    
    public DriverPackageInfo(Shipment shipment) {
        this.id = shipment.getId();
        this.trackingNumber = shipment.getTrackingNumber();
        this.recipientName = shipment.getRecipientName();
        this.recipientPhone = shipment.getRecipientPhone();
        this.pickupAddress = shipment.getPickupAddress();
        this.pickupCity = shipment.getPickupCity();
        this.pickupState = shipment.getPickupState();
        this.deliveryAddress = shipment.getDeliveryAddress();
        this.deliveryCity = shipment.getDeliveryCity();
        this.deliveryState = shipment.getDeliveryState();
        this.weight = shipment.getWeight();
        this.description = shipment.getDescription();
        this.status = shipment.getStatus();
        this.estimatedDeliveryDate = shipment.getEstimatedDeliveryDate();
        this.actualDeliveryDate = shipment.getActualDeliveryDate();
        
        // Format status for display
        this.formattedStatus = formatStatus(shipment.getStatus());
        this.formattedEstimatedDelivery = formatDate(shipment.getEstimatedDeliveryDate());
    }
    
    private String formatStatus(ShipmentStatus status) {
        if (status == null) return "Unknown";
        switch (status) {
            case PENDING: return "Pending";
            case ASSIGNED: return "Assigned";
            case PICKED_UP: return "Picked Up";
            case IN_TRANSIT: return "In Transit";
            case OUT_FOR_DELIVERY: return "Out for Delivery";
            case DELIVERED: return "Delivered";
            case CANCELLED: return "Cancelled";
            default: return status.name();
        }
    }
    
    private String formatDate(Date date) {
        if (date == null) return "Not set";
        return new java.text.SimpleDateFormat("MMM dd, yyyy").format(date);
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getFormattedStatus() {
        return formattedStatus;
    }

    public void setFormattedStatus(String formattedStatus) {
        this.formattedStatus = formattedStatus;
    }

    public String getFormattedEstimatedDelivery() {
        return formattedEstimatedDelivery;
    }

    public void setFormattedEstimatedDelivery(String formattedEstimatedDelivery) {
        this.formattedEstimatedDelivery = formattedEstimatedDelivery;
    }

    public Integer getDeliveryPriority() {
        return deliveryPriority;
    }

    public void setDeliveryPriority(Integer deliveryPriority) {
        this.deliveryPriority = deliveryPriority;
    }

    public Double getDistanceFromCurrentLocation() {
        return distanceFromCurrentLocation;
    }

    public void setDistanceFromCurrentLocation(Double distanceFromCurrentLocation) {
        this.distanceFromCurrentLocation = distanceFromCurrentLocation;
    }

    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public String getSuggestedRoute() {
        return suggestedRoute;
    }

    public void setSuggestedRoute(String suggestedRoute) {
        this.suggestedRoute = suggestedRoute;
    }

    public Boolean getIsCurrentlyCarrying() {
        return isCurrentlyCarrying;
    }

    public void setIsCurrentlyCarrying(Boolean isCurrentlyCarrying) {
        this.isCurrentlyCarrying = isCurrentlyCarrying;
    }
}

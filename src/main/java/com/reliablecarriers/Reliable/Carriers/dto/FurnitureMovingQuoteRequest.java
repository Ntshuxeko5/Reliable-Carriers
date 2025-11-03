package com.reliablecarriers.Reliable.Carriers.dto;

import java.util.List;
import java.util.Map;

public class FurnitureMovingQuoteRequest {
    
    // Customer Information
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Pickup Location Details
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupPlaceId;
    private String pickupPropertyType;
    private String pickupFloor;
    private String pickupElevator;
    private String pickupParking;
    
    // Delivery Location Details
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String deliveryPlaceId;
    private String deliveryPropertyType;
    private String deliveryFloor;
    private String deliveryElevator;
    private String deliveryParking;
    
    // Moving Details
    private String movingDate;
    private String movingTime;
    private String specialInstructions;
    private boolean insurance;
    
    // Furniture Inventory
    private Map<String, Integer> inventory;
    
    // Additional Services
    private List<String> services;
    
    // Service Type
    private String serviceType;

    // Default constructor
    public FurnitureMovingQuoteRequest() {}

    // Getters and Setters
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

    public String getPickupPlaceId() {
        return pickupPlaceId;
    }

    public void setPickupPlaceId(String pickupPlaceId) {
        this.pickupPlaceId = pickupPlaceId;
    }

    public String getPickupPropertyType() {
        return pickupPropertyType;
    }

    public void setPickupPropertyType(String pickupPropertyType) {
        this.pickupPropertyType = pickupPropertyType;
    }

    public String getPickupFloor() {
        return pickupFloor;
    }

    public void setPickupFloor(String pickupFloor) {
        this.pickupFloor = pickupFloor;
    }

    public String getPickupElevator() {
        return pickupElevator;
    }

    public void setPickupElevator(String pickupElevator) {
        this.pickupElevator = pickupElevator;
    }

    public String getPickupParking() {
        return pickupParking;
    }

    public void setPickupParking(String pickupParking) {
        this.pickupParking = pickupParking;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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

    public String getDeliveryPlaceId() {
        return deliveryPlaceId;
    }

    public void setDeliveryPlaceId(String deliveryPlaceId) {
        this.deliveryPlaceId = deliveryPlaceId;
    }

    public String getDeliveryPropertyType() {
        return deliveryPropertyType;
    }

    public void setDeliveryPropertyType(String deliveryPropertyType) {
        this.deliveryPropertyType = deliveryPropertyType;
    }

    public String getDeliveryFloor() {
        return deliveryFloor;
    }

    public void setDeliveryFloor(String deliveryFloor) {
        this.deliveryFloor = deliveryFloor;
    }

    public String getDeliveryElevator() {
        return deliveryElevator;
    }

    public void setDeliveryElevator(String deliveryElevator) {
        this.deliveryElevator = deliveryElevator;
    }

    public String getDeliveryParking() {
        return deliveryParking;
    }

    public void setDeliveryParking(String deliveryParking) {
        this.deliveryParking = deliveryParking;
    }

    public String getMovingDate() {
        return movingDate;
    }

    public void setMovingDate(String movingDate) {
        this.movingDate = movingDate;
    }

    public String getMovingTime() {
        return movingTime;
    }

    public void setMovingTime(String movingTime) {
        this.movingTime = movingTime;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public boolean isInsurance() {
        return insurance;
    }

    public void setInsurance(boolean insurance) {
        this.insurance = insurance;
    }

    public Map<String, Integer> getInventory() {
        return inventory;
    }

    public void setInventory(Map<String, Integer> inventory) {
        this.inventory = inventory;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}

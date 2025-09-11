package com.reliablecarriers.Reliable.Carriers.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "automated_dispatch")
public class AutomatedDispatch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String ruleName;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column(nullable = false)
    private Integer priority; // Higher number = higher priority
    
    @Column(length = 50)
    private String serviceType; // ECONOMY, EXPRESS, SAME_DAY, etc.
    
    @Column(length = 50)
    private String pickupCity;
    
    @Column(length = 50)
    private String pickupState;
    
    @Column(length = 50)
    private String deliveryCity;
    
    @Column(length = 50)
    private String deliveryState;
    
    @Column
    private Double maxDistanceKm; // Maximum distance for this rule
    
    @Column
    private Double minDistanceKm; // Minimum distance for this rule
    
    @Column
    private Double maxWeightKg; // Maximum weight for this rule
    
    @Column
    private Double minWeightKg; // Minimum weight for this rule
    
    @Column(length = 20)
    private String driverRole; // DRIVER, SENIOR_DRIVER, etc.
    
    @Column(length = 20)
    private String vehicleType; // VAN, TRUCK, MOTORCYCLE, etc.
    
    @Column
    private Integer maxPackagesPerDriver; // Maximum packages per driver
    
    @Column
    private Integer maxWeightPerDriver; // Maximum weight per driver in kg
    
    @Column(length = 20)
    private String timeWindow; // MORNING, AFTERNOON, EVENING, etc.
    
    @Column
    private Integer estimatedDeliveryTimeMinutes; // Expected delivery time
    
    @Column(length = 20)
    private String assignmentMethod; // NEAREST, LEAST_LOADED, ROUND_ROBIN, etc.
    
    @Column
    private Boolean requireSignature;
    
    @Column
    private Boolean requirePhoto;
    
    @Column
    private Boolean requireIdVerification;
    
    @Column(length = 500)
    private String specialInstructions;
    
    @Column
    private Long driverId; // Assigned driver ID
    
    @Column
    private Long shipmentId; // Associated shipment ID
    
    @Column(length = 20)
    private String dispatchStatus; // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    
    @Column(length = 20)
    private String priorityLevel; // LOW, MEDIUM, HIGH, URGENT
    
    @Column(length = 20)
    private String dispatchType; // PICKUP, DELIVERY, BOTH
    
    @Column
    private Long vehicleId; // Assigned vehicle ID
    
    @Column
    private Double optimizationScore; // AI optimization score
    
    @Column
    private Date estimatedPickupTime; // Estimated pickup time
    
    @Column
    private Date actualDeliveryTime; // Actual delivery time
    
    @Column(nullable = false)
    private Date createdAt;
    
    @Column(nullable = false)
    private Date updatedAt;
    
    // Constructor
    public AutomatedDispatch() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isActive = true;
        this.priority = 1;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public Double getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(Double maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public Double getMinDistanceKm() {
        return minDistanceKm;
    }

    public void setMinDistanceKm(Double minDistanceKm) {
        this.minDistanceKm = minDistanceKm;
    }

    public Double getMaxWeightKg() {
        return maxWeightKg;
    }

    public void setMaxWeightKg(Double maxWeightKg) {
        this.maxWeightKg = maxWeightKg;
    }

    public Double getMinWeightKg() {
        return minWeightKg;
    }

    public void setMinWeightKg(Double minWeightKg) {
        this.minWeightKg = minWeightKg;
    }

    public String getDriverRole() {
        return driverRole;
    }

    public void setDriverRole(String driverRole) {
        this.driverRole = driverRole;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getMaxPackagesPerDriver() {
        return maxPackagesPerDriver;
    }

    public void setMaxPackagesPerDriver(Integer maxPackagesPerDriver) {
        this.maxPackagesPerDriver = maxPackagesPerDriver;
    }

    public Integer getMaxWeightPerDriver() {
        return maxWeightPerDriver;
    }

    public void setMaxWeightPerDriver(Integer maxWeightPerDriver) {
        this.maxWeightPerDriver = maxWeightPerDriver;
    }

    public String getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(String timeWindow) {
        this.timeWindow = timeWindow;
    }

    public Integer getEstimatedDeliveryTimeMinutes() {
        return estimatedDeliveryTimeMinutes;
    }

    public void setEstimatedDeliveryTimeMinutes(Integer estimatedDeliveryTimeMinutes) {
        this.estimatedDeliveryTimeMinutes = estimatedDeliveryTimeMinutes;
    }

    public String getAssignmentMethod() {
        return assignmentMethod;
    }

    public void setAssignmentMethod(String assignmentMethod) {
        this.assignmentMethod = assignmentMethod;
    }

    public Boolean getRequireSignature() {
        return requireSignature;
    }

    public void setRequireSignature(Boolean requireSignature) {
        this.requireSignature = requireSignature;
    }

    public Boolean getRequirePhoto() {
        return requirePhoto;
    }

    public void setRequirePhoto(Boolean requirePhoto) {
        this.requirePhoto = requirePhoto;
    }

    public Boolean getRequireIdVerification() {
        return requireIdVerification;
    }

    public void setRequireIdVerification(Boolean requireIdVerification) {
        this.requireIdVerification = requireIdVerification;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getDispatchStatus() {
        return dispatchStatus;
    }

    public void setDispatchStatus(String dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getDispatchType() {
        return dispatchType;
    }

    public void setDispatchType(String dispatchType) {
        this.dispatchType = dispatchType;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getOptimizationScore() {
        return optimizationScore;
    }

    public void setOptimizationScore(Double optimizationScore) {
        this.optimizationScore = optimizationScore;
    }

    public Date getEstimatedPickupTime() {
        return estimatedPickupTime;
    }

    public void setEstimatedPickupTime(Date estimatedPickupTime) {
        this.estimatedPickupTime = estimatedPickupTime;
    }

    public Date getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(Date actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
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
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}

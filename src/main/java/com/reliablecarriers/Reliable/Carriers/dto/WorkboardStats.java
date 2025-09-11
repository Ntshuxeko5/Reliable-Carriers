package com.reliablecarriers.Reliable.Carriers.dto;

import java.math.BigDecimal;
import java.util.Date;

public class WorkboardStats {
    private Long totalPackages;
    private Long packagesToPickup;
    private Long packagesInVehicle;
    private Long packagesDeliveredToday;
    private Long packagesFailedToday;
    private Double totalWeight;
    private Double totalDistance;
    private Integer estimatedTimeRemaining; // in minutes
    private BigDecimal todayEarnings;
    private BigDecimal totalEarnings;
    private String currentLocation;
    private Double currentLat;
    private Double currentLng;
    private Date lastLocationUpdate;
    private String driverStatus; // ONLINE, OFFLINE, BUSY, BREAK
    private Integer totalStops;
    private Integer completedStops;
    private Double averageDeliveryTime; // in minutes
    private Integer onTimeDeliveries;
    private Integer lateDeliveries;
    private Double customerSatisfactionScore;

    // Constructors
    public WorkboardStats() {}

    // Getters and Setters
    public Long getTotalPackages() {
        return totalPackages;
    }

    public void setTotalPackages(Long totalPackages) {
        this.totalPackages = totalPackages;
    }

    public Long getPackagesToPickup() {
        return packagesToPickup;
    }

    public void setPackagesToPickup(Long packagesToPickup) {
        this.packagesToPickup = packagesToPickup;
    }

    public Long getPackagesInVehicle() {
        return packagesInVehicle;
    }

    public void setPackagesInVehicle(Long packagesInVehicle) {
        this.packagesInVehicle = packagesInVehicle;
    }

    public Long getPackagesDeliveredToday() {
        return packagesDeliveredToday;
    }

    public void setPackagesDeliveredToday(Long packagesDeliveredToday) {
        this.packagesDeliveredToday = packagesDeliveredToday;
    }

    public Long getPackagesFailedToday() {
        return packagesFailedToday;
    }

    public void setPackagesFailedToday(Long packagesFailedToday) {
        this.packagesFailedToday = packagesFailedToday;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Integer getEstimatedTimeRemaining() {
        return estimatedTimeRemaining;
    }

    public void setEstimatedTimeRemaining(Integer estimatedTimeRemaining) {
        this.estimatedTimeRemaining = estimatedTimeRemaining;
    }

    public BigDecimal getTodayEarnings() {
        return todayEarnings;
    }

    public void setTodayEarnings(BigDecimal todayEarnings) {
        this.todayEarnings = todayEarnings;
    }

    public BigDecimal getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(BigDecimal totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLat = currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLng = currentLng;
    }

    public Date getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(Date lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public Integer getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(Integer totalStops) {
        this.totalStops = totalStops;
    }

    public Integer getCompletedStops() {
        return completedStops;
    }

    public void setCompletedStops(Integer completedStops) {
        this.completedStops = completedStops;
    }

    public Double getAverageDeliveryTime() {
        return averageDeliveryTime;
    }

    public void setAverageDeliveryTime(Double averageDeliveryTime) {
        this.averageDeliveryTime = averageDeliveryTime;
    }

    public Integer getOnTimeDeliveries() {
        return onTimeDeliveries;
    }

    public void setOnTimeDeliveries(Integer onTimeDeliveries) {
        this.onTimeDeliveries = onTimeDeliveries;
    }

    public Integer getLateDeliveries() {
        return lateDeliveries;
    }

    public void setLateDeliveries(Integer lateDeliveries) {
        this.lateDeliveries = lateDeliveries;
    }

    public Double getCustomerSatisfactionScore() {
        return customerSatisfactionScore;
    }

    public void setCustomerSatisfactionScore(Double customerSatisfactionScore) {
        this.customerSatisfactionScore = customerSatisfactionScore;
    }
}

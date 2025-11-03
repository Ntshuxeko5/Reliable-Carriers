package com.reliablecarriers.Reliable.Carriers.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TrackingResponse {
    private String trackingNumber;
    private String status;
    private String statusText;
    private int progress;
    private String pickupLocation;
    private String deliveryLocation;
    private String estimatedDelivery;
    private String serviceType;
    private LocalDateTime lastUpdated;
    private List<TrackingEvent> timeline;
    private DriverInfo driver;
    private Map<String, Object> packageDetails;

    // Constructors
    public TrackingResponse() {}

    public TrackingResponse(String trackingNumber, String status, String statusText) {
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.statusText = statusText;
    }

    // Getters and Setters
    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public void setEstimatedDelivery(String estimatedDelivery) {
        this.estimatedDelivery = estimatedDelivery;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<TrackingEvent> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TrackingEvent> timeline) {
        this.timeline = timeline;
    }

    public DriverInfo getDriver() {
        return driver;
    }

    public void setDriver(DriverInfo driver) {
        this.driver = driver;
    }

    public Map<String, Object> getPackageDetails() {
        return packageDetails;
    }

    public void setPackageDetails(Map<String, Object> packageDetails) {
        this.packageDetails = packageDetails;
    }

    // Inner classes
    public static class TrackingEvent {
        private String status;
        private String title;
        private String description;
        private LocalDateTime timestamp;
        private boolean completed;
        private boolean current;
        private String location;

        // Constructors
        public TrackingEvent() {}

        public TrackingEvent(String status, String title, String description, LocalDateTime timestamp) {
            this.status = status;
            this.title = title;
            this.description = description;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean isCurrent() {
            return current;
        }

        public void setCurrent(boolean current) {
            this.current = current;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class DriverInfo {
        private String name;
        private String phone;
        private String vehicle;
        private String vehiclePlate;
        private String currentLocation;
        private LocalDateTime lastLocationUpdate;

        // Constructors
        public DriverInfo() {}

        public DriverInfo(String name, String phone, String vehicle) {
            this.name = name;
            this.phone = phone;
            this.vehicle = vehicle;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getVehicle() {
            return vehicle;
        }

        public void setVehicle(String vehicle) {
            this.vehicle = vehicle;
        }

        public String getVehiclePlate() {
            return vehiclePlate;
        }

        public void setVehiclePlate(String vehiclePlate) {
            this.vehiclePlate = vehiclePlate;
        }

        public String getCurrentLocation() {
            return currentLocation;
        }

        public void setCurrentLocation(String currentLocation) {
            this.currentLocation = currentLocation;
        }

        public LocalDateTime getLastLocationUpdate() {
            return lastLocationUpdate;
        }

        public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
            this.lastLocationUpdate = lastLocationUpdate;
        }
    }
}



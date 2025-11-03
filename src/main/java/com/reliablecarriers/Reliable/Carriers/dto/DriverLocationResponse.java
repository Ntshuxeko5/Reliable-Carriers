package com.reliablecarriers.Reliable.Carriers.dto;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import com.reliablecarriers.Reliable.Carriers.model.DriverLocation;
import com.reliablecarriers.Reliable.Carriers.model.User;

public class DriverLocationResponse {
    private Long id;
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String driverEmail;
    private String driverStatus;
    private Double latitude;
    private Double longitude;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String notes;
    private Date timestamp;
    private String formattedTimestamp;
    private String status;
    private Boolean isOnline;
    private Long vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private String vehiclePlate;
    private String vehicleType;
    private List<Object> activeShipments;
    private Integer totalPackages;
    private Double totalWeight;
    private String nextDeliveryLocation;
    private String nextDeliveryTime;
    private Date lastLocationUpdate;
    private Integer assignedPackageCount;
    private Integer packagesToPickup;
    private Integer packagesInVehicle;

    // Constructors
    public DriverLocationResponse() {}

    public DriverLocationResponse(Long driverId, String driverName, String driverPhone, String driverEmail, 
                                 String driverStatus, Double latitude, Double longitude, String address, 
                                 Date lastLocationUpdate, Integer assignedPackageCount, Integer packagesToPickup, 
                                 Integer packagesInVehicle) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.driverEmail = driverEmail;
        this.driverStatus = driverStatus;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.lastLocationUpdate = lastLocationUpdate;
        this.assignedPackageCount = assignedPackageCount;
        this.packagesToPickup = packagesToPickup;
        this.packagesInVehicle = packagesInVehicle;
    }

    public DriverLocationResponse(DriverLocation driverLocation) {
        this.id = driverLocation.getId();
        this.latitude = driverLocation.getLatitude();
        this.longitude = driverLocation.getLongitude();
        this.address = driverLocation.getAddress();
        this.city = driverLocation.getCity();
        this.state = driverLocation.getState();
        this.zipCode = driverLocation.getZipCode();
        this.country = driverLocation.getCountry();
        this.notes = driverLocation.getNotes();
        this.timestamp = java.sql.Timestamp.valueOf(driverLocation.getTimestamp());
        this.formattedTimestamp = formatTimestamp(java.sql.Timestamp.valueOf(driverLocation.getTimestamp()));
        
        // Driver information
        if (driverLocation.getDriver() != null) {
            User driver = driverLocation.getDriver();
            this.driverId = driver.getId();
            this.driverName = driver.getFirstName() + " " + driver.getLastName();
            this.driverEmail = driver.getEmail();
            this.driverPhone = driver.getPhone();
        }
        
        // Vehicle information (stored as String in DriverLocation)
        if (driverLocation.getVehicle() != null) {
            this.vehiclePlate = driverLocation.getVehicle(); // Vehicle is stored as registration string
            this.vehicleMake = "Unknown"; // Not available in String format
            this.vehicleModel = "Unknown"; // Not available in String format
            this.vehicleType = "Unknown"; // Not available in String format
        }
        
        // Set default status as online if location is recent (within last 5 minutes)
        this.isOnline = isLocationRecent(java.sql.Timestamp.valueOf(driverLocation.getTimestamp()));
        this.status = this.isOnline ? "ACTIVE" : "OFFLINE";
        
        // Initialize shipment information (will be populated by service layer)
        this.activeShipments = new ArrayList<>();
        this.totalPackages = 0;
        this.totalWeight = 0.0;
        this.nextDeliveryLocation = "";
        this.nextDeliveryTime = "";
    }

    private String formatTimestamp(Date timestamp) {
        if (timestamp == null) return "";
        return new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(timestamp);
    }

    private boolean isLocationRecent(Date timestamp) {
        if (timestamp == null) return false;
        long currentTime = System.currentTimeMillis();
        long locationTime = timestamp.getTime();
        long fiveMinutesInMillis = 5 * 60 * 1000;
        return (currentTime - locationTime) < fiveMinutesInMillis;
    }

    // Getters and Setters
    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(Date lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public Integer getAssignedPackageCount() {
        return assignedPackageCount;
    }

    public void setAssignedPackageCount(Integer assignedPackageCount) {
        this.assignedPackageCount = assignedPackageCount;
    }

    public Integer getPackagesToPickup() {
        return packagesToPickup;
    }

    public void setPackagesToPickup(Integer packagesToPickup) {
        this.packagesToPickup = packagesToPickup;
    }

    public Integer getPackagesInVehicle() {
        return packagesInVehicle;
    }

    public void setPackagesInVehicle(Integer packagesInVehicle) {
        this.packagesInVehicle = packagesInVehicle;
    }

    // Additional getters and setters for new fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = formattedTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleMake() {
        return vehicleMake;
    }

    public void setVehicleMake(String vehicleMake) {
        this.vehicleMake = vehicleMake;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public List<Object> getActiveShipments() {
        return activeShipments;
    }

    public void setActiveShipments(List<Object> activeShipments) {
        this.activeShipments = activeShipments;
    }

    public Integer getTotalPackages() {
        return totalPackages;
    }

    public void setTotalPackages(Integer totalPackages) {
        this.totalPackages = totalPackages;
    }

    public Double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(Double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public String getNextDeliveryLocation() {
        return nextDeliveryLocation;
    }

    public void setNextDeliveryLocation(String nextDeliveryLocation) {
        this.nextDeliveryLocation = nextDeliveryLocation;
    }

    public String getNextDeliveryTime() {
        return nextDeliveryTime;
    }

    public void setNextDeliveryTime(String nextDeliveryTime) {
        this.nextDeliveryTime = nextDeliveryTime;
    }
}
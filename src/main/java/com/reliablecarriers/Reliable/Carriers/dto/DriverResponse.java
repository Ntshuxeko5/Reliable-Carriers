package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Vehicle;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;

import java.util.Date;

public class DriverResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private UserRole role;
    private Date createdAt;
    private Date updatedAt;
    
    // Vehicle Information
    private Long vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private String vehicleRegistrationNumber;
    private String vehicleType;
    private Integer vehicleYear;
    private String vehicleColor;
    private String vehicleFuelType;
    private Double vehicleCapacity;
    private Double vehicleMileage;
    private String vehicleStatus;
    private Date vehicleLastMaintenanceDate;
    private Date vehicleNextMaintenanceDate;
    
    // Driver Status Information
    private Boolean isOnline;
    private String currentStatus; // "ACTIVE", "INACTIVE", "OFFLINE", "ON_DELIVERY"
    private Integer activePackages;
    private Double totalWeightCarrying;
    private String currentLocation;
    private Date lastLocationUpdate;
    
    public DriverResponse() {}
    
    public DriverResponse(User driver) {
        this.id = driver.getId();
        this.firstName = driver.getFirstName();
        this.lastName = driver.getLastName();
        this.email = driver.getEmail();
        this.phone = driver.getPhone();
        this.address = driver.getAddress();
        this.city = driver.getCity();
        this.state = driver.getState();
        this.zipCode = driver.getZipCode();
        this.country = driver.getCountry();
        this.role = driver.getRole();
        this.createdAt = driver.getCreatedAt();
        this.updatedAt = driver.getUpdatedAt();
    }
    
    public DriverResponse(User driver, Vehicle vehicle) {
        this(driver);
        if (vehicle != null) {
            this.vehicleId = vehicle.getId();
            this.vehicleMake = vehicle.getMake();
            this.vehicleModel = vehicle.getModel();
            this.vehicleRegistrationNumber = vehicle.getRegistrationNumber();
            this.vehicleType = vehicle.getType() != null ? vehicle.getType().toString() : null;
            this.vehicleYear = vehicle.getYear();
            this.vehicleColor = vehicle.getColor();
            this.vehicleFuelType = vehicle.getFuelType();
            this.vehicleCapacity = vehicle.getCapacity();
            this.vehicleMileage = vehicle.getMileage();
            this.vehicleStatus = vehicle.getStatus() != null ? vehicle.getStatus().toString() : null;
            this.vehicleLastMaintenanceDate = vehicle.getLastMaintenanceDate();
            this.vehicleNextMaintenanceDate = vehicle.getNextMaintenanceDate();
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
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

    // Vehicle getters and setters
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

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(Integer vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public String getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(String vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public String getVehicleFuelType() {
        return vehicleFuelType;
    }

    public void setVehicleFuelType(String vehicleFuelType) {
        this.vehicleFuelType = vehicleFuelType;
    }

    public Double getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(Double vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }

    public Double getVehicleMileage() {
        return vehicleMileage;
    }

    public void setVehicleMileage(Double vehicleMileage) {
        this.vehicleMileage = vehicleMileage;
    }

    public String getVehicleStatus() {
        return vehicleStatus;
    }

    public void setVehicleStatus(String vehicleStatus) {
        this.vehicleStatus = vehicleStatus;
    }

    public Date getVehicleLastMaintenanceDate() {
        return vehicleLastMaintenanceDate;
    }

    public void setVehicleLastMaintenanceDate(Date vehicleLastMaintenanceDate) {
        this.vehicleLastMaintenanceDate = vehicleLastMaintenanceDate;
    }

    public Date getVehicleNextMaintenanceDate() {
        return vehicleNextMaintenanceDate;
    }

    public void setVehicleNextMaintenanceDate(Date vehicleNextMaintenanceDate) {
        this.vehicleNextMaintenanceDate = vehicleNextMaintenanceDate;
    }

    // Status getters and setters
    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Integer getActivePackages() {
        return activePackages;
    }

    public void setActivePackages(Integer activePackages) {
        this.activePackages = activePackages;
    }

    public Double getTotalWeightCarrying() {
        return totalWeightCarrying;
    }

    public void setTotalWeightCarrying(Double totalWeightCarrying) {
        this.totalWeightCarrying = totalWeightCarrying;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Date getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(Date lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getVehicleFullName() {
        if (vehicleMake != null && vehicleModel != null) {
            return vehicleMake + " " + vehicleModel;
        } else if (vehicleMake != null) {
            return vehicleMake;
        } else if (vehicleModel != null) {
            return vehicleModel;
        }
        return "Vehicle not assigned";
    }
    
    public String getVehicleDisplayInfo() {
        StringBuilder info = new StringBuilder();
        if (vehicleMake != null && vehicleModel != null) {
            info.append(vehicleMake).append(" ").append(vehicleModel);
        }
        if (vehicleYear != null) {
            info.append(" (").append(vehicleYear).append(")");
        }
        if (vehicleRegistrationNumber != null) {
            info.append(" - ").append(vehicleRegistrationNumber);
        }
        return info.toString();
    }
}

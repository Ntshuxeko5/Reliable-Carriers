package com.reliablecarriers.Reliable.Carriers.dto;

public class DriverPackageAssignmentRequest {
    private Long driverId;
    private Long packageId;

    // Constructors
    public DriverPackageAssignmentRequest() {}

    public DriverPackageAssignmentRequest(Long driverId, Long packageId) {
        this.driverId = driverId;
        this.packageId = packageId;
    }

    // Getters and Setters
    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }
}

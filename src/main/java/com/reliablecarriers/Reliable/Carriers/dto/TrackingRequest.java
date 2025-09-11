package com.reliablecarriers.Reliable.Carriers.dto;

import java.util.Date;
import java.util.List;

public class TrackingRequest {
    private List<Long> driverIds;
    private List<Long> vehicleIds;
    private String status; // "ACTIVE", "INACTIVE", "OFFLINE", "ALL"
    private String city;
    private String state;
    private Date startTime;
    private Date endTime;
    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;
    private Boolean includeOfflineDrivers;
    private String searchTerm; // For searching by driver name, vehicle plate, etc.
    private Integer limit; // Limit number of results
    private Boolean realTime; // Whether to get real-time updates

    public TrackingRequest() {
        this.includeOfflineDrivers = false;
        this.realTime = false;
        this.limit = 100;
    }

    // Getters and Setters
    public List<Long> getDriverIds() {
        return driverIds;
    }

    public void setDriverIds(List<Long> driverIds) {
        this.driverIds = driverIds;
    }

    public List<Long> getVehicleIds() {
        return vehicleIds;
    }

    public void setVehicleIds(List<Long> vehicleIds) {
        this.vehicleIds = vehicleIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Double getMinLatitude() {
        return minLatitude;
    }

    public void setMinLatitude(Double minLatitude) {
        this.minLatitude = minLatitude;
    }

    public Double getMaxLatitude() {
        return maxLatitude;
    }

    public void setMaxLatitude(Double maxLatitude) {
        this.maxLatitude = maxLatitude;
    }

    public Double getMinLongitude() {
        return minLongitude;
    }

    public void setMinLongitude(Double minLongitude) {
        this.minLongitude = minLongitude;
    }

    public Double getMaxLongitude() {
        return maxLongitude;
    }

    public void setMaxLongitude(Double maxLongitude) {
        this.maxLongitude = maxLongitude;
    }

    public Boolean getIncludeOfflineDrivers() {
        return includeOfflineDrivers;
    }

    public void setIncludeOfflineDrivers(Boolean includeOfflineDrivers) {
        this.includeOfflineDrivers = includeOfflineDrivers;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean getRealTime() {
        return realTime;
    }

    public void setRealTime(Boolean realTime) {
        this.realTime = realTime;
    }
}

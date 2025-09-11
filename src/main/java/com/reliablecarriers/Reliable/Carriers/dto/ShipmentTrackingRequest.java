package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import java.util.Date;

public class ShipmentTrackingRequest {
    
    private Long shipmentId;
    private ShipmentStatus status;
    private String location;
    private String notes;
    private Long updatedById;
    private Date createdAt;
    
    // Constructors
    public ShipmentTrackingRequest() {
    }
    
    public ShipmentTrackingRequest(Long shipmentId, ShipmentStatus status, String location, String notes, Long updatedById) {
        this.shipmentId = shipmentId;
        this.status = status;
        this.location = location;
        this.notes = notes;
        this.updatedById = updatedById;
    }
    
    // Getters and Setters
    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
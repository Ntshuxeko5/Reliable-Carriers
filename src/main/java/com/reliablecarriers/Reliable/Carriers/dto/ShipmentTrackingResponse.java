package com.reliablecarriers.Reliable.Carriers.dto;

import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking;
import java.util.Date;

public class ShipmentTrackingResponse {
    
    private Long id;
    private Long shipmentId;
    private String shipmentTrackingNumber;
    private ShipmentStatus status;
    private String location;
    private String notes;
    private Long updatedById;
    private String updatedByName;
    private Date createdAt;
    
    // Constructors
    public ShipmentTrackingResponse() {
    }
    
    public ShipmentTrackingResponse(ShipmentTracking tracking) {
        this.id = tracking.getId();
        
        if (tracking.getShipment() != null) {
            this.shipmentId = tracking.getShipment().getId();
            this.shipmentTrackingNumber = tracking.getShipment().getTrackingNumber();
        }
        
        this.status = tracking.getStatus();
        this.location = tracking.getLocation();
        this.notes = tracking.getNotes();
        
        if (tracking.getUpdatedBy() != null) {
            this.updatedById = tracking.getUpdatedBy().getId();
            this.updatedByName = tracking.getUpdatedBy().getFirstName() + " " + tracking.getUpdatedBy().getLastName();
        }
        
        this.createdAt = tracking.getCreatedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getShipmentTrackingNumber() {
        return shipmentTrackingNumber;
    }

    public void setShipmentTrackingNumber(String shipmentTrackingNumber) {
        this.shipmentTrackingNumber = shipmentTrackingNumber;
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

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
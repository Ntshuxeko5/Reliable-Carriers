package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.ShipmentTrackingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.ShipmentTrackingResponse;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.util.Date;
import java.util.List;

public interface ShipmentTrackingService {
    
    ShipmentTracking createTrackingEntry(ShipmentTracking tracking);
    
    ShipmentTracking getTrackingEntryById(Long id);
    
    List<ShipmentTracking> getAllTrackingEntries();
    
    List<ShipmentTracking> getTrackingEntriesByShipment(Shipment shipment);
    
    List<ShipmentTracking> getTrackingEntriesByShipmentId(Long shipmentId);
    
    List<ShipmentTracking> getTrackingEntriesByStatus(ShipmentStatus status);
    
    List<ShipmentTracking> getTrackingEntriesByDateRange(Date startDate, Date endDate);
    
    List<ShipmentTracking> getTrackingEntriesByLocation(String location);
    
    List<ShipmentTracking> getTrackingEntriesByUpdatedBy(User user);
    
    List<ShipmentTracking> getTrackingEntriesByUpdatedById(Long userId);
    
    void deleteTrackingEntry(Long id);
    
    // DTO-based methods (for API use)
    ShipmentTrackingResponse createTrackingEntryFromRequest(ShipmentTrackingRequest request);
}
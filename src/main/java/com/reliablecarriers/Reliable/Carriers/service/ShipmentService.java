package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.util.Date;
import java.util.List;

public interface ShipmentService {
    
    Shipment createShipment(Shipment shipment);
    
    Shipment updateShipment(Long id, Shipment shipment);
    
    Shipment getShipmentById(Long id);
    
    Shipment getShipmentByTrackingNumber(String trackingNumber);
    
    List<Shipment> getAllShipments();
    
    List<Shipment> getShipmentsBySender(User sender);
    
    List<Shipment> getShipmentsByDriver(User driver);
    
    List<Shipment> getShipmentsByDriverId(Long driverId);
    
    List<Shipment> getShipmentsByStatus(ShipmentStatus status);
    
    List<Shipment> getShipmentsByDateRange(Date startDate, Date endDate);
    
    List<Shipment> getShipmentsByDeliveryDateRange(Date startDate, Date endDate);
    
    List<Shipment> getShipmentsByPickupLocation(String city, String state);
    
    List<Shipment> getShipmentsByDeliveryLocation(String city, String state);
    
    void deleteShipment(Long id);
    
    Shipment assignDriverToShipment(Long shipmentId, Long driverId);
    
    Shipment updateShipmentStatus(Long shipmentId, ShipmentStatus status, String location, String notes);
}
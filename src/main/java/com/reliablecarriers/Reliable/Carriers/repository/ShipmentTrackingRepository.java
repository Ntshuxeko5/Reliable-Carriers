package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentTracking;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long> {
    
    List<ShipmentTracking> findByShipment(Shipment shipment);
    
    List<ShipmentTracking> findByShipmentOrderByCreatedAtDesc(Shipment shipment);
    
    List<ShipmentTracking> findByStatus(ShipmentStatus status);
    
    List<ShipmentTracking> findByStatusOrderByCreatedAtDesc(ShipmentStatus status);
    
    List<ShipmentTracking> findByCreatedAtBetween(Date startDate, Date endDate);
    
    List<ShipmentTracking> findByCreatedAtBetweenOrderByCreatedAtDesc(Date startDate, Date endDate);
    
    List<ShipmentTracking> findByLocationContaining(String location);
    
    List<ShipmentTracking> findByLocationContainingIgnoreCaseOrderByCreatedAtDesc(String location);
    
    List<ShipmentTracking> findByUpdatedByOrderByCreatedAtDesc(User updatedBy);
}
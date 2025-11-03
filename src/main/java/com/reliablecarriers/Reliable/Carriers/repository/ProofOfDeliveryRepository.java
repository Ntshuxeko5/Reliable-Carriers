package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.ProofOfDelivery;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProofOfDeliveryRepository extends JpaRepository<ProofOfDelivery, Long> {
    
    Optional<ProofOfDelivery> findByShipment(Shipment shipment);
    
    List<ProofOfDelivery> findByDriver(User driver);
    
    List<ProofOfDelivery> findByDeliveryDateBetween(Date startDate, Date endDate);
    
    List<ProofOfDelivery> findByDeliveryStatus(String deliveryStatus);
    
    List<ProofOfDelivery> findByDriverAndDeliveryDateBetween(User driver, Date startDate, Date endDate);
    
    List<ProofOfDelivery> findByDriverIdAndDeliveryDateAfter(Long driverId, Date date);
    
    Optional<ProofOfDelivery> findByShipmentId(Long shipmentId);
    
    List<ProofOfDelivery> findByDriverOrderByDeliveryDateDesc(User driver);
    
    List<ProofOfDelivery> findByDeliveryDateBetweenOrderByDeliveryDateDesc(Date startDate, Date endDate);
    
    List<ProofOfDelivery> findByDeliveryStatusOrderByDeliveryDateDesc(String deliveryStatus);
    
    long countByDeliveryStatus(String deliveryStatus);
    
    long countByDeliveryDateBetween(Date startDate, Date endDate);
    
    long countByDeliveryDateBetweenAndDeliveryStatus(Date startDate, Date endDate, String deliveryStatus);
    
    List<ProofOfDelivery> findByShipmentOrderByDeliveryDateDesc(Shipment shipment);
}
package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.ProofOfDelivery;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProofOfDeliveryRepository extends JpaRepository<ProofOfDelivery, Long> {
    
    // Find by shipment
    Optional<ProofOfDelivery> findByShipment(Shipment shipment);
    
    // Find by driver
    List<ProofOfDelivery> findByDriverOrderByDeliveryDateDesc(User driver);
    
    // Find by delivery status
    List<ProofOfDelivery> findByDeliveryStatusOrderByDeliveryDateDesc(String deliveryStatus);
    
    // Find by date range
    List<ProofOfDelivery> findByDeliveryDateBetweenOrderByDeliveryDateDesc(Date startDate, Date endDate);
    
    // Find by shipment with ordering
    List<ProofOfDelivery> findByShipmentOrderByDeliveryDateDesc(Shipment shipment);
    
    // Count by delivery status
    long countByDeliveryStatus(String deliveryStatus);
    
    // Count by date range
    long countByDeliveryDateBetween(Date startDate, Date endDate);
    
    // Count by date range and delivery status
    long countByDeliveryDateBetweenAndDeliveryStatus(Date startDate, Date endDate, String deliveryStatus);
    
    // Find by driver and date range
    List<ProofOfDelivery> findByDriverAndDeliveryDateBetweenOrderByDeliveryDateDesc(User driver, Date startDate, Date endDate);
    
    // Find by delivery method
    List<ProofOfDelivery> findByDeliveryMethodOrderByDeliveryDateDesc(String deliveryMethod);
    
    // Find by signature required
    List<ProofOfDelivery> findBySignatureRequiredOrderByDeliveryDateDesc(Boolean signatureRequired);
    
    // Find by photo required
    List<ProofOfDelivery> findByPhotoRequiredOrderByDeliveryDateDesc(Boolean photoRequired);
    
    // Find by ID verification required
    List<ProofOfDelivery> findByIdVerificationRequiredOrderByDeliveryDateDesc(Boolean idVerificationRequired);
    
    // Find failed deliveries
    List<ProofOfDelivery> findByDeliveryStatusAndFailureReasonIsNotNullOrderByDeliveryDateDesc(String deliveryStatus);
    
    // Find completed deliveries with signature
    List<ProofOfDelivery> findByDeliveryStatusAndRecipientSignatureIsNotNullOrderByDeliveryDateDesc(String deliveryStatus);
    
    // Find completed deliveries with photo
    List<ProofOfDelivery> findByDeliveryStatusAndDeliveryPhotoUrlIsNotNullOrderByDeliveryDateDesc(String deliveryStatus);
    
    // Custom query for delivery statistics
    @Query("SELECT COUNT(p) FROM ProofOfDelivery p WHERE p.deliveryDate >= :startDate AND p.deliveryDate <= :endDate")
    long countDeliveriesInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Custom query for successful deliveries in date range
    @Query("SELECT COUNT(p) FROM ProofOfDelivery p WHERE p.deliveryDate >= :startDate AND p.deliveryDate <= :endDate AND p.deliveryStatus = 'COMPLETED'")
    long countSuccessfulDeliveriesInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Custom query for failed deliveries in date range
    @Query("SELECT COUNT(p) FROM ProofOfDelivery p WHERE p.deliveryDate >= :startDate AND p.deliveryDate <= :endDate AND p.deliveryStatus = 'FAILED'")
    long countFailedDeliveriesInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Custom query for driver performance
    @Query("SELECT p FROM ProofOfDelivery p WHERE p.driver = :driver AND p.deliveryDate >= :startDate AND p.deliveryDate <= :endDate ORDER BY p.deliveryDate DESC")
    List<ProofOfDelivery> findDriverDeliveriesInDateRange(@Param("driver") User driver, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Custom query for delivery success rate by driver
    @Query("SELECT COUNT(p) FROM ProofOfDelivery p WHERE p.driver = :driver AND p.deliveryStatus = 'COMPLETED'")
    long countSuccessfulDeliveriesByDriver(@Param("driver") User driver);
    
    // Custom query for total deliveries by driver
    @Query("SELECT COUNT(p) FROM ProofOfDelivery p WHERE p.driver = :driver")
    long countTotalDeliveriesByDriver(@Param("driver") User driver);
    
    // Custom query for average delivery time
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, p.createdAt, p.deliveryDate)) FROM ProofOfDelivery p WHERE p.deliveryStatus = 'COMPLETED' AND p.deliveryDate >= :startDate AND p.deliveryDate <= :endDate")
    Double getAverageDeliveryTimeInMinutes(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Custom query for delivery methods distribution
    @Query("SELECT p.deliveryMethod, COUNT(p) FROM ProofOfDelivery p WHERE p.deliveryDate >= :startDate AND p.deliveryDate <= :endDate GROUP BY p.deliveryMethod")
    List<Object[]> getDeliveryMethodsDistribution(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Custom query for failure reasons
    @Query("SELECT p.failureReason, COUNT(p) FROM ProofOfDelivery p WHERE p.deliveryStatus = 'FAILED' AND p.deliveryDate >= :startDate AND p.deliveryDate <= :endDate GROUP BY p.failureReason ORDER BY COUNT(p) DESC")
    List<Object[]> getFailureReasonsDistribution(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Driver workboard methods
    List<ProofOfDelivery> findByDriverIdAndDeliveryDateAfter(Long driverId, Date date);
    
    Optional<ProofOfDelivery> findByShipmentId(Long shipmentId);
}

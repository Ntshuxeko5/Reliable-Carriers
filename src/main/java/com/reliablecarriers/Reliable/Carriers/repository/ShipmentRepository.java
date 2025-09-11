package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    List<Shipment> findBySender(User sender);
    
    List<Shipment> findByAssignedDriver(User driver);
    
    List<Shipment> findByStatus(ShipmentStatus status);
    
    List<Shipment> findByCreatedAtBetween(Date startDate, Date endDate);
    
    List<Shipment> findByEstimatedDeliveryDateBetween(Date startDate, Date endDate);
    
    List<Shipment> findByPickupCityAndPickupState(String city, String state);
    
    List<Shipment> findByDeliveryCityAndDeliveryState(String city, String state);
    
    // Customer package management methods
    @Query("SELECT s FROM Shipment s WHERE s.sender.email = :email OR s.recipientEmail = :email")
    List<Shipment> findBySenderEmailOrRecipientEmail(@Param("email") String email);
    
    @Query("SELECT s FROM Shipment s WHERE s.sender.email = :email OR s.recipientEmail = :email ORDER BY s.createdAt DESC")
    List<Shipment> findBySenderEmailOrRecipientEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    @Query("SELECT s FROM Shipment s WHERE s.sender.phone = :phone OR s.recipientPhone = :phone")
    List<Shipment> findBySenderPhoneOrRecipientPhone(@Param("phone") String phone);
    
    // Driver workboard methods
    List<Shipment> findByAssignedDriverId(Long driverId);
    
    List<Shipment> findByStatusAndAssignedDriverIsNull(ShipmentStatus status);
    
    List<Shipment> findByAssignedDriverIdAndStatusAndUpdatedAtAfter(Long driverId, ShipmentStatus status, Date date);
    
    // Analytics methods
    long countByStatus(ShipmentStatus status);
    
    long countByStatusIn(List<ShipmentStatus> statuses);
    
    long countByCreatedAtBetween(Date startDate, Date endDate);
    
    long countByStatusAndCreatedAtBetween(ShipmentStatus status, Date startDate, Date endDate);
    
    @Query("SELECT CONCAT(u.firstName, ' ', u.lastName) as driverName, COUNT(s) as deliveries, " +
           "AVG(f.overallRating) as avgRating, " +
           "(COUNT(CASE WHEN s.status = 'DELIVERED' THEN 1 END) * 100.0 / COUNT(s)) as completionRate " +
           "FROM Shipment s LEFT JOIN s.assignedDriver u LEFT JOIN CustomerFeedback f ON f.shipment.id = s.id " +
           "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY u.id, u.firstName, u.lastName " +
           "ORDER BY deliveries DESC")
    List<Object[]> getDriverPerformanceStats(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT s.deliveryCity, COUNT(s) FROM Shipment s " +
           "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY s.deliveryCity ORDER BY COUNT(s) DESC")
    List<Object[]> getShipmentDistributionByLocation(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT HOUR(s.createdAt), COUNT(s) FROM Shipment s " +
           "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY HOUR(s.createdAt) ORDER BY HOUR(s.createdAt)")
    List<Object[]> getShipmentDistributionByHour(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT s.serviceType, COUNT(s), SUM(s.shippingCost), " +
           "((COUNT(s) - LAG(COUNT(s)) OVER (ORDER BY s.serviceType)) * 100.0 / LAG(COUNT(s)) OVER (ORDER BY s.serviceType)) as growth " +
           "FROM Shipment s WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY s.serviceType")
    List<Object[]> getServicePerformanceStats(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT DATE(s.createdAt), COUNT(s) FROM Shipment s " +
           "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(s.createdAt) ORDER BY DATE(s.createdAt)")
    List<Object[]> getDailyShipmentCounts(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
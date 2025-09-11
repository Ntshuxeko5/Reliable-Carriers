package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MovingServiceRepository extends JpaRepository<MovingService, Long> {
    
    // Find by customer
    List<MovingService> findByCustomerOrderByCreatedAtDesc(User customer);
    
    // Find by driver
    List<MovingService> findByDriverOrderByCreatedAtDesc(User driver);
    
    // Find by service type
    List<MovingService> findByServiceTypeOrderByCreatedAtDesc(ServiceType serviceType);
    
    // Find by status
    List<MovingService> findByStatusOrderByCreatedAtDesc(ShipmentStatus status);
    
    // Find by customer and status
    List<MovingService> findByCustomerAndStatusOrderByCreatedAtDesc(User customer, ShipmentStatus status);
    
    // Find by driver and status
    List<MovingService> findByDriverAndStatusOrderByCreatedAtDesc(User driver, ShipmentStatus status);
    
    // Find by date range
    List<MovingService> findByCreatedAtBetweenOrderByCreatedAtDesc(Date startDate, Date endDate);
    
    // Find by scheduled date range
    List<MovingService> findByScheduledDateBetweenOrderByScheduledDateAsc(Date startDate, Date endDate);
    
    // Find by distance range
    List<MovingService> findByDistanceKmBetweenOrderByDistanceKmAsc(Double minDistance, Double maxDistance);
    
    // Find by price range
    List<MovingService> findByTotalPriceBetweenOrderByTotalPriceAsc(Double minPrice, Double maxPrice);
    
    // Find pending services (no driver assigned)
    List<MovingService> findByDriverIsNullAndStatusOrderByCreatedAtAsc(ShipmentStatus status);
    
    // Find services by location (city/area)
    @Query("SELECT ms FROM MovingService ms WHERE " +
           "LOWER(ms.pickupAddress) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(ms.deliveryAddress) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "ORDER BY ms.createdAt DESC")
    List<MovingService> findByLocationContaining(@Param("location") String location);
    
    // Find services by weight range
    List<MovingService> findByWeightKgBetweenOrderByWeightKgAsc(Double minWeight, Double maxWeight);
    
    // Find services by number of items
    List<MovingService> findByNumberOfItemsBetweenOrderByNumberOfItemsAsc(Integer minItems, Integer maxItems);
    
    // Find today's services
    @Query("SELECT ms FROM MovingService ms WHERE DATE(ms.scheduledDate) = DATE(:date) ORDER BY ms.scheduledDate ASC")
    List<MovingService> findByScheduledDateToday(@Param("date") Date date);
    
    // Find services for a specific date
    @Query("SELECT ms FROM MovingService ms WHERE DATE(ms.scheduledDate) = DATE(:date) ORDER BY ms.scheduledDate ASC")
    List<MovingService> findByScheduledDateOn(@Param("date") Date date);
    
    // Find completed services in date range
    List<MovingService> findByStatusAndCompletedDateBetweenOrderByCompletedDateDesc(
        ShipmentStatus status, Date startDate, Date endDate);
    
    // Find services by customer email
    @Query("SELECT ms FROM MovingService ms WHERE ms.customer.email = :email ORDER BY ms.createdAt DESC")
    List<MovingService> findByCustomerEmail(@Param("email") String email);
    
    // Find services by customer phone
    @Query("SELECT ms FROM MovingService ms WHERE ms.customer.phone = :phone ORDER BY ms.createdAt DESC")
    List<MovingService> findByCustomerPhone(@Param("phone") String phone);
    
    // Count services by status
    long countByStatus(ShipmentStatus status);
    
    // Count services by service type
    long countByServiceType(ServiceType serviceType);
    
    // Count services by customer
    long countByCustomer(User customer);
    
    // Count services by driver
    long countByDriver(User driver);
    
    // Get total revenue for a date range
    @Query("SELECT SUM(ms.totalPrice) FROM MovingService ms WHERE ms.status = 'DELIVERED' AND ms.completedDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueForPeriod(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Get average service price
    @Query("SELECT AVG(ms.totalPrice) FROM MovingService ms WHERE ms.status = 'DELIVERED'")
    Double getAverageServicePrice();
    
    // Get services with highest prices
    @Query("SELECT ms FROM MovingService ms ORDER BY ms.totalPrice DESC")
    List<MovingService> findTopByOrderByTotalPriceDesc();
    
    // Get services with longest distances
    @Query("SELECT ms FROM MovingService ms ORDER BY ms.distanceKm DESC")
    List<MovingService> findTopByOrderByDistanceKmDesc();
}

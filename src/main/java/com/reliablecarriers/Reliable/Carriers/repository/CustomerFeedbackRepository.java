package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    
    /**
     * Find feedback by customer ID
     */
    List<CustomerFeedback> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    /**
     * Find feedback by driver ID
     */
    List<CustomerFeedback> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    
    /**
     * Find feedback by shipment ID
     */
    List<CustomerFeedback> findByShipmentIdOrderByCreatedAtDesc(Long shipmentId);
    
    /**
     * Find feedback by rating range
     */
    List<CustomerFeedback> findByOverallRatingBetweenOrderByCreatedAtDesc(Integer minRating, Integer maxRating);
    
    /**
     * Find feedback by type
     */
    List<CustomerFeedback> findByFeedbackTypeOrderByCreatedAtDesc(String feedbackType);
    
    /**
     * Find unresolved feedback
     */
    List<CustomerFeedback> findByIsResolvedFalseOrderByCreatedAtDesc();
    
    /**
     * Find feedback created after a specific date
     */
    List<CustomerFeedback> findByCreatedAtAfterOrderByCreatedAtDesc(Date date);
    
    /**
     * Find feedback by customer and driver
     */
    List<CustomerFeedback> findByCustomerIdAndDriverIdOrderByCreatedAtDesc(Long customerId, Long driverId);
    
    /**
     * Get average rating for a driver
     */
    @Query("SELECT AVG(f.overallRating) FROM CustomerFeedback f WHERE f.driverId = :driverId")
    Double getAverageRatingByDriverId(@Param("driverId") Long driverId);
    
    /**
     * Get average rating for the company
     */
    @Query("SELECT AVG(f.overallRating) FROM CustomerFeedback f")
    Double getOverallAverageRating();
    
    /**
     * Get feedback count by rating
     */
    @Query("SELECT f.overallRating, COUNT(f) FROM CustomerFeedback f GROUP BY f.overallRating ORDER BY f.overallRating DESC")
    List<Object[]> getFeedbackCountByRating();
    
    /**
     * Get recent feedback (last 30 days)
     */
    @Query("SELECT f FROM CustomerFeedback f WHERE f.createdAt >= :startDate ORDER BY f.createdAt DESC")
    List<CustomerFeedback> getRecentFeedback(@Param("startDate") Date startDate);
    
    // Analytics methods
    @Query("SELECT AVG(f.overallRating) FROM CustomerFeedback f WHERE f.createdAt BETWEEN :startDate AND :endDate")
    Double getAverageRatingByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT f.overallRating, COUNT(f) FROM CustomerFeedback f WHERE f.createdAt BETWEEN :startDate AND :endDate GROUP BY f.overallRating ORDER BY f.overallRating DESC")
    List<Object[]> getRatingDistributionByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}

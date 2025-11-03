package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.Quote;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    
    /**
     * Find quote by quote ID
     */
    Optional<Quote> findByQuoteId(String quoteId);
    
    /**
     * Find quotes by customer email, ordered by creation date descending
     */
    List<Quote> findByCustomerEmailOrderByCreatedAtDesc(String email);
    
    /**
     * Find active quotes by customer email
     */
    List<Quote> findByCustomerEmailAndIsActiveTrueOrderByCreatedAtDesc(String email);
    
    /**
     * Find quotes by service type
     */
    List<Quote> findByServiceTypeOrderByCreatedAtDesc(ServiceType serviceType);
    
    /**
     * Find active quotes by service type
     */
    List<Quote> findByServiceTypeAndIsActiveTrueOrderByCreatedAtDesc(ServiceType serviceType);
    
    /**
     * Find quotes created between dates
     */
    List<Quote> findByCreatedAtBetweenOrderByCreatedAtDesc(Date startDate, Date endDate);
    
    /**
     * Find active quotes created between dates
     */
    List<Quote> findByCreatedAtBetweenAndIsActiveTrueOrderByCreatedAtDesc(Date startDate, Date endDate);
    
    /**
     * Find expired quotes
     */
    @Query("SELECT q FROM Quote q WHERE q.expiryDate < :currentDate AND q.isActive = true")
    List<Quote> findExpiredQuotes(@Param("currentDate") Date currentDate);
    
    /**
     * Find quotes expiring soon (within specified days)
     */
    @Query("SELECT q FROM Quote q WHERE q.expiryDate BETWEEN :currentDate AND :expiryThreshold AND q.isActive = true")
    List<Quote> findQuotesExpiringSoon(@Param("currentDate") Date currentDate, @Param("expiryThreshold") Date expiryThreshold);
    
    /**
     * Count active quotes by customer email
     */
    long countByCustomerEmailAndIsActiveTrue(String email);
    
    /**
     * Count quotes by service type
     */
    long countByServiceType(ServiceType serviceType);
    
    /**
     * Count active quotes by service type
     */
    long countByServiceTypeAndIsActiveTrue(ServiceType serviceType);
    
    /**
     * Find quotes by pickup city
     */
    List<Quote> findByPickupCityOrderByCreatedAtDesc(String city);
    
    /**
     * Find quotes by delivery city
     */
    List<Quote> findByDeliveryCityOrderByCreatedAtDesc(String city);
    
    /**
     * Find quotes by pickup and delivery cities
     */
    List<Quote> findByPickupCityAndDeliveryCityOrderByCreatedAtDesc(String pickupCity, String deliveryCity);
    
    /**
     * Find quotes with total cost within range
     */
    List<Quote> findByTotalCostBetweenOrderByCreatedAtDesc(java.math.BigDecimal minCost, java.math.BigDecimal maxCost);
    
    /**
     * Find active quotes with total cost within range
     */
    List<Quote> findByTotalCostBetweenAndIsActiveTrueOrderByCreatedAtDesc(java.math.BigDecimal minCost, java.math.BigDecimal maxCost);
    
    /**
     * Get quote statistics by date range
     */
    @Query("SELECT q.serviceType, COUNT(q), AVG(q.totalCost) FROM Quote q " +
           "WHERE q.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY q.serviceType")
    List<Object[]> getQuoteStatisticsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Get revenue from quotes by date range
     */
    @Query("SELECT DATE(q.createdAt), SUM(q.totalCost) FROM Quote q " +
           "WHERE q.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(q.createdAt) ORDER BY DATE(q.createdAt)")
    List<Object[]> getRevenueByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}

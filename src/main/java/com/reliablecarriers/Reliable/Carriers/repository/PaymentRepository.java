package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.PaymentMethod;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    Optional<Payment> findByReference(String reference);
    
    List<Payment> findByBooking(Booking booking);
    
    List<Payment> findByShipment(Shipment shipment);
    
    List<Payment> findByUser(User user);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    List<Payment> findByPaymentDateBetween(Date startDate, Date endDate);
    
    List<Payment> findByAmountGreaterThanEqual(BigDecimal amount);
    
    List<Payment> findByAmountLessThanEqual(BigDecimal amount);
    
    // Analytics methods
    @Query("SELECT DATE(p.paymentDate), SUM(p.amount) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED' " +
           "GROUP BY DATE(p.paymentDate) ORDER BY DATE(p.paymentDate)")
    List<Object[]> getRevenueStatsByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT DATE(p.createdAt), SUM(p.amount) FROM Payment p " +
           "WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status = 'COMPLETED' " +
           "GROUP BY DATE(p.createdAt) ORDER BY DATE(p.createdAt)")
    List<Object[]> getDailyRevenueData(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
} 
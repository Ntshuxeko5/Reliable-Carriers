package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.BookingStatus;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Find booking by booking number
     */
    Optional<Booking> findByBookingNumber(String bookingNumber);
    
    /**
     * Find bookings by customer email, ordered by creation date descending
     */
    List<Booking> findByCustomerEmailOrderByCreatedAtDesc(String email);
    
    /**
     * Find bookings by customer email and status
     */
    List<Booking> findByCustomerEmailAndStatusOrderByCreatedAtDesc(String email, BookingStatus status);
    
    /**
     * Find bookings by status
     */
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);
    
    /**
     * Find bookings by service type
     */
    List<Booking> findByServiceTypeOrderByCreatedAtDesc(ServiceType serviceType);
    
    /**
     * Find bookings created between dates
     */
    List<Booking> findByCreatedAtBetweenOrderByCreatedAtDesc(Date startDate, Date endDate);
    
    /**
     * Find bookings by payment reference
     */
    Optional<Booking> findByPaymentReference(String paymentReference);
    
    /**
     * Find bookings by tracking number
     */
    Optional<Booking> findByTrackingNumber(String trackingNumber);
    
    /**
     * Find bookings by shipment ID
     */
    Optional<Booking> findByShipmentId(String shipmentId);
    
    /**
     * Count bookings by status
     */
    long countByStatus(BookingStatus status);
    
    /**
     * Count bookings by customer email
     */
    long countByCustomerEmail(String email);
    
    /**
     * Find pending bookings older than specified date
     */
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :date")
    List<Booking> findPendingBookingsOlderThan(@Param("status") BookingStatus status, @Param("date") Date date);
    
    /**
     * Find bookings requiring payment
     */
    @Query("SELECT b FROM Booking b WHERE b.status IN ('PENDING', 'PAYMENT_PENDING') AND b.paymentReference IS NULL")
    List<Booking> findBookingsRequiringPayment();
    
    /**
     * Find confirmed bookings ready for dispatch
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND b.driver IS NULL")
    List<Booking> findConfirmedBookingsReadyForDispatch();
    
    /**
     * Get booking statistics by customer email
     */
    @Query("SELECT COUNT(b) as total, " +
           "SUM(CASE WHEN b.status = 'PENDING' THEN 1 ELSE 0 END) as pending, " +
           "SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END) as confirmed, " +
           "SUM(CASE WHEN b.status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered, " +
           "SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled " +
           "FROM Booking b WHERE b.customerEmail = :email")
    Object[] getBookingStatisticsByEmail(@Param("email") String email);
    
    /**
     * Find bookings by driver ID
     */
    List<Booking> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    
    /**
     * Find bookings with pickup date in the future
     */
    @Query("SELECT b FROM Booking b WHERE b.pickupDate > :currentDate AND b.status IN ('CONFIRMED', 'ASSIGNED')")
    List<Booking> findUpcomingPickups(@Param("currentDate") Date currentDate);
    
    /**
     * Find bookings with delivery date in the future
     */
    @Query("SELECT b FROM Booking b WHERE b.deliveryDate > :currentDate AND b.status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY')")
    List<Booking> findUpcomingDeliveries(@Param("currentDate") Date currentDate);
}

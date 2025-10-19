package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.BookingRequest;
import com.reliablecarriers.Reliable.Carriers.dto.BookingResponse;
import com.reliablecarriers.Reliable.Carriers.model.BookingStatus;
import java.util.List;
import java.util.Map;

public interface BookingService {
    
    /**
     * Create a new booking
     */
    BookingResponse createBooking(BookingRequest request);
    
    /**
     * Get booking by ID
     */
    BookingResponse getBookingById(Long bookingId);
    
    /**
     * Get bookings by customer email
     */
    List<BookingResponse> getBookingsByEmail(String email);
    
    /**
     * Update booking details
     */
    BookingResponse updateBooking(Long bookingId, BookingRequest request);
    
    /**
     * Cancel a booking
     */
    boolean cancelBooking(Long bookingId);
    
    /**
     * Confirm booking after successful payment
     */
    BookingResponse confirmBooking(Long bookingId, String paymentReference);
    
    /**
     * Update booking status
     */
    void updateBookingStatus(Long bookingId, BookingStatus status);
    
    /**
     * Get booking status
     */
    BookingStatus getBookingStatus(Long bookingId);
    
    /**
     * Update booking payment reference
     */
    void updateBookingPaymentReference(Long bookingId, String paymentReference);
    
    /**
     * Calculate booking price
     */
    Map<String, Object> calculatePrice(BookingRequest request);
    
    /**
     * Get available service types with pricing
     */
    List<Map<String, Object>> getAvailableServices();
    
    /**
     * Check if booking can be cancelled
     */
    boolean canCancelBooking(Long bookingId);
    
    /**
     * Get booking statistics for customer
     */
    Map<String, Object> getBookingStatistics(String email);
}

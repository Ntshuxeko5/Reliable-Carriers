package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.BookingStatus;
import com.reliablecarriers.Reliable.Carriers.model.PaymentStatus;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class PaymentProcessingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    // GoogleMapsGeocodingService not used in this service
    
    @Autowired
    private GoogleMapsService googleMapsService;
    
    /**
     * Process payment and create booking with Google Maps integration
     */
    @Transactional
    public Map<String, Object> processPayment(Map<String, Object> paymentData) {
        try {
            // Extract payment data
            String reference = (String) paymentData.get("reference");
            String customerName = (String) paymentData.get("customerName");
            String customerEmail = (String) paymentData.get("customerEmail");
            String customerPhone = (String) paymentData.get("customerPhone");
            String serviceType = (String) paymentData.get("serviceType");
            // String distance = (String) paymentData.get("distance"); // Unused
            String weight = (String) paymentData.get("weight");
            String totalAmount = (String) paymentData.get("totalAmount");
            
            // Extract address data
            String pickupAddress = (String) paymentData.get("pickupAddress");
            String deliveryAddress = (String) paymentData.get("deliveryAddress");
            Double pickupLatitude = (Double) paymentData.get("pickupLatitude");
            Double pickupLongitude = (Double) paymentData.get("pickupLongitude");
            // String pickupPlaceId = (String) paymentData.get("pickupPlaceId"); // Unused
            Double deliveryLatitude = (Double) paymentData.get("deliveryLatitude");
            Double deliveryLongitude = (Double) paymentData.get("deliveryLongitude");
            // String deliveryPlaceId = (String) paymentData.get("deliveryPlaceId"); // Unused
            
            // Create booking
            Booking booking = new Booking();
            
            // Set basic information
            booking.setCustomerName(customerName);
            booking.setCustomerEmail(customerEmail);
            booking.setCustomerPhone(customerPhone);
            booking.setServiceType(ServiceType.valueOf(serviceType.replace(" ", "_").toUpperCase()));
            booking.setStatus(BookingStatus.PENDING);
            booking.setBookingNumber(generateBookingNumber());
            
            // Set package information
            booking.setDescription("Package from " + pickupAddress + " to " + deliveryAddress);
            booking.setWeight(parseWeight(weight));
            
            // Set addresses with Google Maps coordinates
            booking.setPickupAddress(pickupAddress);
            booking.setPickupLatitude(new BigDecimal(pickupLatitude));
            booking.setPickupLongitude(new BigDecimal(pickupLongitude));
            
            booking.setDeliveryAddress(deliveryAddress);
            booking.setDeliveryLatitude(new BigDecimal(deliveryLatitude));
            booking.setDeliveryLongitude(new BigDecimal(deliveryLongitude));
            
            // Set payment information
            booking.setTotalAmount(parseAmount(totalAmount));
            booking.setPaymentReference(reference);
            booking.setPaymentStatus(PaymentStatus.COMPLETED);
            booking.setPaymentDate(new Date());
            
            // Generate pickup and delivery codes
            String pickupCode = generatePickupCode();
            String deliveryCode = generateDeliveryCode();
            booking.setCustomerPickupCode(pickupCode);
            booking.setCustomerDeliveryCode(deliveryCode);
            
            // Calculate distance using Google Maps
            try {
                GoogleMapsService.DistanceResult distanceResult = googleMapsService.calculateDistance(
                    pickupAddress, deliveryAddress
                );
                if (distanceResult != null) {
                    // Distance calculation successful - could store in a separate table if needed
                    System.out.println("Distance calculated: " + distanceResult.getDistanceKm() + " km");
                }
            } catch (Exception e) {
                System.err.println("Failed to calculate distance: " + e.getMessage());
            }
            
            // Save booking
            Booking savedBooking = bookingRepository.save(booking);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bookingId", savedBooking.getId());
            response.put("bookingNumber", savedBooking.getBookingNumber());
            response.put("pickupCode", pickupCode);
            response.put("deliveryCode", deliveryCode);
            response.put("message", "Payment processed and booking created successfully");
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to process payment: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Generate unique booking number
     */
    private String generateBookingNumber() {
        return "RC" + System.currentTimeMillis() + (new Random().nextInt(9000) + 1000);
    }
    
    /**
     * Generate pickup code
     */
    private String generatePickupCode() {
        return "P" + (new Random().nextInt(900000) + 100000);
    }
    
    /**
     * Generate delivery code
     */
    private String generateDeliveryCode() {
        return "D" + (new Random().nextInt(900000) + 100000);
    }
    
    /**
     * Parse weight string to Double
     */
    private Double parseWeight(String weightStr) {
        try {
            return Double.parseDouble(weightStr.replace(" kg", "").trim());
        } catch (Exception e) {
            return 1.0; // Default weight
        }
    }
    
    /**
     * Parse amount string to BigDecimal
     */
    private BigDecimal parseAmount(String amountStr) {
        try {
            return new BigDecimal(amountStr.replace("R", "").replace(",", "").trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Get booking details for payment success page
     */
    public Map<String, Object> getBookingDetails(Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            Map<String, Object> details = new HashMap<>();
            details.put("bookingNumber", booking.getBookingNumber());
            details.put("customerName", booking.getCustomerName());
            details.put("customerEmail", booking.getCustomerEmail());
            details.put("serviceType", booking.getServiceType());
            details.put("pickupAddress", booking.getPickupAddress());
            details.put("deliveryAddress", booking.getDeliveryAddress());
            details.put("totalAmount", booking.getTotalAmount());
            details.put("pickupCode", booking.getCustomerPickupCode());
            details.put("deliveryCode", booking.getCustomerDeliveryCode());
            details.put("createdAt", booking.getCreatedAt());
            details.put("status", booking.getStatus());
            
            return details;
        } catch (Exception e) {
            return null;
        }
    }
}

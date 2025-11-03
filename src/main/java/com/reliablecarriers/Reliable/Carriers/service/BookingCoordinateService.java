package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.AddressCoordinates;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.BookingStatus;
import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class BookingCoordinateService {
    
    @Autowired
    private GoogleMapsGeocodingService geocodingService;
    
    @Autowired
    private GoogleMapsService googleMapsService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Process a customer package request with Google Maps integration
     */
    @Transactional
    public Booking processBookingWithCoordinates(CustomerPackageRequest request) {
        // Get coordinates for both addresses
        Map<String, AddressCoordinates> coordinates = geocodingService.getCoordinatesForBooking(
            request.getPickupAddress(), 
            request.getDeliveryAddress()
        );
        
        // Create booking with coordinates
        Booking booking = new Booking();
        
        // Set basic booking information
        booking.setCustomerName(request.getSenderName());
        booking.setCustomerEmail(request.getSenderEmail());
        booking.setCustomerPhone(request.getSenderPhone());
        booking.setDescription(request.getDescription());
        booking.setWeight(request.getWeight());
        booking.setDimensions(request.getDimensions());
        booking.setServiceType(ServiceType.valueOf(request.getServiceType()));
        booking.setSpecialInstructions("");
        
        // Set addresses with Google Maps validation
        AddressCoordinates pickupCoords = coordinates.get("pickup");
        if (pickupCoords != null) {
            booking.setPickupAddress(pickupCoords.getFormattedAddress());
            booking.setPickupLatitude(pickupCoords.getLatitude());
            booking.setPickupLongitude(pickupCoords.getLongitude());
        } else {
            // Fallback to original address if geocoding fails
            booking.setPickupAddress(request.getPickupAddress());
        }
        
        AddressCoordinates deliveryCoords = coordinates.get("delivery");
        if (deliveryCoords != null) {
            booking.setDeliveryAddress(deliveryCoords.getFormattedAddress());
            booking.setDeliveryLatitude(deliveryCoords.getLatitude());
            booking.setDeliveryLongitude(deliveryCoords.getLongitude());
        } else {
            // Fallback to original address if geocoding fails
            booking.setDeliveryAddress(request.getDeliveryAddress());
        }
        
        // Calculate accurate distance using Google Maps
        if (pickupCoords != null && deliveryCoords != null) {
            try {
                GoogleMapsService.DistanceResult distanceResult = googleMapsService.calculateDistance(
                    pickupCoords.getFormattedAddress(),
                    deliveryCoords.getFormattedAddress()
                );
                
                if (distanceResult != null) {
                    // Distance calculation successful - could store in a separate table if needed
                    System.out.println("Distance calculated: " + distanceResult.getDistanceKm() + " km");
                }
            } catch (Exception e) {
                System.err.println("Failed to calculate distance: " + e.getMessage());
            }
        }
        
        // Set initial status
        booking.setStatus(BookingStatus.PENDING);
        
        return booking;
    }
    
    /**
     * Update booking coordinates when addresses are modified
     */
    @Transactional
    public Booking updateBookingCoordinates(Long bookingId, String pickupAddress, String deliveryAddress) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Update pickup coordinates
        if (pickupAddress != null && !pickupAddress.equals(booking.getPickupAddress())) {
            AddressCoordinates pickupCoords = geocodingService.validateAndNormalizeAddress(pickupAddress);
            if (pickupCoords != null) {
                booking.setPickupAddress(pickupCoords.getFormattedAddress());
                booking.setPickupLatitude(pickupCoords.getLatitude());
                booking.setPickupLongitude(pickupCoords.getLongitude());
            }
        }
        
        // Update delivery coordinates
        if (deliveryAddress != null && !deliveryAddress.equals(booking.getDeliveryAddress())) {
            AddressCoordinates deliveryCoords = geocodingService.validateAndNormalizeAddress(deliveryAddress);
            if (deliveryCoords != null) {
                booking.setDeliveryAddress(deliveryCoords.getFormattedAddress());
                booking.setDeliveryLatitude(deliveryCoords.getLatitude());
                booking.setDeliveryLongitude(deliveryCoords.getLongitude());
            }
        }
        
        // Recalculate distance if both addresses are updated
        if (booking.getPickupLatitude() != null && booking.getDeliveryLatitude() != null) {
            try {
                GoogleMapsService.DistanceResult distanceResult = googleMapsService.calculateDistance(
                    booking.getPickupAddress(),
                    booking.getDeliveryAddress()
                );
                
                if (distanceResult != null) {
                    // Distance calculation successful - could store in a separate table if needed
                    System.out.println("Distance calculated: " + distanceResult.getDistanceKm() + " km");
                }
            } catch (Exception e) {
                System.err.println("Failed to recalculate distance: " + e.getMessage());
            }
        }
        
        return bookingRepository.save(booking);
    }
    
    /**
     * Validate addresses and get coordinates for quote calculation
     */
    public Map<String, AddressCoordinates> validateAddressesForQuote(String pickupAddress, String deliveryAddress) {
        return geocodingService.getCoordinatesForBooking(pickupAddress, deliveryAddress);
    }
    
    /**
     * Get coordinates for driver navigation
     */
    public Map<String, BigDecimal[]> getDriverNavigationCoordinates(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        Map<String, BigDecimal[]> coordinates = new java.util.HashMap<>();
        
        if (booking.getPickupLatitude() != null && booking.getPickupLongitude() != null) {
            coordinates.put("pickup", new BigDecimal[]{
                booking.getPickupLatitude(), 
                booking.getPickupLongitude()
            });
        }
        
        if (booking.getDeliveryLatitude() != null && booking.getDeliveryLongitude() != null) {
            coordinates.put("delivery", new BigDecimal[]{
                booking.getDeliveryLatitude(), 
                booking.getDeliveryLongitude()
            });
        }
        
        return coordinates;
    }
    
    /**
     * Check if booking has valid coordinates for navigation
     */
    public boolean hasValidCoordinates(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        return booking.getPickupLatitude() != null && 
               booking.getPickupLongitude() != null &&
               booking.getDeliveryLatitude() != null && 
               booking.getDeliveryLongitude() != null;
    }
}

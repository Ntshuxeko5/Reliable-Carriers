package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.AddressCoordinates;
import com.reliablecarriers.Reliable.Carriers.dto.CustomerPackageRequest;
import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.service.BookingCoordinateService;
import com.reliablecarriers.Reliable.Carriers.service.GoogleMapsGeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingCoordinateController {
    
    @Autowired
    private BookingCoordinateService bookingCoordinateService;
    
    @Autowired
    private GoogleMapsGeocodingService geocodingService;
    
    /**
     * Validate addresses and get coordinates for quote calculation
     */
    @PostMapping("/validate-addresses")
    public ResponseEntity<Map<String, Object>> validateAddresses(@RequestBody Map<String, String> request) {
        String pickupAddress = request.get("pickupAddress");
        String deliveryAddress = request.get("deliveryAddress");
        
        Map<String, AddressCoordinates> coordinates = bookingCoordinateService.validateAddressesForQuote(
            pickupAddress, deliveryAddress
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("coordinates", coordinates);
        
        // Check if both addresses are valid
        boolean bothValid = coordinates.containsKey("pickup") && coordinates.containsKey("delivery");
        response.put("bothAddressesValid", bothValid);
        
        if (bothValid) {
            response.put("message", "Both addresses are valid and coordinates captured");
        } else {
            response.put("message", "Some addresses could not be validated");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Process booking with Google Maps coordinates
     */
    @PostMapping("/process-with-coordinates")
    public ResponseEntity<Map<String, Object>> processBookingWithCoordinates(@RequestBody CustomerPackageRequest request) {
        try {
            Booking booking = bookingCoordinateService.processBookingWithCoordinates(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("booking", booking);
            response.put("message", "Booking processed successfully with coordinates");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to process booking: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update booking coordinates
     */
    @PutMapping("/{bookingId}/coordinates")
    public ResponseEntity<Map<String, Object>> updateBookingCoordinates(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request) {
        try {
            String pickupAddress = request.get("pickupAddress");
            String deliveryAddress = request.get("deliveryAddress");
            
            Booking booking = bookingCoordinateService.updateBookingCoordinates(
                bookingId, pickupAddress, deliveryAddress
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("booking", booking);
            response.put("message", "Booking coordinates updated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to update coordinates: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get coordinates for driver navigation
     */
    @GetMapping("/{bookingId}/navigation-coordinates")
    public ResponseEntity<Map<String, Object>> getNavigationCoordinates(@PathVariable Long bookingId) {
        try {
            Map<String, BigDecimal[]> coordinates = bookingCoordinateService.getDriverNavigationCoordinates(bookingId);
            boolean hasValidCoordinates = bookingCoordinateService.hasValidCoordinates(bookingId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("coordinates", coordinates);
            response.put("hasValidCoordinates", hasValidCoordinates);
            
            if (hasValidCoordinates) {
                response.put("message", "Coordinates available for navigation");
            } else {
                response.put("message", "Coordinates not available for navigation");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get coordinates: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Geocode a single address
     */
    @PostMapping("/geocode")
    public ResponseEntity<Map<String, Object>> geocodeAddress(@RequestBody Map<String, String> request) {
        String address = request.get("address");
        
        try {
            AddressCoordinates coordinates = geocodingService.validateAndNormalizeAddress(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", coordinates != null);
            response.put("coordinates", coordinates);
            
            if (coordinates != null) {
                response.put("message", "Address geocoded successfully");
            } else {
                response.put("message", "Could not geocode address");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Geocoding failed: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}

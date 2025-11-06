package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.BookingRepository;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for live package tracking with real-time location updates
 */
@RestController
@RequestMapping("/api/customer/live-tracking")
@CrossOrigin(origins = "*")
public class CustomerLiveTrackingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    /**
     * Get live tracking data for a package
     */
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<?> getLiveTracking(@PathVariable String trackingNumber) {
        try {
            // Find booking or shipment by tracking number
            Optional<Booking> bookingOpt = bookingRepository.findByTrackingNumber(trackingNumber);
            Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);

            if (bookingOpt.isEmpty() && shipmentOpt.isEmpty()) {
                return ResponseEntity.status(404)
                    .body(Map.of("success", false, "message", "Tracking number not found"));
            }

            Map<String, Object> trackingData = new HashMap<>();
            
            // Get driver location if assigned
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                trackingData.put("trackingNumber", booking.getTrackingNumber());
                trackingData.put("status", booking.getStatus().toString());
                trackingData.put("pickupAddress", booking.getPickupAddress());
                trackingData.put("deliveryAddress", booking.getDeliveryAddress());
                
                // Get driver location if assigned
                if (booking.getDriver() != null) {
                    User driver = booking.getDriver();
                    if (driver.getCurrentLatitude() != null && driver.getCurrentLongitude() != null) {
                        Map<String, Object> driverLocation = new HashMap<>();
                        driverLocation.put("latitude", driver.getCurrentLatitude());
                        driverLocation.put("longitude", driver.getCurrentLongitude());
                        driverLocation.put("name", driver.getFirstName() + " " + driver.getLastName());
                        driverLocation.put("lastUpdate", driver.getLastLocationUpdate());
                        trackingData.put("driverLocation", driverLocation);
                    }
                }
                
                trackingData.put("pickupLat", booking.getPickupLatitude());
                trackingData.put("pickupLng", booking.getPickupLongitude());
                trackingData.put("deliveryLat", booking.getDeliveryLatitude());
                trackingData.put("deliveryLng", booking.getDeliveryLongitude());
            } else if (shipmentOpt.isPresent()) {
                Shipment shipment = shipmentOpt.get();
                trackingData.put("trackingNumber", shipment.getTrackingNumber());
                trackingData.put("status", shipment.getStatus().toString());
                trackingData.put("pickupAddress", shipment.getPickupAddress());
                trackingData.put("deliveryAddress", shipment.getDeliveryAddress());
                
                // Get driver location if assigned
                if (shipment.getAssignedDriver() != null) {
                    User driver = shipment.getAssignedDriver();
                    if (driver.getCurrentLatitude() != null && driver.getCurrentLongitude() != null) {
                        Map<String, Object> driverLocation = new HashMap<>();
                        driverLocation.put("latitude", driver.getCurrentLatitude());
                        driverLocation.put("longitude", driver.getCurrentLongitude());
                        driverLocation.put("name", driver.getFirstName() + " " + driver.getLastName());
                        driverLocation.put("lastUpdate", driver.getLastLocationUpdate());
                        trackingData.put("driverLocation", driverLocation);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("success", true, "data", trackingData));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error fetching tracking data: " + e.getMessage()));
        }
    }
}

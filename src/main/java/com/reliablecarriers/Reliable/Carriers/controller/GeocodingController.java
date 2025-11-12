package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.dto.AddressCoordinates;
import com.reliablecarriers.Reliable.Carriers.service.GoogleMapsGeocodingService;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentGeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for geocoding addresses
 * Provides server-side geocoding for frontend use
 */
@RestController
@RequestMapping("/api/geocoding")
@CrossOrigin(origins = "*")
public class GeocodingController {

    private final GoogleMapsGeocodingService geocodingService;
    private final ShipmentGeocodingService shipmentGeocodingService;

    @Autowired
    public GeocodingController(GoogleMapsGeocodingService geocodingService,
                              ShipmentGeocodingService shipmentGeocodingService) {
        this.geocodingService = geocodingService;
        this.shipmentGeocodingService = shipmentGeocodingService;
    }

    /**
     * Geocode a single address
     * POST /api/geocoding/address
     * Body: { "address": "123 Main St, Johannesburg, South Africa" }
     */
    @PostMapping("/address")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRACKING_MANAGER', 'DRIVER', 'CUSTOMER')")
    public ResponseEntity<Map<String, Object>> geocodeAddress(@RequestBody Map<String, String> request) {
        String address = request.get("address");
        
        if (address == null || address.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Address is required"
            ));
        }
        
        try {
            AddressCoordinates coords = geocodingService.validateAndNormalizeAddress(address);
            
            if (coords != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "latitude", coords.getLatitude(),
                    "longitude", coords.getLongitude(),
                    "formattedAddress", coords.getFormattedAddress(),
                    "placeId", coords.getPlaceId() != null ? coords.getPlaceId() : ""
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "Could not geocode address"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Geocoding failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Geocode pickup and delivery addresses
     * POST /api/geocoding/addresses
     * Body: { "pickupAddress": "...", "deliveryAddress": "..." }
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRACKING_MANAGER', 'DRIVER', 'CUSTOMER')")
    public ResponseEntity<Map<String, Object>> geocodeAddresses(@RequestBody Map<String, String> request) {
        String pickupAddress = request.get("pickupAddress");
        String deliveryAddress = request.get("deliveryAddress");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        // Geocode pickup address
        if (pickupAddress != null && !pickupAddress.trim().isEmpty()) {
            try {
                AddressCoordinates pickupCoords = geocodingService.validateAndNormalizeAddress(pickupAddress);
                if (pickupCoords != null) {
                    result.put("pickup", Map.of(
                        "latitude", pickupCoords.getLatitude(),
                        "longitude", pickupCoords.getLongitude(),
                        "formattedAddress", pickupCoords.getFormattedAddress(),
                        "placeId", pickupCoords.getPlaceId() != null ? pickupCoords.getPlaceId() : ""
                    ));
                } else {
                    result.put("pickup", Map.of("error", "Could not geocode pickup address"));
                }
            } catch (Exception e) {
                result.put("pickup", Map.of("error", "Geocoding failed: " + e.getMessage()));
            }
        }
        
        // Geocode delivery address
        if (deliveryAddress != null && !deliveryAddress.trim().isEmpty()) {
            try {
                AddressCoordinates deliveryCoords = geocodingService.validateAndNormalizeAddress(deliveryAddress);
                if (deliveryCoords != null) {
                    result.put("delivery", Map.of(
                        "latitude", deliveryCoords.getLatitude(),
                        "longitude", deliveryCoords.getLongitude(),
                        "formattedAddress", deliveryCoords.getFormattedAddress(),
                        "placeId", deliveryCoords.getPlaceId() != null ? deliveryCoords.getPlaceId() : ""
                    ));
                } else {
                    result.put("delivery", Map.of("error", "Could not geocode delivery address"));
                }
            } catch (Exception e) {
                result.put("delivery", Map.of("error", "Geocoding failed: " + e.getMessage()));
            }
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Manually trigger geocoding for a specific shipment
     * POST /api/geocoding/shipment/{shipmentId}
     */
    @PostMapping("/shipment/{shipmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRACKING_MANAGER')")
    public ResponseEntity<Map<String, Object>> geocodeShipment(@PathVariable Long shipmentId) {
        try {
            shipmentGeocodingService.geocodeShipment(shipmentId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Shipment geocoded successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to geocode shipment: " + e.getMessage()
            ));
        }
    }

    /**
     * Manually trigger background geocoding job
     * POST /api/geocoding/background-job
     */
    @PostMapping("/background-job")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> triggerBackgroundGeocoding() {
        try {
            // Run in background thread to avoid blocking
            new Thread(() -> {
                try {
                    shipmentGeocodingService.geocodeMissingCoordinates();
                } catch (Exception e) {
                    System.err.println("Background geocoding job failed: " + e.getMessage());
                }
            }).start();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Background geocoding job started"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to start background job: " + e.getMessage()
            ));
        }
    }
}


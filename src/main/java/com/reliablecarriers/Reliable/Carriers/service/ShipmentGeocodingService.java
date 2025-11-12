package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.AddressCoordinates;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Background service to geocode Shipment addresses that don't have coordinates
 * Runs periodically to ensure all shipments have geocoded addresses
 */
@Service
public class ShipmentGeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentGeocodingService.class);

    private final ShipmentRepository shipmentRepository;
    private final GoogleMapsGeocodingService geocodingService;

    @Autowired
    public ShipmentGeocodingService(ShipmentRepository shipmentRepository,
                                    GoogleMapsGeocodingService geocodingService) {
        this.shipmentRepository = shipmentRepository;
        this.geocodingService = geocodingService;
    }

    /**
     * Geocode all shipments that are missing coordinates
     * Runs every hour to catch any shipments that were created before geocoding was implemented
     */
    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    @Transactional
    public void geocodeMissingCoordinates() {
        logger.info("Starting background geocoding job for shipments without coordinates");
        
        // Find shipments missing coordinates (either pickup or delivery)
        List<Shipment> missingCoords = shipmentRepository.findByPickupLatitudeIsNullOrDeliveryLatitudeIsNull();
        logger.info("Found {} shipments missing coordinates", missingCoords.size());
        
        int geocodedCount = 0;
        for (Shipment shipment : missingCoords) {
            try {
                boolean hadPickup = shipment.getPickupLatitude() != null;
                boolean hadDelivery = shipment.getDeliveryLatitude() != null;
                
                geocodeShipmentAddresses(shipment);
                
                boolean nowHasPickup = shipment.getPickupLatitude() != null;
                boolean nowHasDelivery = shipment.getDeliveryLatitude() != null;
                
                if ((!hadPickup && nowHasPickup) || (!hadDelivery && nowHasDelivery)) {
                    shipmentRepository.save(shipment);
                    geocodedCount++;
                    logger.debug("Geocoded shipment: {}", shipment.getTrackingNumber());
                }
                
                // Add small delay to respect API rate limits (10 requests/second)
                Thread.sleep(100);
            } catch (Exception e) {
                logger.error("Error geocoding shipment {}: {}", shipment.getTrackingNumber(), e.getMessage(), e);
            }
        }
        
        logger.info("Background geocoding job completed. Geocoded {} shipments.", geocodedCount);
    }

    /**
     * Manually trigger geocoding for a specific shipment
     */
    @Transactional
    public void geocodeShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + shipmentId));
        
        geocodeShipmentAddresses(shipment);
        shipmentRepository.save(shipment);
    }

    /**
     * Geocode addresses for a shipment
     */
    private void geocodeShipmentAddresses(Shipment shipment) {
        // Geocode pickup address if coordinates are missing
        if (shipment.getPickupLatitude() == null || shipment.getPickupLongitude() == null) {
            String fullPickupAddress = buildFullAddress(
                shipment.getPickupAddress(),
                shipment.getPickupCity(),
                shipment.getPickupState(),
                shipment.getPickupZipCode(),
                shipment.getPickupCountry()
            );
            
            AddressCoordinates pickupCoords = geocodingService.validateAndNormalizeAddress(fullPickupAddress);
            if (pickupCoords != null) {
                shipment.setPickupLatitude(pickupCoords.getLatitude());
                shipment.setPickupLongitude(pickupCoords.getLongitude());
            }
        }
        
        // Geocode delivery address if coordinates are missing
        if (shipment.getDeliveryLatitude() == null || shipment.getDeliveryLongitude() == null) {
            String fullDeliveryAddress = buildFullAddress(
                shipment.getDeliveryAddress(),
                shipment.getDeliveryCity(),
                shipment.getDeliveryState(),
                shipment.getDeliveryZipCode(),
                shipment.getDeliveryCountry()
            );
            
            AddressCoordinates deliveryCoords = geocodingService.validateAndNormalizeAddress(fullDeliveryAddress);
            if (deliveryCoords != null) {
                shipment.setDeliveryLatitude(deliveryCoords.getLatitude());
                shipment.setDeliveryLongitude(deliveryCoords.getLongitude());
            }
        }
    }

    /**
     * Build full address string from components
     */
    private String buildFullAddress(String address, String city, String state, String zipCode, String country) {
        StringBuilder fullAddress = new StringBuilder();
        if (address != null && !address.isEmpty()) {
            fullAddress.append(address);
        }
        if (city != null && !city.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(state);
        }
        if (zipCode != null && !zipCode.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(zipCode);
        }
        if (country != null && !country.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country);
        }
        return fullAddress.toString();
    }
}


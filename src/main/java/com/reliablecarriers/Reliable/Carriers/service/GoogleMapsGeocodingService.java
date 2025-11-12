package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.AddressCoordinates;
import com.reliablecarriers.Reliable.Carriers.dto.GeocodingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleMapsGeocodingService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleMapsGeocodingService.class);
    
    @Value("${google.maps.api.key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    
    /**
     * Geocode an address to get coordinates
     * Results are cached to reduce API calls
     */
    @Cacheable(value = "geocoding", key = "#address")
    public GeocodingResult geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }
        
        logger.debug("Geocoding address (not cached): {}", address);
        
        try {
            Map<String, String> params = new HashMap<>();
            params.put("address", address);
            params.put("key", apiKey);
            params.put("region", "za"); // Restrict to South Africa
            
            String url = buildUrl(GEOCODING_URL, params);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "OK".equals(response.get("status"))) {
                return parseGeocodingResponse(response);
            }
            
            logger.warn("Geocoding failed for address: {}", address);
            return null;
        } catch (RestClientException e) {
            logger.error("Geocoding API call failed for address {}: {}", address, e.getMessage());
            return null;
        }
    }
    
    /**
     * Reverse geocode coordinates to get address
     * Results are cached to reduce API calls
     */
    @Cacheable(value = "geocoding", key = "#latitude + ',' + #longitude")
    public GeocodingResult reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        
        logger.debug("Reverse geocoding coordinates (not cached): {}, {}", latitude, longitude);
        
        try {
            String latlng = latitude + "," + longitude;
            Map<String, String> params = new HashMap<>();
            params.put("latlng", latlng);
            params.put("key", apiKey);
            params.put("region", "za");
            
            String url = buildUrl(GEOCODING_URL, params);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "OK".equals(response.get("status"))) {
                return parseGeocodingResponse(response);
            }
            
            logger.warn("Reverse geocoding failed for coordinates: {}, {}", latitude, longitude);
            return null;
        } catch (RestClientException e) {
            logger.error("Reverse geocoding API call failed: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate and normalize an address
     * Results are cached
     */
    @Cacheable(value = "geocoding", key = "'validate:' + #address")
    public AddressCoordinates validateAndNormalizeAddress(String address) {
        GeocodingResult result = geocodeAddress(address);
        
        if (result != null && result.isValid()) {
            return new AddressCoordinates(
                result.getFormattedAddress(),
                result.getLatitude(),
                result.getLongitude(),
                result.getPlaceId()
            );
        }
        
        return null;
    }
    
    /**
     * Get coordinates for pickup and delivery addresses
     */
    public Map<String, AddressCoordinates> getCoordinatesForBooking(String pickupAddress, String deliveryAddress) {
        Map<String, AddressCoordinates> coordinates = new HashMap<>();
        
        // Geocode pickup address
        AddressCoordinates pickupCoords = validateAndNormalizeAddress(pickupAddress);
        if (pickupCoords != null) {
            coordinates.put("pickup", pickupCoords);
        }
        
        // Geocode delivery address
        AddressCoordinates deliveryCoords = validateAndNormalizeAddress(deliveryAddress);
        if (deliveryCoords != null) {
            coordinates.put("delivery", deliveryCoords);
        }
        
        return coordinates;
    }
    
    private String buildUrl(String baseUrl, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return url.toString();
    }
    
    private GeocodingResult parseGeocodingResponse(Map<String, Object> response) {
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) response.get("results");
            
            if (results != null && !results.isEmpty()) {
                Map<String, Object> result = results.get(0);
                
                String formattedAddress = (String) result.get("formatted_address");
                String placeId = (String) result.get("place_id");
                
                @SuppressWarnings("unchecked")
                Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
                @SuppressWarnings("unchecked")
                Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                
                BigDecimal lat = new BigDecimal(location.get("lat").toString());
                BigDecimal lng = new BigDecimal(location.get("lng").toString());
                
                return new GeocodingResult(formattedAddress, lat, lng, placeId, true);
            }
        } catch (Exception e) {
            System.err.println("Error parsing geocoding response: " + e.getMessage());
        }
        
        return null;
    }
}


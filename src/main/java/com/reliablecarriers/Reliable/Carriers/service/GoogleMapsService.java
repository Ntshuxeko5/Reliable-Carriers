package com.reliablecarriers.Reliable.Carriers.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class GoogleMapsService {
    
    @Value("${google.maps.api.key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String DISTANCE_MATRIX_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final String DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json";
    
    public DistanceResult calculateDistance(String origin, String destination) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your-google-maps-api-key")) {
            System.out.println("Google Maps API key not configured, skipping API call");
            return null;
        }
        
        System.out.println("Google Maps API key configured, making API call...");
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(DISTANCE_MATRIX_URL)
                    .queryParam("origins", origin)
                    .queryParam("destinations", destination)
                    .queryParam("units", "metric")
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, 
                (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && "OK".equals(responseBody.get("status"))) {
                System.out.println("Google Maps API call successful");
                return parseDistanceMatrixResponse(responseBody);
            } else {
                System.out.println("Google Maps API call failed with status: " + (responseBody != null ? responseBody.get("status") : "null"));
            }
            
        } catch (Exception e) {
            System.err.println("Error calling Google Maps Distance Matrix API: " + e.getMessage());
        }
        
        return null;
    }
    
    public RouteResult getRoute(String origin, String destination) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your-google-maps-api-key")) {
            System.out.println("Google Maps API key not configured, skipping API call");
            return null;
        }
        
        try {
            String url = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_URL)
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("units", "metric")
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, 
                (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && "OK".equals(responseBody.get("status"))) {
                return parseDirectionsResponse(responseBody);
            }
            
        } catch (Exception e) {
            System.err.println("Error calling Google Maps Directions API: " + e.getMessage());
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private DistanceResult parseDistanceMatrixResponse(Map<String, Object> response) {
        try {
            Map<String, Object> rows = (Map<String, Object>) ((java.util.List<?>) response.get("rows")).get(0);
            Map<String, Object> elements = (Map<String, Object>) ((java.util.List<?>) rows.get("elements")).get(0);
            
            if ("OK".equals(elements.get("status"))) {
                Map<String, Object> distance = (Map<String, Object>) elements.get("distance");
                Map<String, Object> duration = (Map<String, Object>) elements.get("duration");
                
                return new DistanceResult(
                    (Integer) distance.get("value"), // Distance in meters
                    (Integer) duration.get("value")   // Duration in seconds
                );
            }
        } catch (Exception e) {
            System.err.println("Error parsing distance matrix response: " + e.getMessage());
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private RouteResult parseDirectionsResponse(Map<String, Object> response) {
        try {
            Map<String, Object> route = (Map<String, Object>) ((java.util.List<?>) response.get("routes")).get(0);
            Map<String, Object> leg = (Map<String, Object>) ((java.util.List<?>) route.get("legs")).get(0);
            
            Map<String, Object> distance = (Map<String, Object>) leg.get("distance");
            Map<String, Object> duration = (Map<String, Object>) leg.get("duration");
            
            return new RouteResult(
                (Integer) distance.get("value"), // Distance in meters
                (Integer) duration.get("value")  // Duration in seconds
            );
        } catch (Exception e) {
            System.err.println("Error parsing directions response: " + e.getMessage());
        }
        
        return null;
    }
    
    public static class DistanceResult {
        private final int distanceMeters;
        private final int durationSeconds;
        
        public DistanceResult(int distanceMeters, int durationSeconds) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
        }
        
        public double getDistanceKm() {
            return distanceMeters / 1000.0;
        }
        
        public int getDistanceMeters() {
            return distanceMeters;
        }
        
        public int getDurationSeconds() {
            return durationSeconds;
        }
        
        public String getFormattedDuration() {
            int hours = durationSeconds / 3600;
            int minutes = (durationSeconds % 3600) / 60;
            
            if (hours > 0) {
                return hours + "h " + minutes + "m";
            } else {
                return minutes + "m";
            }
        }
    }
    
    public static class RouteResult {
        private final int distanceMeters;
        private final int durationSeconds;
        
        public RouteResult(int distanceMeters, int durationSeconds) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
        }
        
        public double getDistanceKm() {
            return distanceMeters / 1000.0;
        }
        
        public int getDistanceMeters() {
            return distanceMeters;
        }
        
        public int getDurationSeconds() {
            return durationSeconds;
        }
        
        public String getFormattedDuration() {
            int hours = durationSeconds / 3600;
            int minutes = (durationSeconds % 3600) / 60;
            
            if (hours > 0) {
                return hours + "h " + minutes + "m";
            } else {
                return minutes + "m";
            }
        }
    }
}

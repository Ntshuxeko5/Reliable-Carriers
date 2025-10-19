package com.reliablecarriers.Reliable.Carriers.service.impl;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SimpleGeocodingService {

    // Simple mapping of common South African cities to approximate coordinates
    private static final Map<String, double[]> CITY_COORDINATES = new HashMap<>();
    
    static {
        // Major South African cities with precise coordinates
        CITY_COORDINATES.put("johannesburg", new double[]{-26.2041, 28.0473});
        CITY_COORDINATES.put("jhb", new double[]{-26.2041, 28.0473});
        CITY_COORDINATES.put("pretoria", new double[]{-25.7461, 28.1881});
        CITY_COORDINATES.put("cape town", new double[]{-33.9249, 18.4241});
        CITY_COORDINATES.put("durban", new double[]{-29.8587, 31.0218});
        CITY_COORDINATES.put("sandton", new double[]{-26.1076, 28.0567});
        CITY_COORDINATES.put("midrand", new double[]{-25.9964, 28.1378});
        CITY_COORDINATES.put("soweto", new double[]{-26.2678, 27.8585});
        CITY_COORDINATES.put("centurion", new double[]{-25.8603, 28.1892});
        CITY_COORDINATES.put("randburg", new double[]{-26.0936, 28.0014});
        CITY_COORDINATES.put("roodepoort", new double[]{-26.1625, 27.8725});
        CITY_COORDINATES.put("kempton park", new double[]{-26.1011, 28.2331});
        CITY_COORDINATES.put("boksburg", new double[]{-26.2118, 28.2596});
        CITY_COORDINATES.put("benoni", new double[]{-26.1885, 28.3206});
        CITY_COORDINATES.put("germiston", new double[]{-26.2219, 28.1692});
        CITY_COORDINATES.put("alberton", new double[]{-26.2678, 28.1225});
        CITY_COORDINATES.put("krugersdorp", new double[]{-26.1004, 27.7756});
        CITY_COORDINATES.put("carletonville", new double[]{-26.3589, 27.3975});
        CITY_COORDINATES.put("westonaria", new double[]{-26.3206, 27.6497});
        CITY_COORDINATES.put("mogale city", new double[]{-26.1004, 27.7756});
        
        // Additional major cities
        CITY_COORDINATES.put("port elizabeth", new double[]{-33.9608, 25.6022});
        CITY_COORDINATES.put("bloemfontein", new double[]{-29.1167, 26.2167});
        CITY_COORDINATES.put("nelspruit", new double[]{-25.4744, 30.9703});
        CITY_COORDINATES.put("polokwane", new double[]{-23.9045, 29.4689});
        CITY_COORDINATES.put("kimberley", new double[]{-28.7383, 24.7639});
        CITY_COORDINATES.put("richards bay", new double[]{-28.7831, 32.0377});
        CITY_COORDINATES.put("east london", new double[]{-33.0292, 27.8546});
        CITY_COORDINATES.put("george", new double[]{-33.9633, 22.4617});
        CITY_COORDINATES.put("mbombela", new double[]{-25.4744, 30.9703});
        
        // Gauteng suburbs
        CITY_COORDINATES.put("fourways", new double[]{-25.9858, 28.0136});
        CITY_COORDINATES.put("rosebank", new double[]{-26.1464, 28.0436});
        CITY_COORDINATES.put("melville", new double[]{-26.1706, 28.0042});
        CITY_COORDINATES.put("parktown", new double[]{-26.1856, 28.0381});
        CITY_COORDINATES.put("houghton", new double[]{-26.1714, 28.0492});
        CITY_COORDINATES.put("saxonwold", new double[]{-26.1636, 28.0497});
        CITY_COORDINATES.put("hillbrow", new double[]{-26.1908, 28.0458});
        CITY_COORDINATES.put("yeoville", new double[]{-26.1875, 28.0611});
        CITY_COORDINATES.put("bedfordview", new double[]{-26.2333, 28.1500});
        CITY_COORDINATES.put("edenvale", new double[]{-26.2167, 28.1667});
        CITY_COORDINATES.put("kempton park", new double[]{-26.1011, 28.2331});
        CITY_COORDINATES.put("boksburg", new double[]{-26.2118, 28.2596});
        CITY_COORDINATES.put("brakpan", new double[]{-26.2364, 28.3694});
        CITY_COORDINATES.put("springs", new double[]{-26.2500, 28.4167});
        CITY_COORDINATES.put("alberton", new double[]{-26.2678, 28.1225});
        CITY_COORDINATES.put("germiston", new double[]{-26.2219, 28.1692});
        CITY_COORDINATES.put("katlehong", new double[]{-26.3333, 28.1500});
        CITY_COORDINATES.put("thokoza", new double[]{-26.3333, 28.1667});
        CITY_COORDINATES.put("vanderbijlpark", new double[]{-26.7000, 27.8167});
        CITY_COORDINATES.put("vereeniging", new double[]{-26.6833, 27.9333});
        CITY_COORDINATES.put("hebron", new double[]{-26.6833, 27.9500});
    }

    /**
     * Extract approximate coordinates from an address string
     * @param address The address to geocode
     * @return Array with [latitude, longitude] or null if not found
     */
    public double[] getCoordinatesFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }
        
        String lowerAddress = address.toLowerCase().trim();
        
        // Try to find city in the address
        for (Map.Entry<String, double[]> entry : CITY_COORDINATES.entrySet()) {
            if (lowerAddress.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Default to Johannesburg if no city found
        return CITY_COORDINATES.get("johannesburg");
    }

    /**
     * Calculate distance between two addresses using simple geocoding
     * @param address1 First address
     * @param address2 Second address
     * @return Distance in kilometers
     */
    public double calculateDistance(String address1, String address2) {
        double[] coords1 = getCoordinatesFromAddress(address1);
        double[] coords2 = getCoordinatesFromAddress(address2);
        
        if (coords1 == null || coords2 == null) {
            // Fallback distance calculation
            return estimateDistanceFromAddresses(address1, address2);
        }
        
        return haversineDistance(coords1[0], coords1[1], coords2[0], coords2[1]);
    }

    /**
     * Haversine formula for calculating distance between two coordinates
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * Estimate distance based on address content (fallback method)
     */
    private double estimateDistanceFromAddresses(String address1, String address2) {
        String lower1 = address1.toLowerCase();
        String lower2 = address2.toLowerCase();
        
        // Same city
        for (String city : CITY_COORDINATES.keySet()) {
            if (lower1.contains(city) && lower2.contains(city)) {
                return 10.0; // Average intra-city distance
            }
        }
        
        // Common inter-city distances
        if ((lower1.contains("johannesburg") && lower2.contains("pretoria")) ||
            (lower1.contains("pretoria") && lower2.contains("johannesburg"))) {
            return 55.0;
        }
        
        if ((lower1.contains("sandton") && lower2.contains("midrand")) ||
            (lower1.contains("midrand") && lower2.contains("sandton"))) {
            return 20.0;
        }
        
        if ((lower1.contains("johannesburg") && lower2.contains("soweto")) ||
            (lower1.contains("soweto") && lower2.contains("johannesburg"))) {
            return 25.0;
        }
        
        // Default fallback
        return 30.0;
    }
}

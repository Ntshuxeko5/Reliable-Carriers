package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.QuoteRequest;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * UNIFIED Quote Service - Single source of truth for all quote calculations
 * Replaces multiple inconsistent quote calculation implementations
 */
@Service
public class UnifiedQuoteService {

    @Autowired(required = false)
    private GoogleMapsService googleMapsService;

    // UNIFIED SERVICE CONFIGURATIONS - Realistic South African Courier Rates (2025)
    // Based on market research: PostNet, Fastway, CourierGuy, etc.
    private static final Map<String, ServiceConfig> SERVICE_CONFIGS = Map.of(
        "economy", new ServiceConfig(
            new BigDecimal("120.00"), // Base price for economy (R120 for first 15km, up to 5kg)
            new BigDecimal("0.85"),    // Service multiplier (15% discount for economy)
            "4-7 Business Days",      // Description
            5,                        // Delivery days
            new BigDecimal("8.50")    // Rate per km after base distance (R8.50/km)
        ),
        "standard", new ServiceConfig(
            new BigDecimal("180.00"), // Base price for standard (R180 for first 15km, up to 5kg)
            new BigDecimal("1.0"),    // Service multiplier (standard rate)
            "2-3 Business Days",      // Description
            3,                        // Delivery days
            new BigDecimal("12.00")   // Rate per km after base distance (R12/km)
        ),
        "express", new ServiceConfig(
            new BigDecimal("350.00"), // Base price for express (R350 for first 15km, up to 5kg)
            new BigDecimal("1.4"),    // Service multiplier (40% premium for express)
            "Same-day & Next-day",    // Description
            1,                        // Delivery days
            new BigDecimal("20.00")   // Rate per km after base distance (R20/km)
        ),
        "same_day", new ServiceConfig(
            new BigDecimal("550.00"), // Base price for same day (R550 for first 15km, up to 5kg)
            new BigDecimal("1.8"),    // Service multiplier (80% premium for same-day)
            "Same Day Delivery",      // Description
            0,                        // Same day
            new BigDecimal("30.00")   // Rate per km after base distance (R30/km)
        )
    );

    // UNIFIED PACKAGE TYPE MULTIPLIERS
    private static final Map<String, BigDecimal> PACKAGE_TYPE_MULTIPLIERS = Map.of(
        "document", new BigDecimal("0.8"),
        "parcel", new BigDecimal("1.0"),
        "fragile", new BigDecimal("1.3"),
        "valuable", new BigDecimal("1.5")
    );

    // UNIFIED CONSTANTS - Realistic South African Rates
    private static final BigDecimal BASE_DISTANCE_KM = new BigDecimal("15.00"); // Free distance included in base price (15km)
    private static final BigDecimal BASE_WEIGHT_KG = new BigDecimal("5.00"); // Base weight included (5kg)
    private static final BigDecimal WEIGHT_RATE_PER_KG = new BigDecimal("8.00"); // Additional charge per kg after base weight (R8/kg)
    private static final BigDecimal FUEL_SURCHARGE_RATE = new BigDecimal("0.08"); // 8% fuel surcharge (realistic for SA)
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05"); // 5% service fee (platform/processing fee)
    private static final BigDecimal INSURANCE_RATE = new BigDecimal("0.025"); // 2.5% insurance fee (optional)

    /**
     * UNIFIED quote calculation method - replaces all other implementations
     */
    public QuoteResponse calculateUnifiedQuote(QuoteRequest request) {
        try {
            // Validate input
            if (!isValidRequest(request)) {
                return new QuoteResponse(false, "Invalid request parameters");
            }

            // Get service configuration
            ServiceConfig serviceConfig = SERVICE_CONFIGS.get(request.getServiceType().toLowerCase());
            if (serviceConfig == null) {
                return new QuoteResponse(false, "Invalid service type: " + request.getServiceType());
            }

            // Calculate chargeable weight (higher of actual or volumetric)
            BigDecimal volumeWeight = calculateVolumeWeight(request.getLength(), request.getWidth(), request.getHeight());
            BigDecimal chargeableWeight = request.getWeight().max(volumeWeight);

            // Calculate distance (use Google Maps API in production)
            BigDecimal distance = calculateDistance(request.getPickupAddress(), request.getDeliveryAddress());

            // UNIFIED PRICING CALCULATION
            BigDecimal basePrice = serviceConfig.getBasePrice();
            
            // Distance charges (only for distance beyond base distance)
            BigDecimal distanceCharge = BigDecimal.ZERO;
            if (distance.compareTo(BASE_DISTANCE_KM) > 0) {
                BigDecimal extraDistance = distance.subtract(BASE_DISTANCE_KM);
                distanceCharge = extraDistance.multiply(serviceConfig.getRatePerKm())
                    .setScale(2, RoundingMode.HALF_UP);
            }

            // Weight charges (for weight above base weight of 5kg)
            BigDecimal weightCharge = BigDecimal.ZERO;
            if (chargeableWeight.compareTo(BASE_WEIGHT_KG) > 0) {
                BigDecimal extraWeight = chargeableWeight.subtract(BASE_WEIGHT_KG);
                weightCharge = extraWeight.multiply(WEIGHT_RATE_PER_KG)
                    .setScale(2, RoundingMode.HALF_UP);
            }

            // Package type multiplier
            BigDecimal packageTypeMultiplier = PACKAGE_TYPE_MULTIPLIERS.getOrDefault(
                request.getPackageType(), BigDecimal.ONE);

            // Calculate subtotal
            BigDecimal subtotal = basePrice
                .add(distanceCharge)
                .add(weightCharge)
                .multiply(packageTypeMultiplier)
                .multiply(serviceConfig.getServiceMultiplier());

            // Add fees
            BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE);
            BigDecimal fuelSurcharge = subtotal.multiply(FUEL_SURCHARGE_RATE);
            BigDecimal insuranceFee = request.isInsurance() ? 
                subtotal.multiply(INSURANCE_RATE) : BigDecimal.ZERO;

            // Calculate total
            BigDecimal totalCost = subtotal
                .add(serviceFee)
                .add(fuelSurcharge)
                .add(insuranceFee)
                .setScale(2, RoundingMode.HALF_UP);

            // Calculate estimated delivery
            String estimatedDelivery = calculateEstimatedDelivery(serviceConfig.getDeliveryDays());

            // Create unified response
            QuoteResponse response = new QuoteResponse(true, totalCost, 
                request.getServiceType(), estimatedDelivery);
            
            response.setCurrency("ZAR");
            response.setServiceDescription(serviceConfig.getDescription());
            response.setBasePrice(basePrice);
            response.setDistanceCharge(distanceCharge);
            response.setWeightCharge(weightCharge);
            response.setPackageTypeMultiplier(packageTypeMultiplier);
            response.setVolumeWeight(volumeWeight);
            response.setActualWeight(request.getWeight());
            response.setDistance(distance);

            // Create detailed breakdown
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("basePrice", basePrice);
            breakdown.put("distanceCharge", distanceCharge);
            breakdown.put("weightCharge", weightCharge);
            breakdown.put("packageTypeMultiplier", packageTypeMultiplier);
            breakdown.put("serviceMultiplier", serviceConfig.getServiceMultiplier());
            breakdown.put("subtotal", subtotal);
            breakdown.put("serviceFee", serviceFee);
            breakdown.put("fuelSurcharge", fuelSurcharge);
            breakdown.put("insuranceFee", insuranceFee);
            breakdown.put("totalCost", totalCost);
            response.setBreakdown(breakdown);

            return response;

        } catch (Exception e) {
            return new QuoteResponse(false, "Error calculating quote: " + e.getMessage());
        }
    }

    /**
     * Get available service options with unified pricing
     */
    public Map<String, Object> getUnifiedServiceOptions() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> services = new ArrayList<>();
        for (Map.Entry<String, ServiceConfig> entry : SERVICE_CONFIGS.entrySet()) {
            Map<String, Object> service = new HashMap<>();
            service.put("id", entry.getKey());
            service.put("name", capitalizeFirst(entry.getKey()) + " Delivery");
            service.put("description", entry.getValue().getDescription());
            service.put("basePrice", entry.getValue().getBasePrice());
            service.put("deliveryDays", entry.getValue().getDeliveryDays());
            services.add(service);
        }
        
        result.put("services", services);
        result.put("packageTypes", PACKAGE_TYPE_MULTIPLIERS);
        
        return result;
    }

    // UNIFIED HELPER METHODS
    private boolean isValidRequest(QuoteRequest request) {
        return request != null &&
               request.getServiceType() != null &&
               request.getWeight() != null && request.getWeight().compareTo(BigDecimal.ZERO) > 0 &&
               request.getLength() != null && request.getLength().compareTo(BigDecimal.ZERO) > 0 &&
               request.getWidth() != null && request.getWidth().compareTo(BigDecimal.ZERO) > 0 &&
               request.getHeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0 &&
               request.getPickupAddress() != null && !request.getPickupAddress().trim().isEmpty() &&
               request.getDeliveryAddress() != null && !request.getDeliveryAddress().trim().isEmpty();
    }

    private BigDecimal calculateVolumeWeight(BigDecimal length, BigDecimal width, BigDecimal height) {
        // Standard volumetric weight calculation: (L x W x H) / 5000
        return length.multiply(width).multiply(height)
                .divide(new BigDecimal("5000"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDistance(String pickupAddress, String deliveryAddress) {
        // Try Google Maps Distance Matrix API first if available
        if (googleMapsService != null && pickupAddress != null && deliveryAddress != null) {
            try {
                GoogleMapsService.DistanceResult result = googleMapsService.calculateDistance(
                    pickupAddress, 
                    deliveryAddress
                );
                if (result != null) {
                    // Convert meters to kilometers and return as BigDecimal
                    return BigDecimal.valueOf(result.getDistanceKm()).setScale(2, RoundingMode.HALF_UP);
                }
            } catch (Exception e) {
                // Log error but fall back to heuristic calculation
                System.err.println("Google Maps API call failed, using fallback calculation: " + e.getMessage());
            }
        }
        
        // Fallback: Simplified calculation based on address similarity
        // This is used when Google Maps API is not configured or unavailable
        if (pickupAddress == null || deliveryAddress == null) {
            return new BigDecimal("100.0"); // Default distance if addresses are null
        }
        
        String pickupLower = pickupAddress.toLowerCase();
        String deliveryLower = deliveryAddress.toLowerCase();
        
        // Extract city names (first part before comma)
        String pickupCity = pickupLower.split(",")[0].trim();
        String deliveryCity = deliveryLower.split(",")[0].trim();
        
        // Same city or same street
        if (pickupCity.equals(deliveryCity) || 
            pickupLower.contains(deliveryCity) || 
            deliveryLower.contains(pickupCity)) {
            return new BigDecimal("15.0"); // Same city - approximately 15km
        } 
        // Same province (Gauteng)
        else if ((pickupLower.contains("gauteng") || pickupLower.contains("johannesburg") || pickupLower.contains("pretoria")) &&
                 (deliveryLower.contains("gauteng") || deliveryLower.contains("johannesburg") || deliveryLower.contains("pretoria"))) {
            return new BigDecimal("45.0"); // Same province - approximately 45km
        } 
        // Different provinces
        else {
            return new BigDecimal("350.0"); // Different provinces - approximately 350km
        }
    }

    private String calculateEstimatedDelivery(int deliveryDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryDate = now.plusDays(deliveryDays);
        
        // Skip weekends for business days calculation
        while (deliveryDate.getDayOfWeek().getValue() > 5) { // Saturday = 6, Sunday = 7
            deliveryDate = deliveryDate.plusDays(1);
        }
        
        if (deliveryDays == 0) {
            return "Today by 6:00 PM";
        } else if (deliveryDays == 1) {
            return "Tomorrow by 5:00 PM";
        } else {
            return deliveryDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + " by 5:00 PM";
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // UNIFIED SERVICE CONFIGURATION CLASS
    private static class ServiceConfig {
        private final BigDecimal basePrice;
        private final BigDecimal serviceMultiplier;
        private final String description;
        private final int deliveryDays;
        private final BigDecimal ratePerKm;

        public ServiceConfig(BigDecimal basePrice, BigDecimal serviceMultiplier, 
                           String description, int deliveryDays, BigDecimal ratePerKm) {
            this.basePrice = basePrice;
            this.serviceMultiplier = serviceMultiplier;
            this.description = description;
            this.deliveryDays = deliveryDays;
            this.ratePerKm = ratePerKm;
        }

        // Getters
        public BigDecimal getBasePrice() { return basePrice; }
        public BigDecimal getServiceMultiplier() { return serviceMultiplier; }
        public String getDescription() { return description; }
        public int getDeliveryDays() { return deliveryDays; }
        public BigDecimal getRatePerKm() { return ratePerKm; }
    }
}

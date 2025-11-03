package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.QuoteRequest;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
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

    // UNIFIED SERVICE CONFIGURATIONS - Single source of truth
    private static final Map<String, ServiceConfig> SERVICE_CONFIGS = Map.of(
        "economy", new ServiceConfig(
            new BigDecimal("450.00"), // Base price for economy
            new BigDecimal("0.8"),    // Service multiplier
            "4-7 Business Days",      // Description
            5,                        // Delivery days
            new BigDecimal("15.00")   // Rate per km after base distance
        ),
        "standard", new ServiceConfig(
            new BigDecimal("550.00"), // Base price for standard
            new BigDecimal("1.0"),    // Service multiplier
            "2-3 Business Days",      // Description
            3,                        // Delivery days
            new BigDecimal("25.00")   // Rate per km after base distance
        ),
        "express", new ServiceConfig(
            new BigDecimal("750.00"), // Base price for express
            new BigDecimal("1.5"),    // Service multiplier
            "Same-day & Next-day",    // Description
            1,                        // Delivery days
            new BigDecimal("35.00")   // Rate per km after base distance
        ),
        "same_day", new ServiceConfig(
            new BigDecimal("950.00"), // Base price for same day
            new BigDecimal("2.0"),    // Service multiplier
            "Same Day Delivery",      // Description
            0,                        // Same day
            new BigDecimal("50.00")   // Rate per km after base distance
        )
    );

    // UNIFIED PACKAGE TYPE MULTIPLIERS
    private static final Map<String, BigDecimal> PACKAGE_TYPE_MULTIPLIERS = Map.of(
        "document", new BigDecimal("0.8"),
        "parcel", new BigDecimal("1.0"),
        "fragile", new BigDecimal("1.3"),
        "valuable", new BigDecimal("1.5")
    );

    // UNIFIED CONSTANTS
    private static final BigDecimal BASE_DISTANCE_KM = new BigDecimal("20.00"); // Free distance included in base price
    private static final BigDecimal WEIGHT_RATE_PER_KG = new BigDecimal("5.00"); // Additional charge per kg
    private static final BigDecimal FUEL_SURCHARGE_RATE = new BigDecimal("0.05"); // 5% fuel surcharge
    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.03"); // 3% service fee
    private static final BigDecimal INSURANCE_RATE = new BigDecimal("0.02"); // 2% insurance fee

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
                distanceCharge = extraDistance.multiply(serviceConfig.getRatePerKm());
            }

            // Weight charges (for weight above 1kg)
            BigDecimal weightCharge = BigDecimal.ZERO;
            if (chargeableWeight.compareTo(BigDecimal.ONE) > 0) {
                BigDecimal extraWeight = chargeableWeight.subtract(BigDecimal.ONE);
                weightCharge = extraWeight.multiply(WEIGHT_RATE_PER_KG);
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
        // TODO: Integrate with Google Maps Distance Matrix API for production
        // For demo purposes, use simplified calculation based on address similarity
        if (pickupAddress.toLowerCase().contains(deliveryAddress.toLowerCase().split(",")[0]) ||
            deliveryAddress.toLowerCase().contains(pickupAddress.toLowerCase().split(",")[0])) {
            return new BigDecimal("15.0"); // Same city
        } else if (pickupAddress.toLowerCase().contains("gauteng") && deliveryAddress.toLowerCase().contains("gauteng")) {
            return new BigDecimal("45.0"); // Same province
        } else {
            return new BigDecimal("350.0"); // Different provinces
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

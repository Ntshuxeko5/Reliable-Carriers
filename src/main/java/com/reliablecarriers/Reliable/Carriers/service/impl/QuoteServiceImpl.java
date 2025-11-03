package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.dto.QuoteRequest;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;
import com.reliablecarriers.Reliable.Carriers.service.QuoteService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class QuoteServiceImpl implements QuoteService {

    // Service configurations
    private static final Map<String, Map<String, Object>> SERVICE_CONFIGS = Map.of(
        "express", Map.of(
            "basePrice", new BigDecimal("89"),
            "multiplier", new BigDecimal("1.2"),
            "description", "Same-day & Next-day",
            "deliveryDays", 1
        ),
        "standard", Map.of(
            "basePrice", new BigDecimal("45"),
            "multiplier", new BigDecimal("1.0"),
            "description", "2-3 Business Days",
            "deliveryDays", 3
        ),
        "economy", Map.of(
            "basePrice", new BigDecimal("25"),
            "multiplier", new BigDecimal("0.8"),
            "description", "4-7 Business Days",
            "deliveryDays", 5
        )
    );

    // Package type multipliers
    private static final Map<String, BigDecimal> PACKAGE_TYPE_MULTIPLIERS = Map.of(
        "document", new BigDecimal("0.8"),
        "parcel", new BigDecimal("1.0"),
        "fragile", new BigDecimal("1.3"),
        "valuable", new BigDecimal("1.5")
    );

    @Override
    public QuoteResponse calculateQuote(QuoteRequest request) {
        try {
            // Validate input
            if (!isValidRequest(request)) {
                return new QuoteResponse(false, "Invalid request parameters");
            }

            // Calculate volume weight
            BigDecimal volumeWeight = calculateVolumeWeight(request.getLength(), 
                                                          request.getWidth(), 
                                                          request.getHeight());
            
            // Use the higher of actual weight or volume weight
            BigDecimal actualWeight = request.getWeight().max(volumeWeight);

            // Get service configuration
            Map<String, Object> serviceConfig = SERVICE_CONFIGS.get(request.getServiceType());
            if (serviceConfig == null) {
                return new QuoteResponse(false, "Invalid service type");
            }

            BigDecimal basePrice = (BigDecimal) serviceConfig.get("basePrice");
            BigDecimal serviceMultiplier = (BigDecimal) serviceConfig.get("multiplier");
            String description = (String) serviceConfig.get("description");
            Integer deliveryDays = (Integer) serviceConfig.get("deliveryDays");

            // Calculate distance (simplified - in real app, use Google Maps API)
            BigDecimal distance = calculateDistance(request.getPickupAddress(), 
                                                  request.getDeliveryAddress());

            // Calculate weight charges
            BigDecimal weightCharge = calculateWeightCharge(actualWeight);

            // Calculate distance charges
            BigDecimal distanceCharge = calculateDistanceCharge(distance);

            // Get package type multiplier
            BigDecimal packageTypeMultiplier = PACKAGE_TYPE_MULTIPLIERS.getOrDefault(
                request.getPackageType(), BigDecimal.ONE);

            // Calculate total cost
            BigDecimal totalCost = basePrice
                .add(weightCharge)
                .add(distanceCharge)
                .multiply(packageTypeMultiplier)
                .multiply(serviceMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

            // Calculate estimated delivery
            String estimatedDelivery = calculateEstimatedDelivery(deliveryDays);

            // Create response
            QuoteResponse response = new QuoteResponse(true, totalCost, 
                                                     request.getServiceType(), estimatedDelivery);
            response.setCurrency("ZAR");
            response.setServiceDescription(description);
            response.setBasePrice(basePrice);
            response.setWeightCharge(weightCharge);
            response.setDistanceCharge(distanceCharge);
            response.setPackageTypeMultiplier(packageTypeMultiplier);
            response.setVolumeWeight(volumeWeight);
            response.setActualWeight(actualWeight);
            response.setDistance(distance);

            // Create breakdown
            Map<String, Object> breakdown = new HashMap<>();
            breakdown.put("basePrice", basePrice);
            breakdown.put("weightCharge", weightCharge);
            breakdown.put("distanceCharge", distanceCharge);
            breakdown.put("packageTypeMultiplier", packageTypeMultiplier);
            breakdown.put("serviceMultiplier", serviceMultiplier);
            breakdown.put("totalCost", totalCost);
            response.setBreakdown(breakdown);

            return response;

        } catch (Exception e) {
            return new QuoteResponse(false, "Error calculating quote: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getServiceOptions() {
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> services = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> entry : SERVICE_CONFIGS.entrySet()) {
            Map<String, Object> service = new HashMap<>();
            service.put("id", entry.getKey());
            service.put("name", capitalizeFirst(entry.getKey()) + " Delivery");
            service.put("description", entry.getValue().get("description"));
            service.put("basePrice", entry.getValue().get("basePrice"));
            service.put("multiplier", entry.getValue().get("multiplier"));
            service.put("deliveryDays", entry.getValue().get("deliveryDays"));
            services.add(service);
        }
        
        result.put("services", services);
        result.put("packageTypes", PACKAGE_TYPE_MULTIPLIERS);
        
        return result;
    }

    @Override
    public Map<String, Object> validateDimensions(Map<String, Object> dimensions) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            BigDecimal length = new BigDecimal(dimensions.get("length").toString());
            BigDecimal width = new BigDecimal(dimensions.get("width").toString());
            BigDecimal height = new BigDecimal(dimensions.get("height").toString());
            BigDecimal weight = new BigDecimal(dimensions.get("weight").toString());
            
            // Validate dimensions
            boolean validLength = length.compareTo(BigDecimal.ZERO) > 0 && length.compareTo(new BigDecimal("200")) <= 0;
            boolean validWidth = width.compareTo(BigDecimal.ZERO) > 0 && width.compareTo(new BigDecimal("200")) <= 0;
            boolean validHeight = height.compareTo(BigDecimal.ZERO) > 0 && height.compareTo(new BigDecimal("200")) <= 0;
            boolean validWeight = weight.compareTo(BigDecimal.ZERO) > 0 && weight.compareTo(new BigDecimal("50")) <= 0;
            
            result.put("valid", validLength && validWidth && validHeight && validWeight);
            result.put("volumeWeight", calculateVolumeWeight(length, width, height));
            result.put("actualWeight", weight);
            result.put("chargeableWeight", weight.max(calculateVolumeWeight(length, width, height)));
            
            if (!validLength) result.put("lengthError", "Length must be between 1-200 cm");
            if (!validWidth) result.put("widthError", "Width must be between 1-200 cm");
            if (!validHeight) result.put("heightError", "Height must be between 1-200 cm");
            if (!validWeight) result.put("weightError", "Weight must be between 0.1-50 kg");
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", "Invalid dimension values");
        }
        
        return result;
    }

    private boolean isValidRequest(QuoteRequest request) {
        return request.getPickupAddress() != null && !request.getPickupAddress().trim().isEmpty() &&
               request.getDeliveryAddress() != null && !request.getDeliveryAddress().trim().isEmpty() &&
               request.getLength() != null && request.getLength().compareTo(BigDecimal.ZERO) > 0 &&
               request.getWidth() != null && request.getWidth().compareTo(BigDecimal.ZERO) > 0 &&
               request.getHeight() != null && request.getHeight().compareTo(BigDecimal.ZERO) > 0 &&
               request.getWeight() != null && request.getWeight().compareTo(BigDecimal.ZERO) > 0 &&
               request.getPackageType() != null && !request.getPackageType().trim().isEmpty() &&
               request.getServiceType() != null && !request.getServiceType().trim().isEmpty();
    }

    private BigDecimal calculateVolumeWeight(BigDecimal length, BigDecimal width, BigDecimal height) {
        // Volume weight = (L × W × H) ÷ 5000 (for cm to kg conversion)
        return length.multiply(width).multiply(height)
                    .divide(new BigDecimal("5000"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDistance(String pickupAddress, String deliveryAddress) {
        // Simplified distance calculation - in real app, use Google Maps Distance Matrix API
        // For now, return a random distance between 50-500 km
        Random random = new Random();
        return new BigDecimal(50 + random.nextInt(450)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateWeightCharge(BigDecimal weight) {
        BigDecimal charge = BigDecimal.ZERO;
        
        // Additional charges for weight over 5kg
        if (weight.compareTo(new BigDecimal("5")) > 0) {
            BigDecimal excessWeight = weight.subtract(new BigDecimal("5"));
            charge = charge.add(excessWeight.multiply(new BigDecimal("5")));
        }
        
        // Additional charges for weight over 10kg
        if (weight.compareTo(new BigDecimal("10")) > 0) {
            BigDecimal excessWeight = weight.subtract(new BigDecimal("10"));
            charge = charge.add(excessWeight.multiply(new BigDecimal("3")));
        }
        
        return charge.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDistanceCharge(BigDecimal distance) {
        BigDecimal charge = BigDecimal.ZERO;
        
        // Additional charges for distance over 100km
        if (distance.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal excessDistance = distance.subtract(new BigDecimal("100"));
            charge = charge.add(excessDistance.multiply(new BigDecimal("0.5")));
        }
        
        // Additional charges for distance over 500km
        if (distance.compareTo(new BigDecimal("500")) > 0) {
            BigDecimal excessDistance = distance.subtract(new BigDecimal("500"));
            charge = charge.add(excessDistance.multiply(new BigDecimal("0.3")));
        }
        
        return charge.setScale(2, RoundingMode.HALF_UP);
    }

    private String calculateEstimatedDelivery(int deliveryDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryDate = now.plusDays(deliveryDays);
        
        // Skip weekends
        while (deliveryDate.getDayOfWeek().getValue() > 5) {
            deliveryDate = deliveryDate.plusDays(1);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        return deliveryDate.format(formatter);
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}



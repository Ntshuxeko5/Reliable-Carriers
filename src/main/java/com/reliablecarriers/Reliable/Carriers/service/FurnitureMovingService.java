package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.FurnitureMovingQuoteRequest;
import com.reliablecarriers.Reliable.Carriers.model.FurnitureMovingQuote;
import com.reliablecarriers.Reliable.Carriers.repository.FurnitureMovingQuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FurnitureMovingService {

    @Autowired
    private FurnitureMovingQuoteRepository furnitureMovingQuoteRepository;

    // @Autowired
    // private GoogleMapsGeocodingService googleMapsService; // Unused for now

    // Furniture weight and complexity factors
    private static final Map<String, Double> FURNITURE_WEIGHTS = new HashMap<>();
    private static final Map<String, Double> FURNITURE_COMPLEXITY = new HashMap<>();
    
    static {
        // Living Room (weight in kg, complexity factor)
        FURNITURE_WEIGHTS.put("sofa_3seat", 80.0);
        FURNITURE_COMPLEXITY.put("sofa_3seat", 1.5);
        FURNITURE_WEIGHTS.put("sofa_2seat", 60.0);
        FURNITURE_COMPLEXITY.put("sofa_2seat", 1.3);
        FURNITURE_WEIGHTS.put("armchair", 30.0);
        FURNITURE_COMPLEXITY.put("armchair", 1.0);
        FURNITURE_WEIGHTS.put("coffee_table", 25.0);
        FURNITURE_COMPLEXITY.put("coffee_table", 1.0);
        FURNITURE_WEIGHTS.put("tv_stand", 35.0);
        FURNITURE_COMPLEXITY.put("tv_stand", 1.2);
        FURNITURE_WEIGHTS.put("bookshelf", 45.0);
        FURNITURE_COMPLEXITY.put("bookshelf", 1.4);
        
        // Bedroom
        FURNITURE_WEIGHTS.put("king_bed", 70.0);
        FURNITURE_COMPLEXITY.put("king_bed", 1.8);
        FURNITURE_WEIGHTS.put("queen_bed", 60.0);
        FURNITURE_COMPLEXITY.put("queen_bed", 1.6);
        FURNITURE_WEIGHTS.put("single_bed", 40.0);
        FURNITURE_COMPLEXITY.put("single_bed", 1.4);
        FURNITURE_WEIGHTS.put("mattress_king", 35.0);
        FURNITURE_COMPLEXITY.put("mattress_king", 1.2);
        FURNITURE_WEIGHTS.put("mattress_queen", 30.0);
        FURNITURE_COMPLEXITY.put("mattress_queen", 1.1);
        FURNITURE_WEIGHTS.put("mattress_single", 20.0);
        FURNITURE_COMPLEXITY.put("mattress_single", 1.0);
        FURNITURE_WEIGHTS.put("wardrobe", 90.0);
        FURNITURE_COMPLEXITY.put("wardrobe", 2.0);
        FURNITURE_WEIGHTS.put("dresser", 50.0);
        FURNITURE_COMPLEXITY.put("dresser", 1.3);
        FURNITURE_WEIGHTS.put("nightstand", 20.0);
        FURNITURE_COMPLEXITY.put("nightstand", 1.0);
        
        // Dining Room
        FURNITURE_WEIGHTS.put("dining_table", 55.0);
        FURNITURE_COMPLEXITY.put("dining_table", 1.4);
        FURNITURE_WEIGHTS.put("dining_chairs", 8.0);
        FURNITURE_COMPLEXITY.put("dining_chairs", 0.8);
        FURNITURE_WEIGHTS.put("china_cabinet", 75.0);
        FURNITURE_COMPLEXITY.put("china_cabinet", 1.6);
        
        // Kitchen & Appliances
        FURNITURE_WEIGHTS.put("refrigerator", 120.0);
        FURNITURE_COMPLEXITY.put("refrigerator", 2.5);
        FURNITURE_WEIGHTS.put("washing_machine", 80.0);
        FURNITURE_COMPLEXITY.put("washing_machine", 2.0);
        FURNITURE_WEIGHTS.put("dryer", 70.0);
        FURNITURE_COMPLEXITY.put("dryer", 1.8);
        FURNITURE_WEIGHTS.put("dishwasher", 60.0);
        FURNITURE_COMPLEXITY.put("dishwasher", 1.7);
        FURNITURE_WEIGHTS.put("microwave", 15.0);
        FURNITURE_COMPLEXITY.put("microwave", 1.0);
        
        // Office
        FURNITURE_WEIGHTS.put("desk", 40.0);
        FURNITURE_COMPLEXITY.put("desk", 1.2);
        FURNITURE_WEIGHTS.put("office_chair", 20.0);
        FURNITURE_COMPLEXITY.put("office_chair", 1.0);
        FURNITURE_WEIGHTS.put("filing_cabinet", 50.0);
        FURNITURE_COMPLEXITY.put("filing_cabinet", 1.3);
        
        // Miscellaneous
        FURNITURE_WEIGHTS.put("boxes_small", 5.0);
        FURNITURE_COMPLEXITY.put("boxes_small", 0.5);
        FURNITURE_WEIGHTS.put("boxes_medium", 10.0);
        FURNITURE_COMPLEXITY.put("boxes_medium", 0.7);
        FURNITURE_WEIGHTS.put("boxes_large", 15.0);
        FURNITURE_COMPLEXITY.put("boxes_large", 0.9);
        FURNITURE_WEIGHTS.put("piano", 300.0);
        FURNITURE_COMPLEXITY.put("piano", 4.0);
        FURNITURE_WEIGHTS.put("safe", 150.0);
        FURNITURE_COMPLEXITY.put("safe", 3.0);
        FURNITURE_WEIGHTS.put("exercise_equipment", 50.0);
        FURNITURE_COMPLEXITY.put("exercise_equipment", 1.5);
    }

    public Map<String, Object> generateQuote(FurnitureMovingQuoteRequest request) {
        try {
            // Calculate distance
            double distance = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDeliveryLatitude(), request.getDeliveryLongitude()
            );

            // Calculate total weight and complexity
            double totalWeight = 0;
            double totalComplexity = 0;
            int totalItems = 0;

            if (request.getInventory() != null) {
                for (Map.Entry<String, Integer> entry : request.getInventory().entrySet()) {
                    String item = entry.getKey();
                    int quantity = entry.getValue();
                    
                    if (FURNITURE_WEIGHTS.containsKey(item)) {
                        totalWeight += FURNITURE_WEIGHTS.get(item) * quantity;
                        totalComplexity += FURNITURE_COMPLEXITY.get(item) * quantity;
                        totalItems += quantity;
                    }
                }
            }

            // Base pricing calculation
            BigDecimal basePrice = calculateBasePrice(distance, totalWeight, totalComplexity, totalItems);
            
            // Apply location factors
            BigDecimal locationMultiplier = calculateLocationMultiplier(request);
            basePrice = basePrice.multiply(locationMultiplier);
            
            // Apply service additions
            BigDecimal serviceAdditions = calculateServiceAdditions(request.getServices(), basePrice);
            
            // Calculate insurance
            BigDecimal insuranceCost = BigDecimal.ZERO;
            if (request.isInsurance()) {
                insuranceCost = basePrice.multiply(new BigDecimal("0.03")); // 3% of base price
            }
            
            // Calculate total
            BigDecimal totalPrice = basePrice.add(serviceAdditions).add(insuranceCost);
            
            // Generate quote ID
            String quoteId = "FMQ" + System.currentTimeMillis();
            
            // Save quote to database
            FurnitureMovingQuote quote = new FurnitureMovingQuote();
            quote.setQuoteId(quoteId);
            quote.setCustomerName(request.getCustomerName());
            quote.setCustomerEmail(request.getCustomerEmail());
            quote.setCustomerPhone(request.getCustomerPhone());
            quote.setPickupAddress(request.getPickupAddress());
            quote.setDeliveryAddress(request.getDeliveryAddress());
            quote.setPickupLatitude(request.getPickupLatitude());
            quote.setPickupLongitude(request.getPickupLongitude());
            quote.setDeliveryLatitude(request.getDeliveryLatitude());
            quote.setDeliveryLongitude(request.getDeliveryLongitude());
            quote.setMovingDate(request.getMovingDate());
            quote.setMovingTime(request.getMovingTime());
            quote.setTotalWeight(totalWeight);
            quote.setTotalItems(totalItems);
            quote.setDistance(distance);
            quote.setBasePrice(basePrice);
            quote.setServiceAdditions(serviceAdditions);
            quote.setInsuranceCost(insuranceCost);
            quote.setTotalPrice(totalPrice);
            quote.setSpecialInstructions(request.getSpecialInstructions());
            quote.setInventoryJson(convertInventoryToJson(request.getInventory()));
            quote.setServicesJson(convertServicesToJson(request.getServices()));
            quote.setCreatedAt(LocalDateTime.now());
            
            furnitureMovingQuoteRepository.save(quote);
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("quoteId", quoteId);
            response.put("basePrice", basePrice);
            response.put("serviceAdditions", serviceAdditions);
            response.put("insuranceCost", insuranceCost);
            response.put("totalPrice", totalPrice);
            response.put("distance", distance);
            response.put("totalWeight", totalWeight);
            response.put("totalItems", totalItems);
            response.put("estimatedDuration", calculateEstimatedDuration(totalComplexity, distance));
            
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating furniture moving quote: " + e.getMessage());
        }
    }

    public Map<String, Object> getQuoteById(String quoteId) {
        Optional<FurnitureMovingQuote> quoteOpt = furnitureMovingQuoteRepository.findByQuoteId(quoteId);
        if (quoteOpt.isPresent()) {
            FurnitureMovingQuote quote = quoteOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("quoteId", quote.getQuoteId());
            response.put("customerName", quote.getCustomerName());
            response.put("customerEmail", quote.getCustomerEmail());
            response.put("customerPhone", quote.getCustomerPhone());
            response.put("pickupAddress", quote.getPickupAddress());
            response.put("deliveryAddress", quote.getDeliveryAddress());
            response.put("movingDate", quote.getMovingDate());
            response.put("movingTime", quote.getMovingTime());
            response.put("basePrice", quote.getBasePrice());
            response.put("serviceAdditions", quote.getServiceAdditions());
            response.put("insuranceCost", quote.getInsuranceCost());
            response.put("totalPrice", quote.getTotalPrice());
            response.put("distance", quote.getDistance());
            response.put("totalWeight", quote.getTotalWeight());
            response.put("totalItems", quote.getTotalItems());
            response.put("specialInstructions", quote.getSpecialInstructions());
            response.put("createdAt", quote.getCreatedAt());
            
            return response;
        }
        return null;
    }

    private BigDecimal calculateBasePrice(double distance, double totalWeight, double totalComplexity, int totalItems) {
        // Base rate per km
        BigDecimal distanceRate = new BigDecimal("15.00"); // R15 per km
        
        // Weight factor (R2 per kg)
        BigDecimal weightRate = new BigDecimal("2.00");
        
        // Complexity factor (R50 per complexity point)
        BigDecimal complexityRate = new BigDecimal("50.00");
        
        // Minimum charge
        BigDecimal minimumCharge = new BigDecimal("800.00");
        
        BigDecimal distanceCost = distanceRate.multiply(new BigDecimal(distance));
        BigDecimal weightCost = weightRate.multiply(new BigDecimal(totalWeight));
        BigDecimal complexityCost = complexityRate.multiply(new BigDecimal(totalComplexity));
        
        BigDecimal totalCost = distanceCost.add(weightCost).add(complexityCost);
        
        // Apply minimum charge
        if (totalCost.compareTo(minimumCharge) < 0) {
            totalCost = minimumCharge;
        }
        
        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLocationMultiplier(FurnitureMovingQuoteRequest request) {
        BigDecimal multiplier = BigDecimal.ONE;
        
        // Floor penalties
        if ("6+".equals(request.getPickupFloor()) || "6+".equals(request.getDeliveryFloor())) {
            multiplier = multiplier.add(new BigDecimal("0.3")); // 30% increase for high floors
        } else if (isHighFloor(request.getPickupFloor()) || isHighFloor(request.getDeliveryFloor())) {
            multiplier = multiplier.add(new BigDecimal("0.15")); // 15% increase for mid floors
        }
        
        // No elevator penalty
        if ("no".equals(request.getPickupElevator()) || "no".equals(request.getDeliveryElevator())) {
            multiplier = multiplier.add(new BigDecimal("0.25")); // 25% increase for no elevator
        }
        
        // Parking distance penalty
        if ("far".equals(request.getPickupParking()) || "far".equals(request.getDeliveryParking())) {
            multiplier = multiplier.add(new BigDecimal("0.15")); // 15% increase for far parking
        } else if ("medium".equals(request.getPickupParking()) || "medium".equals(request.getDeliveryParking())) {
            multiplier = multiplier.add(new BigDecimal("0.08")); // 8% increase for medium parking
        }
        
        return multiplier;
    }

    private boolean isHighFloor(String floor) {
        return "3".equals(floor) || "4".equals(floor) || "5".equals(floor);
    }

    private BigDecimal calculateServiceAdditions(List<String> services, BigDecimal basePrice) {
        BigDecimal additions = BigDecimal.ZERO;
        
        if (services != null) {
            for (String service : services) {
                switch (service) {
                    case "packing":
                        additions = additions.add(basePrice.multiply(new BigDecimal("0.25"))); // 25% of base
                        break;
                    case "unpacking":
                        additions = additions.add(basePrice.multiply(new BigDecimal("0.20"))); // 20% of base
                        break;
                    case "assembly":
                        additions = additions.add(basePrice.multiply(new BigDecimal("0.15"))); // 15% of base
                        break;
                    case "storage":
                        additions = additions.add(new BigDecimal("200.00")); // Flat R200 per day
                        break;
                }
            }
        }
        
        return additions.setScale(2, RoundingMode.HALF_UP);
    }

    private String calculateEstimatedDuration(double totalComplexity, double distance) {
        // Base time: 2 hours + complexity factor + travel time
        double baseHours = 2.0;
        double complexityHours = totalComplexity * 0.3; // 18 minutes per complexity point
        double travelHours = distance / 40.0; // Assuming 40 km/h average speed
        
        double totalHours = baseHours + complexityHours + travelHours;
        
        if (totalHours < 3) {
            return "3-4 hours";
        } else if (totalHours < 6) {
            return "4-6 hours";
        } else if (totalHours < 8) {
            return "6-8 hours";
        } else {
            return "Full day (8+ hours)";
        }
    }

    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return 50.0; // Default distance if coordinates not available
        }
        
        // Haversine formula for distance calculation
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        
        return Math.round(distance * 100.0) / 100.0; // Round to 2 decimal places
    }

    private String convertInventoryToJson(Map<String, Integer> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        json.append("}");
        
        return json.toString();
    }

    private String convertServicesToJson(List<String> services) {
        if (services == null || services.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < services.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append("\"").append(services.get(i)).append("\"");
        }
        json.append("]");
        
        return json.toString();
    }
}

package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
@CrossOrigin(origins = "*")
public class PricingController {

    @Autowired
    private PricingService pricingService;

    /**
     * Get all courier service prices (Gauteng only)
     */
    @GetMapping("/courier-services")
    public ResponseEntity<Map<String, Object>> getCourierServicePrices() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("prices", pricingService.getCourierServicePrices());
            response.put("availability", "Gauteng Province Only");
            response.put("note", "These prices apply only to Gauteng province. Nationwide expansion planned.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all moving service base prices
     */
    @GetMapping("/moving-services")
    public ResponseEntity<Map<String, Object>> getMovingServicePrices() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("basePrices", pricingService.getMovingServiceBasePrices());
            response.put("availability", "Nationwide");
            response.put("pricingModel", "R550 for 20km, R25/km thereafter");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calculate price for any service type
     */
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculatePrice(
            @RequestParam ServiceType serviceType,
            @RequestParam(required = false) Double distanceKm) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (serviceType.isCourierService()) {
                BigDecimal price = pricingService.calculateCourierPrice(serviceType);
                response.put("serviceType", serviceType);
                response.put("price", price);
                response.put("pricingModel", "Fixed Price");
                response.put("availability", "Gauteng Province Only");
            } else if (serviceType.isMovingService()) {
                if (distanceKm == null) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Distance is required for moving services"));
                }
                BigDecimal price = pricingService.calculateMovingServicePrice(serviceType, distanceKm);
                Map<String, Object> breakdown = pricingService.getMovingServicePricingBreakdown(distanceKm);
                
                response.put("serviceType", serviceType);
                response.put("price", price);
                response.put("breakdown", breakdown);
                response.put("pricingModel", "Distance-based");
                response.put("availability", "Nationwide");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get moving service pricing breakdown
     */
    @PostMapping("/moving-service-breakdown")
    public ResponseEntity<Map<String, Object>> getMovingServiceBreakdown(@RequestParam Double distanceKm) {
        try {
            Map<String, Object> breakdown = pricingService.getMovingServicePricingBreakdown(distanceKm);
            return ResponseEntity.ok(breakdown);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check service availability in province
     */
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam ServiceType serviceType,
            @RequestParam String province) {
        try {
            boolean isAvailable = pricingService.isServiceAvailableInProvince(serviceType, province);
            Map<String, Object> response = new HashMap<>();
            response.put("serviceType", serviceType);
            response.put("province", province);
            response.put("isAvailable", isAvailable);
            
            if (serviceType.isCourierService()) {
                response.put("note", "Courier services are currently only available in Gauteng Province");
            } else if (serviceType.isMovingService()) {
                response.put("note", "Moving services are available nationwide");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get service information
     */
    @GetMapping("/service-info/{serviceType}")
    public ResponseEntity<Map<String, Object>> getServiceInfo(@PathVariable ServiceType serviceType) {
        try {
            Map<String, Object> info = pricingService.getServiceInfo(serviceType);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get complete price list
     */
    @GetMapping("/price-list")
    public ResponseEntity<Map<String, Object>> getCompletePriceList() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Courier services
            Map<String, Object> courierServices = new HashMap<>();
            for (ServiceType serviceType : ServiceType.values()) {
                if (serviceType.isCourierService()) {
                    Map<String, Object> serviceInfo = new HashMap<>();
                    serviceInfo.put("price", serviceType.getBasePrice());
                    serviceInfo.put("description", serviceType.getDescription());
                    serviceInfo.put("availability", "Gauteng Province Only");
                    courierServices.put(serviceType.getDisplayName(), serviceInfo);
                }
            }
            response.put("courierServices", courierServices);
            
            // Moving services
            Map<String, Object> movingServices = new HashMap<>();
            for (ServiceType serviceType : ServiceType.values()) {
                if (serviceType.isMovingService()) {
                    Map<String, Object> serviceInfo = new HashMap<>();
                    serviceInfo.put("basePrice", serviceType.getBasePrice());
                    serviceInfo.put("description", serviceType.getDescription());
                    serviceInfo.put("pricingModel", "R550 for 20km, R25/km thereafter");
                    serviceInfo.put("availability", "Nationwide");
                    movingServices.put(serviceType.getDisplayName(), serviceInfo);
                }
            }
            response.put("movingServices", movingServices);
            
            response.put("note", "Courier services are currently only available in Gauteng province. Nationwide expansion is planned.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import com.reliablecarriers.Reliable.Carriers.service.PricingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PricingServiceImpl implements PricingService {

    // Moving service pricing constants
    private static final BigDecimal MAX_FREE_PRICE = new BigDecimal("550.00"); // R550
    private static final BigDecimal PRICE_PER_KM = new BigDecimal("25.00"); // R25 per km
    private static final int MAX_FREE_DISTANCE_KM = 20; // 20km

    @Override
    public BigDecimal calculateCourierPrice(ServiceType serviceType) {
        if (!serviceType.isCourierService()) {
            throw new IllegalArgumentException("Service type " + serviceType + " is not a courier service");
        }
        return serviceType.getBasePrice();
    }

    @Override
    public BigDecimal calculateMovingServicePrice(ServiceType serviceType, Double distanceKm) {
        if (!serviceType.isMovingService()) {
            throw new IllegalArgumentException("Service type " + serviceType + " is not a moving service");
        }
        
        if (distanceKm <= MAX_FREE_DISTANCE_KM) {
            return MAX_FREE_PRICE;
        } else {
            double extraDistance = distanceKm - MAX_FREE_DISTANCE_KM;
            BigDecimal extraCost = PRICE_PER_KM.multiply(BigDecimal.valueOf(extraDistance));
            return MAX_FREE_PRICE.add(extraCost);
        }
    }

    @Override
    public BigDecimal calculatePrice(ServiceType serviceType, Double distanceKm) {
        if (serviceType.isCourierService()) {
            return calculateCourierPrice(serviceType);
        } else if (serviceType.isMovingService()) {
            return calculateMovingServicePrice(serviceType, distanceKm);
        } else {
            throw new IllegalArgumentException("Unknown service type: " + serviceType);
        }
    }

    @Override
    public Map<String, Object> getMovingServicePricingBreakdown(Double distanceKm) {
        Map<String, Object> breakdown = new HashMap<>();
        
        if (distanceKm <= MAX_FREE_DISTANCE_KM) {
            breakdown.put("basePrice", MAX_FREE_PRICE);
            breakdown.put("extraDistanceCharge", BigDecimal.ZERO);
            breakdown.put("totalPrice", MAX_FREE_PRICE);
            breakdown.put("breakdown", String.format("Base price for %d km: R%s", MAX_FREE_DISTANCE_KM, MAX_FREE_PRICE));
        } else {
            double extraDistance = distanceKm - MAX_FREE_DISTANCE_KM;
            BigDecimal extraCost = PRICE_PER_KM.multiply(BigDecimal.valueOf(extraDistance));
            BigDecimal totalPrice = MAX_FREE_PRICE.add(extraCost);
            
            breakdown.put("basePrice", MAX_FREE_PRICE);
            breakdown.put("extraDistanceCharge", extraCost);
            breakdown.put("totalPrice", totalPrice);
            breakdown.put("breakdown", String.format(
                "Base price for %d km: R%s + Extra %.1f km × R%s/km = R%s",
                MAX_FREE_DISTANCE_KM, MAX_FREE_PRICE, extraDistance, PRICE_PER_KM, extraCost
            ));
        }
        
        breakdown.put("distanceKm", distanceKm);
        breakdown.put("maxFreeDistanceKm", MAX_FREE_DISTANCE_KM);
        breakdown.put("pricePerKm", PRICE_PER_KM);
        
        return breakdown;
    }

    @Override
    public Map<ServiceType, BigDecimal> getCourierServicePrices() {
        Map<ServiceType, BigDecimal> prices = new HashMap<>();
        for (ServiceType serviceType : ServiceType.values()) {
            if (serviceType.isCourierService()) {
                prices.put(serviceType, serviceType.getBasePrice());
            }
        }
        return prices;
    }

    @Override
    public Map<ServiceType, BigDecimal> getMovingServiceBasePrices() {
        Map<ServiceType, BigDecimal> prices = new HashMap<>();
        for (ServiceType serviceType : ServiceType.values()) {
            if (serviceType.isMovingService()) {
                prices.put(serviceType, serviceType.getBasePrice());
            }
        }
        return prices;
    }

    @Override
    public boolean isServiceAvailableInProvince(ServiceType serviceType, String province) {
        // Currently, courier services are only available in Gauteng
        if (serviceType.isCourierService()) {
            return "Gauteng".equalsIgnoreCase(province) || 
                   "GP".equalsIgnoreCase(province) ||
                   "Gauteng Province".equalsIgnoreCase(province);
        }
        
        // Moving services are available nationwide
        return serviceType.isMovingService();
    }

    @Override
    public Map<String, Object> getServiceInfo(ServiceType serviceType) {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceType", serviceType);
        info.put("displayName", serviceType.getDisplayName());
        info.put("description", serviceType.getDescription());
        info.put("basePrice", serviceType.getBasePrice());
        info.put("isCourierService", serviceType.isCourierService());
        info.put("isMovingService", serviceType.isMovingService());
        info.put("requiresDistanceCalculation", serviceType.requiresDistanceCalculation());
        
        if (serviceType.isCourierService()) {
            info.put("availability", "Gauteng Province Only");
            info.put("pricingModel", "Fixed Price");
        } else if (serviceType.isMovingService()) {
            info.put("availability", "Nationwide");
            info.put("pricingModel", "Distance-based (R550 for 20km, R25/km thereafter)");
        }
        
        return info;
    }
}

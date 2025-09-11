package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.ServiceType;
import java.math.BigDecimal;
import java.util.Map;

public interface PricingService {
    
    /**
     * Calculate price for courier services (fixed pricing for Gauteng)
     */
    BigDecimal calculateCourierPrice(ServiceType serviceType);
    
    /**
     * Calculate price for moving services (distance-based pricing)
     */
    BigDecimal calculateMovingServicePrice(ServiceType serviceType, Double distanceKm);
    
    /**
     * Calculate price for any service type
     */
    BigDecimal calculatePrice(ServiceType serviceType, Double distanceKm);
    
    /**
     * Get pricing breakdown for moving services
     */
    Map<String, Object> getMovingServicePricingBreakdown(Double distanceKm);
    
    /**
     * Get all courier service prices
     */
    Map<ServiceType, BigDecimal> getCourierServicePrices();
    
    /**
     * Get all moving service base prices
     */
    Map<ServiceType, BigDecimal> getMovingServiceBasePrices();
    
    /**
     * Check if service is available in specified province
     */
    boolean isServiceAvailableInProvince(ServiceType serviceType, String province);
    
    /**
     * Get service description and pricing info
     */
    Map<String, Object> getServiceInfo(ServiceType serviceType);
}

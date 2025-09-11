package com.reliablecarriers.Reliable.Carriers.model;

import java.math.BigDecimal;

public enum ServiceType {
    // Courier Services (Gauteng Province Only)
    SAME_DAY("Same Day Delivery", new BigDecimal("140.00"), "Same day delivery within Gauteng"),
    OVERNIGHT("Overnight Delivery", new BigDecimal("120.00"), "Next day delivery within Gauteng"),
    ECONOMY("Economy 2-3 Business Days", new BigDecimal("100.00"), "2-3 business days delivery within Gauteng"),
    URGENT("Urgent Delivery", new BigDecimal("425.00"), "Priority urgent delivery within Gauteng"),
    
    // Moving Services
    FURNITURE("Furniture Moving", new BigDecimal("550.00"), "Furniture moving service - 1 Load (20km): R550"),
    MOVING("Moving Service", new BigDecimal("550.00"), "Complete moving service"),
    LOAD_TRANSPORT("Load Transport", new BigDecimal("550.00"), "Heavy load transport service"),
    EXPRESS_DELIVERY("Express Delivery", new BigDecimal("550.00"), "Express delivery service"),
    SAME_DAY_DELIVERY("Same Day Delivery", new BigDecimal("550.00"), "Same day delivery service");

    private final String displayName;
    private final BigDecimal basePrice;
    private final String description;

    ServiceType(String displayName, BigDecimal basePrice, String description) {
        this.displayName = displayName;
        this.basePrice = basePrice;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCourierService() {
        return this == SAME_DAY || this == OVERNIGHT || this == ECONOMY || this == URGENT;
    }

    public boolean isMovingService() {
        return this == FURNITURE || this == MOVING || this == LOAD_TRANSPORT || 
               this == EXPRESS_DELIVERY || this == SAME_DAY_DELIVERY;
    }

    public boolean requiresDistanceCalculation() {
        return this == FURNITURE || this == MOVING || this == LOAD_TRANSPORT;
    }
}

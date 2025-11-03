package com.reliablecarriers.Reliable.Carriers.model;

public enum CustomerTier {
    INDIVIDUAL("Individual", "Basic package tracking and quotes", false, false, false),
    BUSINESS("Business", "Enhanced features for small businesses", false, false, false),
    ENTERPRISE("Enterprise", "Full features for large corporations", true, true, true),
    PREMIUM("Premium", "VIP service with priority support", true, true, true);

    private final String displayName;
    private final String description;
    private final boolean hasLiveTracking;
    private final boolean hasAnalytics;
    private final boolean hasApiAccess;

    CustomerTier(String displayName, String description, boolean hasLiveTracking, boolean hasAnalytics, boolean hasApiAccess) {
        this.displayName = displayName;
        this.description = description;
        this.hasLiveTracking = hasLiveTracking;
        this.hasAnalytics = hasAnalytics;
        this.hasApiAccess = hasApiAccess;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasLiveTracking() {
        return hasLiveTracking;
    }

    public boolean hasAnalytics() {
        return hasAnalytics;
    }

    public boolean hasApiAccess() {
        return hasApiAccess;
    }

    public boolean isPremiumTier() {
        return this == ENTERPRISE || this == PREMIUM;
    }
}

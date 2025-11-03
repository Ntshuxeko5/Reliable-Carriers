package com.reliablecarriers.Reliable.Carriers.model;

import java.util.List;
import java.util.ArrayList;

public class CustomerTierFeatures {
    private CustomerTier tier;
    private List<String> availableFeatures;
    private List<String> restrictions;
    private boolean hasLiveTracking;
    private boolean hasAnalytics;
    private boolean hasApiAccess;
    private boolean hasPrioritySupport;
    private boolean hasBulkShipping;
    private int maxPackagesPerMonth;
    private double discountPercentage;

    public CustomerTierFeatures(CustomerTier tier) {
        this.tier = tier;
        this.availableFeatures = new ArrayList<>();
        this.restrictions = new ArrayList<>();
        initializeFeatures();
    }

    private void initializeFeatures() {
        switch (tier) {
            case INDIVIDUAL:
                availableFeatures.add("Basic package tracking");
                availableFeatures.add("Email notifications");
                availableFeatures.add("Standard quotes");
                availableFeatures.add("Customer support");
                restrictions.add("No live driver tracking");
                restrictions.add("No analytics dashboard");
                restrictions.add("No API access");
                hasLiveTracking = false;
                hasAnalytics = false;
                hasApiAccess = false;
                hasPrioritySupport = false;
                hasBulkShipping = false;
                maxPackagesPerMonth = 10;
                discountPercentage = 0.0;
                break;
                
            case BUSINESS:
                availableFeatures.add("Enhanced package tracking");
                availableFeatures.add("SMS + Email notifications");
                availableFeatures.add("Priority quotes");
                availableFeatures.add("Basic analytics");
                availableFeatures.add("Bulk shipping");
                availableFeatures.add("Priority support");
                restrictions.add("No live driver tracking");
                restrictions.add("No API access");
                hasLiveTracking = false;
                hasAnalytics = false;
                hasApiAccess = false;
                hasPrioritySupport = true;
                hasBulkShipping = true;
                maxPackagesPerMonth = 100;
                discountPercentage = 5.0;
                break;
                
            case ENTERPRISE:
                availableFeatures.add("Live driver tracking");
                availableFeatures.add("Advanced analytics");
                availableFeatures.add("API access");
                availableFeatures.add("Custom integrations");
                availableFeatures.add("Dedicated account manager");
                availableFeatures.add("White-label solutions");
                availableFeatures.add("Custom reporting");
                hasLiveTracking = true;
                hasAnalytics = true;
                hasApiAccess = true;
                hasPrioritySupport = true;
                hasBulkShipping = true;
                maxPackagesPerMonth = -1; // Unlimited
                discountPercentage = 15.0;
                break;
                
            case PREMIUM:
                availableFeatures.add("All Enterprise features");
                availableFeatures.add("VIP support");
                availableFeatures.add("Same-day delivery");
                availableFeatures.add("Custom delivery windows");
                availableFeatures.add("Dedicated driver assignment");
                availableFeatures.add("Real-time notifications");
                hasLiveTracking = true;
                hasAnalytics = true;
                hasApiAccess = true;
                hasPrioritySupport = true;
                hasBulkShipping = true;
                maxPackagesPerMonth = -1; // Unlimited
                discountPercentage = 20.0;
                break;
        }
    }

    // Getters
    public CustomerTier getTier() {
        return tier;
    }

    public List<String> getAvailableFeatures() {
        return availableFeatures;
    }

    public List<String> getRestrictions() {
        return restrictions;
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

    public boolean hasPrioritySupport() {
        return hasPrioritySupport;
    }

    public boolean hasBulkShipping() {
        return hasBulkShipping;
    }

    public int getMaxPackagesPerMonth() {
        return maxPackagesPerMonth;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }
}

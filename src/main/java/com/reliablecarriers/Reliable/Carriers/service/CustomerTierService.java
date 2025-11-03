package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.CustomerTier;
import com.reliablecarriers.Reliable.Carriers.model.CustomerTierFeatures;
import com.reliablecarriers.Reliable.Carriers.model.User;

public interface CustomerTierService {
    
    /**
     * Check if user has access to live driver tracking
     */
    boolean hasLiveTrackingAccess(User user);
    
    /**
     * Check if user has access to analytics dashboard
     */
    boolean hasAnalyticsAccess(User user);
    
    /**
     * Check if user has API access
     */
    boolean hasApiAccess(User user);
    
    /**
     * Check if user is premium tier
     */
    boolean isPremiumTier(User user);
    
    /**
     * Get user's tier features
     */
    CustomerTierFeatures getTierFeatures(User user);
    
    /**
     * Upgrade user to higher tier
     */
    boolean upgradeUserTier(User user, CustomerTier newTier);
    
    /**
     * Get tier-specific features for display
     */
    String getTierDescription(CustomerTier tier);
    
    /**
     * Check if feature is available for user's tier
     */
    boolean isFeatureAvailable(User user, String feature);
}

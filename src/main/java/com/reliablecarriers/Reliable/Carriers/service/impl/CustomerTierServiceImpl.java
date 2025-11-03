package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.CustomerTier;
import com.reliablecarriers.Reliable.Carriers.model.CustomerTierFeatures;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.CustomerTierService;
import org.springframework.stereotype.Service;

@Service
public class CustomerTierServiceImpl implements CustomerTierService {

    @Override
    public boolean hasLiveTrackingAccess(User user) {
        if (user == null || user.getCustomerTier() == null) {
            return false;
        }
        return user.getCustomerTier().hasLiveTracking();
    }

    @Override
    public boolean hasAnalyticsAccess(User user) {
        if (user == null || user.getCustomerTier() == null) {
            return false;
        }
        return user.getCustomerTier().hasAnalytics();
    }

    @Override
    public boolean hasApiAccess(User user) {
        if (user == null || user.getCustomerTier() == null) {
            return false;
        }
        return user.getCustomerTier().hasApiAccess();
    }

    @Override
    public boolean isPremiumTier(User user) {
        if (user == null || user.getCustomerTier() == null) {
            return false;
        }
        return user.getCustomerTier().isPremiumTier();
    }

    @Override
    public CustomerTierFeatures getTierFeatures(User user) {
        if (user == null || user.getCustomerTier() == null) {
            return new CustomerTierFeatures(CustomerTier.INDIVIDUAL);
        }
        return new CustomerTierFeatures(user.getCustomerTier());
    }

    @Override
    public boolean upgradeUserTier(User user, CustomerTier newTier) {
        if (user == null || newTier == null) {
            return false;
        }
        
        // Check if upgrade is valid (can't downgrade)
        CustomerTier currentTier = user.getCustomerTier();
        if (currentTier == null) {
            currentTier = CustomerTier.INDIVIDUAL;
        }
        
        // Allow upgrade to higher tier
        if (isValidUpgrade(currentTier, newTier)) {
            user.setCustomerTier(newTier);
            return true;
        }
        
        return false;
    }

    @Override
    public String getTierDescription(CustomerTier tier) {
        if (tier == null) {
            return "No tier assigned";
        }
        return tier.getDescription();
    }

    @Override
    public boolean isFeatureAvailable(User user, String feature) {
        if (user == null || user.getCustomerTier() == null || feature == null) {
            return false;
        }
        
        CustomerTier tier = user.getCustomerTier();
        
        switch (feature.toLowerCase()) {
            case "live_tracking":
            case "driver_tracking":
                return tier.hasLiveTracking();
            case "analytics":
            case "dashboard":
                return tier.hasAnalytics();
            case "api_access":
            case "api":
                return tier.hasApiAccess();
            case "bulk_shipping":
                return tier == CustomerTier.BUSINESS || tier.isPremiumTier();
            case "priority_support":
                return tier != CustomerTier.INDIVIDUAL;
            case "custom_integrations":
                return tier.isPremiumTier();
            default:
                return false;
        }
    }

    private boolean isValidUpgrade(CustomerTier currentTier, CustomerTier newTier) {
        // Define upgrade hierarchy
        switch (currentTier) {
            case INDIVIDUAL:
                return newTier == CustomerTier.BUSINESS || 
                       newTier == CustomerTier.ENTERPRISE || 
                       newTier == CustomerTier.PREMIUM;
            case BUSINESS:
                return newTier == CustomerTier.ENTERPRISE || 
                       newTier == CustomerTier.PREMIUM;
            case ENTERPRISE:
                return newTier == CustomerTier.PREMIUM;
            case PREMIUM:
                return false; // Already at highest tier
            default:
                return false;
        }
    }
}

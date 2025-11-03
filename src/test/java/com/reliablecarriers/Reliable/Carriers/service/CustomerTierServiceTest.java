package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.CustomerTier;
import com.reliablecarriers.Reliable.Carriers.model.CustomerTierFeatures;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.impl.CustomerTierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerTierServiceTest {

    private CustomerTierService customerTierService;
    private User testUser;

    @BeforeEach
    void setUp() {
        customerTierService = new CustomerTierServiceImpl();
        
        // Create a test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setCustomerTier(CustomerTier.INDIVIDUAL);
    }

    @Test
    void testGetTierFeatures() {
        // Test getting tier features for individual user
        CustomerTierFeatures features = customerTierService.getTierFeatures(testUser);
        
        assertNotNull(features);
        assertEquals(CustomerTier.INDIVIDUAL, features.getTier());
        assertFalse(features.hasLiveTracking());
        assertFalse(features.hasAnalytics());
        assertFalse(features.hasApiAccess());
        assertEquals(10, features.getMaxPackagesPerMonth());
        assertEquals(0.0, features.getDiscountPercentage());
    }

    @Test
    void testHasLiveTrackingAccess() {
        // Individual tier should not have live tracking
        assertFalse(customerTierService.hasLiveTrackingAccess(testUser));
        
        // Upgrade to business tier
        testUser.setCustomerTier(CustomerTier.BUSINESS);
        assertFalse(customerTierService.hasLiveTrackingAccess(testUser));
        
        // Upgrade to enterprise tier
        testUser.setCustomerTier(CustomerTier.ENTERPRISE);
        assertTrue(customerTierService.hasLiveTrackingAccess(testUser));
    }

    @Test
    void testHasAnalyticsAccess() {
        // Individual tier should not have analytics
        assertFalse(customerTierService.hasAnalyticsAccess(testUser));
        
        // Upgrade to business tier
        testUser.setCustomerTier(CustomerTier.BUSINESS);
        assertFalse(customerTierService.hasAnalyticsAccess(testUser));
        
        // Upgrade to enterprise tier
        testUser.setCustomerTier(CustomerTier.ENTERPRISE);
        assertTrue(customerTierService.hasAnalyticsAccess(testUser));
    }

    @Test
    void testHasApiAccess() {
        // Individual tier should not have API access
        assertFalse(customerTierService.hasApiAccess(testUser));
        
        // Upgrade to business tier
        testUser.setCustomerTier(CustomerTier.BUSINESS);
        assertFalse(customerTierService.hasApiAccess(testUser));
        
        // Upgrade to enterprise tier
        testUser.setCustomerTier(CustomerTier.ENTERPRISE);
        assertTrue(customerTierService.hasApiAccess(testUser));
    }

    @Test
    void testIsPremiumTier() {
        // Individual tier is not premium
        assertFalse(customerTierService.isPremiumTier(testUser));
        
        // Business tier is not premium
        testUser.setCustomerTier(CustomerTier.BUSINESS);
        assertFalse(customerTierService.isPremiumTier(testUser));
        
        // Enterprise tier is premium
        testUser.setCustomerTier(CustomerTier.ENTERPRISE);
        assertTrue(customerTierService.isPremiumTier(testUser));
        
        // Premium tier is premium
        testUser.setCustomerTier(CustomerTier.PREMIUM);
        assertTrue(customerTierService.isPremiumTier(testUser));
    }

    @Test
    void testIsFeatureAvailable() {
        // Test various features
        assertFalse(customerTierService.isFeatureAvailable(testUser, "live_tracking"));
        assertFalse(customerTierService.isFeatureAvailable(testUser, "analytics"));
        assertFalse(customerTierService.isFeatureAvailable(testUser, "api_access"));
        assertFalse(customerTierService.isFeatureAvailable(testUser, "bulk_shipping"));
        assertFalse(customerTierService.isFeatureAvailable(testUser, "priority_support"));
        
        // Upgrade to business tier
        testUser.setCustomerTier(CustomerTier.BUSINESS);
        assertFalse(customerTierService.isFeatureAvailable(testUser, "live_tracking"));
        assertFalse(customerTierService.isFeatureAvailable(testUser, "analytics"));
        assertFalse(customerTierService.isFeatureAvailable(testUser, "api_access"));
        assertTrue(customerTierService.isFeatureAvailable(testUser, "bulk_shipping"));
        assertTrue(customerTierService.isFeatureAvailable(testUser, "priority_support"));
        
        // Upgrade to enterprise tier
        testUser.setCustomerTier(CustomerTier.ENTERPRISE);
        assertTrue(customerTierService.isFeatureAvailable(testUser, "live_tracking"));
        assertTrue(customerTierService.isFeatureAvailable(testUser, "analytics"));
        assertTrue(customerTierService.isFeatureAvailable(testUser, "api_access"));
        assertTrue(customerTierService.isFeatureAvailable(testUser, "bulk_shipping"));
        assertTrue(customerTierService.isFeatureAvailable(testUser, "priority_support"));
        assertTrue(customerTierService.isFeatureAvailable(testUser, "custom_integrations"));
    }

    @Test
    void testUpgradeUserTier() {
        // Test valid upgrade from individual to business
        boolean upgraded = customerTierService.upgradeUserTier(testUser, CustomerTier.BUSINESS);
        assertTrue(upgraded);
        assertEquals(CustomerTier.BUSINESS, testUser.getCustomerTier());
        
        // Test valid upgrade from business to enterprise
        upgraded = customerTierService.upgradeUserTier(testUser, CustomerTier.ENTERPRISE);
        assertTrue(upgraded);
        assertEquals(CustomerTier.ENTERPRISE, testUser.getCustomerTier());
        
        // Test invalid downgrade (should fail)
        upgraded = customerTierService.upgradeUserTier(testUser, CustomerTier.BUSINESS);
        assertFalse(upgraded);
        assertEquals(CustomerTier.ENTERPRISE, testUser.getCustomerTier());
    }

    @Test
    void testGetTierDescription() {
        String description = customerTierService.getTierDescription(CustomerTier.INDIVIDUAL);
        assertEquals("Basic package tracking and quotes", description);
        
        description = customerTierService.getTierDescription(CustomerTier.BUSINESS);
        assertEquals("Enhanced features for small businesses", description);
        
        description = customerTierService.getTierDescription(CustomerTier.ENTERPRISE);
        assertEquals("Full features for large corporations", description);
        
        description = customerTierService.getTierDescription(CustomerTier.PREMIUM);
        assertEquals("VIP service with priority support", description);
    }

    @Test
    void testNullUser() {
        // Test with null user
        assertFalse(customerTierService.hasLiveTrackingAccess(null));
        assertFalse(customerTierService.hasAnalyticsAccess(null));
        assertFalse(customerTierService.hasApiAccess(null));
        assertFalse(customerTierService.isPremiumTier(null));
        assertFalse(customerTierService.isFeatureAvailable(null, "live_tracking"));
        assertFalse(customerTierService.upgradeUserTier(null, CustomerTier.BUSINESS));
        
        CustomerTierFeatures features = customerTierService.getTierFeatures(null);
        assertNotNull(features);
        assertEquals(CustomerTier.INDIVIDUAL, features.getTier());
    }
}





package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Configuration for API keys and external service credentials
 * Securely manages API keys without exposing them in templates
 */
@Configuration
@ControllerAdvice
public class ApiKeyConfig {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Value("${paystack.public.key:}")
    private String paystackPublicKey;

    /**
     * Make Google Maps API key available to all templates securely
     * Only exposes the key server-side, not in client-side code
     */
    @ModelAttribute("googleMapsApiKey")
    public String getGoogleMapsApiKey() {
        // Return a placeholder for demo if not configured
        return googleMapsApiKey.isEmpty() ? "DEMO_MODE" : googleMapsApiKey;
    }

    /**
     * Make Paystack public key available to templates securely
     */
    @ModelAttribute("paystackPublicKey")
    public String getPaystackPublicKey() {
        // Return a placeholder for demo if not configured
        return paystackPublicKey.isEmpty() ? "pk_test_demo" : paystackPublicKey;
    }

    /**
     * Check if we're in demo mode (no real API keys configured)
     */
    @ModelAttribute("isDemoMode")
    public boolean isDemoMode() {
        return googleMapsApiKey.isEmpty() || paystackPublicKey.isEmpty();
    }

    /**
     * Get configuration status for admin dashboard
     */
    public ConfigurationStatus getConfigurationStatus() {
        ConfigurationStatus status = new ConfigurationStatus();
        status.setGoogleMapsConfigured(!googleMapsApiKey.isEmpty());
        status.setPaystackConfigured(!paystackPublicKey.isEmpty());
        status.setDemoMode(isDemoMode());
        return status;
    }

    // Configuration status class
    public static class ConfigurationStatus {
        private boolean googleMapsConfigured;
        private boolean paystackConfigured;
        private boolean demoMode;

        // Getters and setters
        public boolean isGoogleMapsConfigured() { return googleMapsConfigured; }
        public void setGoogleMapsConfigured(boolean googleMapsConfigured) { this.googleMapsConfigured = googleMapsConfigured; }

        public boolean isPaystackConfigured() { return paystackConfigured; }
        public void setPaystackConfigured(boolean paystackConfigured) { this.paystackConfigured = paystackConfigured; }

        public boolean isDemoMode() { return demoMode; }
        public void setDemoMode(boolean demoMode) { this.demoMode = demoMode; }
    }
}

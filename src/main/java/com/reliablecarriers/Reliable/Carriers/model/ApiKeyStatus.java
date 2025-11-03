package com.reliablecarriers.Reliable.Carriers.model;

/**
 * API Key status enum
 */
public enum ApiKeyStatus {
    ACTIVE("Active", "API key is active and can be used"),
    INACTIVE("Inactive", "API key is disabled"),
    EXPIRED("Expired", "API key has expired"),
    REVOKED("Revoked", "API key has been revoked");
    
    private final String displayName;
    private final String description;
    
    ApiKeyStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isActive() {
        return this == ACTIVE;
    }
}


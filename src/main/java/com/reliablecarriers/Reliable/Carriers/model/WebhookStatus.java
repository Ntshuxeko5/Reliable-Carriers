package com.reliablecarriers.Reliable.Carriers.model;

public enum WebhookStatus {
    ACTIVE("Active", "Webhook is active and will receive events"),
    INACTIVE("Inactive", "Webhook is disabled"),
    SUSPENDED("Suspended", "Webhook is suspended due to failures");
    
    private final String displayName;
    private final String description;
    
    WebhookStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}






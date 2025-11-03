package com.reliablecarriers.Reliable.Carriers.model;

public enum DocumentVerificationStatus {
    PENDING("Pending", "Awaiting verification"),
    VERIFIED("Verified", "Document verified and approved"),
    REJECTED("Rejected", "Document rejected"),
    EXPIRED("Expired", "Document has expired");
    
    private final String displayName;
    private final String description;
    
    DocumentVerificationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}






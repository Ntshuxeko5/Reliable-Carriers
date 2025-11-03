package com.reliablecarriers.Reliable.Carriers.model;

/**
 * Business verification status enum
 * Tracks the verification state of business accounts
 */
public enum BusinessVerificationStatus {
    PENDING("Pending", "Awaiting verification"),
    UNDER_REVIEW("Under Review", "Documents are being reviewed by admin"),
    APPROVED("Approved", "Business account has been verified and approved"),
    REJECTED("Rejected", "Business verification was rejected"),
    VERIFIED("Verified", "Business has been fully verified through automated checks");
    
    private final String displayName;
    private final String description;
    
    BusinessVerificationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isVerified() {
        return this == APPROVED || this == VERIFIED;
    }
    
    public boolean canOperate() {
        return this == APPROVED || this == VERIFIED;
    }
}


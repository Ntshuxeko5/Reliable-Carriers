package com.reliablecarriers.Reliable.Carriers.model;

/**
 * Driver verification status enum
 */
public enum DriverVerificationStatus {
    PENDING("Pending", "Driver registration is pending document verification"),
    DOCUMENTS_SUBMITTED("Documents Submitted", "Documents have been submitted and are under review"),
    UNDER_REVIEW("Under Review", "Documents are being reviewed by admin"),
    APPROVED("Approved", "Driver has been verified and approved"),
    REJECTED("Rejected", "Driver verification was rejected"),
    SUSPENDED("Suspended", "Driver account has been suspended");
    
    private final String displayName;
    private final String description;
    
    DriverVerificationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canWork() {
        return this == APPROVED;
    }
    
    public boolean isPending() {
        return this == PENDING || this == DOCUMENTS_SUBMITTED || this == UNDER_REVIEW;
    }
}






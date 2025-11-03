package com.reliablecarriers.Reliable.Carriers.model;

/**
 * Driver document types
 */
public enum DriverDocumentType {
    DRIVER_LICENSE("Driver's License", "Valid driver's license", true),
    ID_DOCUMENT("ID Document", "National ID or passport", true),
    VEHICLE_REGISTRATION("Vehicle Registration", "Vehicle registration certificate", true),
    VEHICLE_INSURANCE("Vehicle Insurance", "Valid vehicle insurance certificate", true),
    PROOF_OF_ADDRESS("Proof of Address", "Utility bill or bank statement", false),
    BACKGROUND_CHECK("Background Check", "Criminal background check", false),
    MEDICAL_CERTIFICATE("Medical Certificate", "Medical fitness certificate", false);
    
    private final String displayName;
    private final String description;
    private final boolean required;
    
    DriverDocumentType(String displayName, String description, boolean required) {
        this.displayName = displayName;
        this.description = description;
        this.required = required;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean isRequired() { return required; }
}






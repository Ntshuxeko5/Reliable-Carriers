package com.reliablecarriers.Reliable.Carriers.model;

/**
 * Driver document types - all documents must be certified copies
 */
public enum DriverDocumentType {
    DRIVER_LICENSE("Driver's License", 
        "Valid driver's license - Certified copy required. Must be valid for at least 6 months", true),
    ID_DOCUMENT("ID Document", 
        "Certified copy of National ID or passport (front and back)", true),
    VEHICLE_REGISTRATION("Vehicle Registration", 
        "Certified copy of vehicle registration certificate (eNatis document)", true),
    VEHICLE_INSURANCE("Vehicle Insurance", 
        "Certified copy of valid vehicle insurance certificate. Must be comprehensive insurance", true),
    PROOF_OF_ADDRESS("Proof of Address", 
        "Certified copy of utility bill or bank statement (not older than 3 months)", false),
    BACKGROUND_CHECK("Background Check", 
        "Certified criminal background check from SAPS (South African Police Service)", false),
    MEDICAL_CERTIFICATE("Medical Certificate", 
        "Certified medical fitness certificate from registered medical practitioner", false);
    
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






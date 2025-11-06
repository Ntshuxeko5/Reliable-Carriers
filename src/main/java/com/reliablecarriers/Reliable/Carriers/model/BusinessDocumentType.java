package com.reliablecarriers.Reliable.Carriers.model;

/**
 * Business document types - all documents must be certified copies
 */
public enum BusinessDocumentType {
    REGISTRATION_CERTIFICATE("Business Registration Certificate", 
        "CIPC (Companies and Intellectual Property Commission) registration certificate - Certified copy required", true),
    VAT_REGISTRATION("VAT/Tax Registration Certificate", 
        "SARS VAT registration certificate - Certified copy required", true),
    TAX_CLEARANCE("Tax Clearance Certificate", 
        "SARS tax clearance certificate - Certified copy required", true),
    IDENTITY_DOCUMENT("Director/Owner ID", 
        "Certified copy of ID document or passport of business owner/director", true),
    PROOF_OF_ADDRESS("Business Address Proof", 
        "Certified copy of utility bill, lease agreement, or municipal rates account in business name", true),
    BANK_STATEMENT("Bank Statement", 
        "Certified bank statement (not older than 3 months) showing business account", true),
    AUTHORIZATION_LETTER("Authorization Letter", 
        "Certified letter authorizing the registrant to represent the business", false),
    BUSINESS_PLAN("Business Plan", 
        "Business plan or operational overview (optional)", false),
    TRADE_LICENSE("Trade License", 
        "Municipal trade license (if applicable) - Certified copy", false);
    
    private final String displayName;
    private final String description;
    private final boolean required;
    
    BusinessDocumentType(String displayName, String description, boolean required) {
        this.displayName = displayName;
        this.description = description;
        this.required = required;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public boolean isRequired() { return required; }
}



package com.reliablecarriers.Reliable.Carriers.service;

/**
 * Service for automated verification of business registrations and tax IDs
 * using external APIs (CIPC and SARS)
 */
public interface AutomatedVerificationService {
    
    /**
     * Verify a business registration number with CIPC
     * @param registrationNumber The business registration number
     * @return Verification result
     */
    BusinessVerificationResult verifyBusinessRegistration(String registrationNumber);
    
    /**
     * Verify a tax registration ID with SARS
     * @param taxId The tax registration ID
     * @return Verification result
     */
    TaxVerificationResult verifyTaxRegistration(String taxId);
    
    /**
     * Verify both business registration and tax ID
     * @param registrationNumber The business registration number
     * @param taxId The tax registration ID
     * @return Combined verification result
     */
    CombinedVerificationResult verifyBusiness(String registrationNumber, String taxId);
    
    /**
     * Result class for business verification
     */
    class BusinessVerificationResult {
        private String registrationNumber;
        private boolean verified;
        private String errorMessage;
        private String businessName;
        private String registrationStatus;
        
        // Getters and setters
        public String getRegistrationNumber() { return registrationNumber; }
        public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
        
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        
        public String getRegistrationStatus() { return registrationStatus; }
        public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    }
    
    /**
     * Result class for tax verification
     */
    class TaxVerificationResult {
        private String taxId;
        private boolean verified;
        private String errorMessage;
        private String taxStatus;
        
        // Getters and setters
        public String getTaxId() { return taxId; }
        public void setTaxId(String taxId) { this.taxId = taxId; }
        
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getTaxStatus() { return taxStatus; }
        public void setTaxStatus(String taxStatus) { this.taxStatus = taxStatus; }
    }
    
    /**
     * Result class for combined verification
     */
    class CombinedVerificationResult {
        private BusinessVerificationResult businessVerification;
        private TaxVerificationResult taxVerification;
        private boolean allVerified;
        
        // Getters and setters
        public BusinessVerificationResult getBusinessVerification() { return businessVerification; }
        public void setBusinessVerification(BusinessVerificationResult businessVerification) { 
            this.businessVerification = businessVerification; 
        }
        
        public TaxVerificationResult getTaxVerification() { return taxVerification; }
        public void setTaxVerification(TaxVerificationResult taxVerification) { 
            this.taxVerification = taxVerification; 
        }
        
        public boolean isAllVerified() { return allVerified; }
        public void setAllVerified(boolean allVerified) { this.allVerified = allVerified; }
    }
}

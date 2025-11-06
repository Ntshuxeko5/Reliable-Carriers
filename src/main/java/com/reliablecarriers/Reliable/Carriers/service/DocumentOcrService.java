package com.reliablecarriers.Reliable.Carriers.service;

import java.io.File;

/**
 * Service for OCR (Optical Character Recognition) validation of documents
 * Extracts text and validates document authenticity
 */
public interface DocumentOcrService {
    
    /**
     * Extract text from document image/PDF
     * @param documentFile The document file
     * @return Extracted text content
     */
    String extractText(File documentFile);
    
    /**
     * Validate document type based on OCR content
     * @param documentFile The document file
     * @param expectedType Expected document type
     * @return Validation result
     */
    DocumentValidationResult validateDocumentType(File documentFile, String expectedType);
    
    /**
     * Extract key information from ID document
     * @param documentFile The ID document file
     * @return Extracted ID information
     */
    IdDocumentInfo extractIdInfo(File documentFile);
    
    /**
     * Extract key information from driver's license
     * @param documentFile The driver's license file
     * @return Extracted license information
     */
    LicenseInfo extractLicenseInfo(File documentFile);
    
    /**
     * Check if document appears to be a certified copy
     * @param documentFile The document file
     * @return True if document appears to be certified
     */
    boolean isCertifiedCopy(File documentFile);
    
    /**
     * Document validation result
     */
    class DocumentValidationResult {
        private boolean valid;
        private String documentType;
        private double confidence;
        private String errorMessage;
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    /**
     * ID document information
     */
    class IdDocumentInfo {
        private String idNumber;
        private String fullName;
        private String dateOfBirth;
        private String nationality;
        
        // Getters and setters
        public String getIdNumber() { return idNumber; }
        public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public String getNationality() { return nationality; }
        public void setNationality(String nationality) { this.nationality = nationality; }
    }
    
    /**
     * Driver's license information
     */
    class LicenseInfo {
        private String licenseNumber;
        private String fullName;
        private String dateOfBirth;
        private String licenseClass;
        private String expiryDate;
        
        // Getters and setters
        public String getLicenseNumber() { return licenseNumber; }
        public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        public String getLicenseClass() { return licenseClass; }
        public void setLicenseClass(String licenseClass) { this.licenseClass = licenseClass; }
        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    }
}





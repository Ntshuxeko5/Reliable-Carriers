package com.reliablecarriers.Reliable.Carriers.model;

public enum SecurityFeature {
    BIOMETRIC_AUTH("Biometric Authentication", "Fingerprint and face recognition"),
    PHOTO_VERIFICATION("Photo Verification", "Driver and package photo verification"),
    DIGITAL_SIGNATURE("Digital Signature", "Electronic signature capture"),
    TWO_FACTOR_AUTH("Two-Factor Authentication", "SMS and email verification"),
    SECURE_DOCUMENTS("Secure Documents", "Encrypted document sharing"),
    FRAUD_DETECTION("Fraud Detection", "AI-powered fraud prevention");

    private final String displayName;
    private final String description;

    SecurityFeature(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}


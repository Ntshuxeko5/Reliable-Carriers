package com.reliablecarriers.Reliable.Carriers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.HashMap;

public class PaystackMetadata {
    
    @JsonProperty("custom_fields")
    private Map<String, String> customFields;
    
    @JsonProperty("cancel_action")
    private String cancelAction;
    
    public PaystackMetadata() {
        this.customFields = new HashMap<>();
    }
    
    public PaystackMetadata(Map<String, String> customFields) {
        this.customFields = customFields != null ? customFields : new HashMap<>();
    }
    
    // Getters and Setters
    public Map<String, String> getCustomFields() {
        return customFields;
    }
    
    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }
    
    public String getCancelAction() {
        return cancelAction;
    }
    
    public void setCancelAction(String cancelAction) {
        this.cancelAction = cancelAction;
    }
    
    // Helper methods
    public void addCustomField(String key, String value) {
        if (customFields == null) {
            customFields = new HashMap<>();
        }
        customFields.put(key, value);
    }
    
    public String getCustomField(String key) {
        return customFields != null ? customFields.get(key) : null;
    }
}

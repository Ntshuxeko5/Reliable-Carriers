package com.reliablecarriers.Reliable.Carriers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaystackResponse {
    
    @JsonProperty("status")
    private boolean status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private PaystackData data;
    
    public PaystackResponse() {}
    
    // Getters and Setters
    public boolean isStatus() {
        return status;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public PaystackData getData() {
        return data;
    }
    
    public void setData(PaystackData data) {
        this.data = data;
    }
    
    public static class PaystackData {
        
        @JsonProperty("authorization_url")
        private String authorizationUrl;
        
        @JsonProperty("access_code")
        private String accessCode;
        
        @JsonProperty("reference")
        private String reference;
        
        @JsonProperty("amount")
        private Long amount;
        
        @JsonProperty("currency")
        private String currency;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("gateway_response")
        private String gatewayResponse;
        
        @JsonProperty("channel")
        private String channel;
        
        @JsonProperty("paid_at")
        private String paidAt;
        
        @JsonProperty("metadata")
        private PaystackMetadata metadata;
        
        public PaystackData() {}
        
        // Getters and Setters
        public String getAuthorizationUrl() {
            return authorizationUrl;
        }
        
        public void setAuthorizationUrl(String authorizationUrl) {
            this.authorizationUrl = authorizationUrl;
        }
        
        public String getAccessCode() {
            return accessCode;
        }
        
        public void setAccessCode(String accessCode) {
            this.accessCode = accessCode;
        }
        
        public String getReference() {
            return reference;
        }
        
        public void setReference(String reference) {
            this.reference = reference;
        }
        
        public Long getAmount() {
            return amount;
        }
        
        public void setAmount(Long amount) {
            this.amount = amount;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getGatewayResponse() {
            return gatewayResponse;
        }
        
        public void setGatewayResponse(String gatewayResponse) {
            this.gatewayResponse = gatewayResponse;
        }
        
        public String getChannel() {
            return channel;
        }
        
        public void setChannel(String channel) {
            this.channel = channel;
        }
        
        public String getPaidAt() {
            return paidAt;
        }
        
        public void setPaidAt(String paidAt) {
            this.paidAt = paidAt;
        }
        
        public PaystackMetadata getMetadata() {
            return metadata;
        }
        
        public void setMetadata(PaystackMetadata metadata) {
            this.metadata = metadata;
        }
    }
}

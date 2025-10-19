package com.reliablecarriers.Reliable.Carriers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class PaystackRequest {
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("callback_url")
    private String callbackUrl;
    
    @JsonProperty("redirect_url")
    private String redirectUrl;
    
    @JsonProperty("currency")
    private String currency = "NGN";
    
    @JsonProperty("metadata")
    private PaystackMetadata metadata;
    
    public PaystackRequest() {}
    
    public PaystackRequest(BigDecimal amount, String email, String reference, String callbackUrl) {
        this.amount = amount;
        this.email = email;
        this.reference = reference;
        this.callbackUrl = callbackUrl;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public PaystackMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(PaystackMetadata metadata) {
        this.metadata = metadata;
    }
    
    // Helper method to convert amount to kobo (Paystack expects amount in kobo)
    public BigDecimal getAmountInKobo() {
        return amount.multiply(new BigDecimal("100"));
    }
}

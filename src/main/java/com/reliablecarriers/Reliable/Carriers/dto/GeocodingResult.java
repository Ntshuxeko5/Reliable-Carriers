package com.reliablecarriers.Reliable.Carriers.dto;

import java.math.BigDecimal;

public class GeocodingResult {
    private String formattedAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String placeId;
    private boolean valid;
    
    public GeocodingResult() {}
    
    public GeocodingResult(String formattedAddress, BigDecimal latitude, BigDecimal longitude, String placeId, boolean valid) {
        this.formattedAddress = formattedAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
        this.valid = valid;
    }
    
    // Getters and setters
    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }
    
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}


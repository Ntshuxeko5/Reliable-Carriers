package com.reliablecarriers.Reliable.Carriers.dto;

import java.math.BigDecimal;

public class AddressCoordinates {
    private String formattedAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String placeId;
    
    public AddressCoordinates() {}
    
    public AddressCoordinates(String formattedAddress, BigDecimal latitude, BigDecimal longitude, String placeId) {
        this.formattedAddress = formattedAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
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
    
    @Override
    public String toString() {
        return "AddressCoordinates{" +
                "formattedAddress='" + formattedAddress + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}


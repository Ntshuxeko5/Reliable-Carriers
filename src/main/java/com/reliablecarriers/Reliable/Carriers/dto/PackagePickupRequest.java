package com.reliablecarriers.Reliable.Carriers.dto;

import org.springframework.web.multipart.MultipartFile;

public class PackagePickupRequest {
    private Long driverId;
    private Long packageId;
    private String signature; // Base64 encoded signature
    private MultipartFile signaturePhoto;
    private MultipartFile packagePhoto;
    private String pickupNotes;
    private Double pickupLat;
    private Double pickupLng;
    private String pickupAddress;

    // Constructors
    public PackagePickupRequest() {}

    public PackagePickupRequest(Long driverId, Long packageId) {
        this.driverId = driverId;
        this.packageId = packageId;
    }

    // Getters and Setters
    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public MultipartFile getSignaturePhoto() {
        return signaturePhoto;
    }

    public void setSignaturePhoto(MultipartFile signaturePhoto) {
        this.signaturePhoto = signaturePhoto;
    }

    public MultipartFile getPackagePhoto() {
        return packagePhoto;
    }

    public void setPackagePhoto(MultipartFile packagePhoto) {
        this.packagePhoto = packagePhoto;
    }

    public String getPickupNotes() {
        return pickupNotes;
    }

    public void setPickupNotes(String pickupNotes) {
        this.pickupNotes = pickupNotes;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public void setPickupLat(Double pickupLat) {
        this.pickupLat = pickupLat;
    }

    public Double getPickupLng() {
        return pickupLng;
    }

    public void setPickupLng(Double pickupLng) {
        this.pickupLng = pickupLng;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }
}

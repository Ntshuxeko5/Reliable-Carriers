package com.reliablecarriers.Reliable.Carriers.dto;

import org.springframework.web.multipart.MultipartFile;

public class PackageDeliveryRequest {
    private Long driverId;
    private Long packageId;
    private String signature; // Base64 encoded signature
    private MultipartFile signaturePhoto;
    private MultipartFile deliveryPhoto;
    private String recipientName;
    private String recipientPhone;
    private String recipientIdNumber;
    private String deliveryNotes;
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
    private String deliveryMethod; // HAND_TO_RECIPIENT, LEAVE_AT_DOOR, etc.

    // Constructors
    public PackageDeliveryRequest() {}

    public PackageDeliveryRequest(Long driverId, Long packageId) {
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

    public MultipartFile getDeliveryPhoto() {
        return deliveryPhoto;
    }

    public void setDeliveryPhoto(MultipartFile deliveryPhoto) {
        this.deliveryPhoto = deliveryPhoto;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientIdNumber() {
        return recipientIdNumber;
    }

    public void setRecipientIdNumber(String recipientIdNumber) {
        this.recipientIdNumber = recipientIdNumber;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    public Double getDeliveryLat() {
        return deliveryLat;
    }

    public void setDeliveryLat(Double deliveryLat) {
        this.deliveryLat = deliveryLat;
    }

    public Double getDeliveryLng() {
        return deliveryLng;
    }

    public void setDeliveryLng(Double deliveryLng) {
        this.deliveryLng = deliveryLng;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }
}

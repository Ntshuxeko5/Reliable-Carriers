package com.reliablecarriers.Reliable.Carriers.service;

/**
 * SMS Service Interface
 * Simplified interface matching existing usage patterns
 */
public interface SmsService {
    
    // Basic SMS operations
    void sendSms(String phoneNumber, String message);
    
    // Existing method signatures from SmsServiceImpl
    void sendShipmentUpdate(String phoneNumber, String trackingNumber, String status);
    void sendDeliveryNotification(String phoneNumber, String trackingNumber, String estimatedTime);
    void sendDriverAssignment(String phoneNumber, String trackingNumber, String pickupLocation);
    void sendPaymentReminder(String phoneNumber, String bookingNumber, String amount);
    void sendBulkSms(String[] phoneNumbers, String message);
    void sendCustomSms(String phoneNumber, String message);
    void sendOtp(String phoneNumber, String otp);
    void sendEmergencyNotification(String phoneNumber, String message);
}
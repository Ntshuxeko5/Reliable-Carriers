package com.reliablecarriers.Reliable.Carriers.service;

public interface SmsService {
    
    /**
     * Send a basic SMS message
     */
    void sendSms(String to, String message);
    
    /**
     * Send shipment status update SMS
     */
    void sendShipmentUpdate(String to, String trackingNumber, String status);
    
    /**
     * Send delivery notification SMS
     */
    void sendDeliveryNotification(String to, String trackingNumber, String estimatedTime);
    
    /**
     * Send driver assignment SMS
     */
    void sendDriverAssignment(String to, String trackingNumber, String pickupAddress);
    
    /**
     * Send payment reminder SMS
     */
    void sendPaymentReminder(String to, String trackingNumber, String amount);
    
    /**
     * Send bulk SMS to multiple recipients
     */
    void sendBulkSms(String[] recipients, String message);
    
    /**
     * Send custom SMS message
     */
    void sendCustomSms(String to, String message);
    
    /**
     * Send OTP verification SMS
     */
    void sendOtp(String to, String otp);
    
    /**
     * Send emergency notification SMS
     */
    void sendEmergencyNotification(String to, String message);
}

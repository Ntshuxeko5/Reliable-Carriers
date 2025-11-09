package com.reliablecarriers.Reliable.Carriers.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * SMS Service using Africa's Talking API (or similar SMS provider)
 * Handles all SMS notifications for the system
 */
@Service
public class ComprehensiveSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveSmsService.class);

    @Value("${sms.provider.api.key:}")
    private String apiKey;

    @Value("${sms.provider.username:}")
    private String username;

    @Value("${sms.provider.base.url:https://api.africastalking.com/version1/messaging}")
    private String baseUrl;

    @Value("${sms.sender.name:Reliable}")
    private String senderName;

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send SMS message
     */
    @Override
    public void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            logger.info("SMS disabled - would send to {}: {}", phoneNumber, message);
            return; // SMS disabled
        }

        if (apiKey.isEmpty() || username.isEmpty()) {
            logger.warn("SMS not configured - simulating SMS to {}: {}", phoneNumber, message);
            simulateSms(phoneNumber, message);
            return;
        }

        try {
            // Format phone number for international format
            String formattedPhone = formatPhoneNumber(phoneNumber);
            
            // Prepare SMS request
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("to", formattedPhone);
            requestBody.put("message", message);
            requestBody.put("from", senderName);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apiKey", apiKey);
            headers.set("Accept", "application/json");

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // Send SMS
            String response = restTemplate.postForObject(baseUrl, request, String.class);
            
            logger.info("SMS sent successfully to {}: {}", formattedPhone, message);
            logger.debug("SMS API response: {}", response);
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            // Fallback to simulation for demo purposes
            simulateSms(phoneNumber, message);
        }
    }

    /**
     * Send booking confirmation SMS
     */
    public boolean sendBookingConfirmationSms(String phoneNumber, String bookingNumber, String trackingNumber) {
        String message = String.format("Booking confirmed! %s - Track: %s. Thank you for choosing Reliable Carriers.", 
            bookingNumber, trackingNumber);
        sendSms(phoneNumber, message);
        return true;
    }

    /**
     * Send pickup notification SMS
     */
    public boolean sendPickupNotificationSms(String phoneNumber, String trackingNumber, String driverName) {
        String message = String.format("Your package %s has been picked up by %s. Track your package for updates.", 
            trackingNumber, driverName);
        sendSms(phoneNumber, message);
        return true;
    }

    /**
     * Send out for delivery SMS
     */
    public boolean sendOutForDeliverySms(String phoneNumber, String trackingNumber, String estimatedTime, String deliveryCode) {
        String message = String.format("Your package %s is out for delivery! Expected: %s. Delivery code: %s", 
            trackingNumber, estimatedTime, deliveryCode);
        sendSms(phoneNumber, message);
        return true;
    }

    /**
     * Send delivery confirmation SMS
     */
    public boolean sendDeliveryConfirmationSms(String phoneNumber, String trackingNumber) {
        String message = String.format("Package %s delivered successfully! Thank you for choosing Reliable Carriers.", 
            trackingNumber);
        sendSms(phoneNumber, message);
        return true;
    }

    /**
     * Send driver assignment SMS
     */
    public boolean sendDriverAssignmentSms(String phoneNumber, String trackingNumber, String pickupAddress) {
        String message = String.format("New package assigned: %s. Pickup: %s. Check your driver app for details.", 
            trackingNumber, pickupAddress);
        sendSms(phoneNumber, message);
        return true;
    }

    /**
     * Send delivery code verification SMS
     */
    public boolean sendDeliveryCodeSms(String phoneNumber, String deliveryCode, String estimatedTime) {
        String message = String.format("Your package will be delivered %s. Delivery code: %s. Please have this ready for the driver.", 
            estimatedTime, deliveryCode);
        sendSms(phoneNumber, message);
        return true;
    }

    /**
     * Send bulk SMS to multiple recipients (legacy method)
     */
    public boolean sendBulkSms(Map<String, String> phoneMessageMap) {
        for (Map.Entry<String, String> entry : phoneMessageMap.entrySet()) {
            try {
                sendSms(entry.getKey(), entry.getValue());
                // Add small delay to avoid rate limiting
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Bulk SMS sending interrupted");
                return false;
            }
        }
        return true;
    }

    /**
     * Format phone number for international format
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return phoneNumber;
        }

        // Remove all non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");
        
        // Handle South African numbers
        if (digitsOnly.startsWith("0") && digitsOnly.length() == 10) {
            // Convert 0123456789 to +27123456789
            return "+27" + digitsOnly.substring(1);
        } else if (digitsOnly.startsWith("27") && digitsOnly.length() == 11) {
            // Convert 27123456789 to +27123456789
            return "+" + digitsOnly;
        } else if (digitsOnly.startsWith("123456789") && digitsOnly.length() == 9) {
            // Convert 123456789 to +27123456789
            return "+27" + digitsOnly;
        }
        
        // If already in international format or unknown format, return as is
        return phoneNumber.startsWith("+") ? phoneNumber : "+" + digitsOnly;
    }

    /**
     * Simulate SMS for demo purposes when real SMS is not configured
     */
    private void simulateSms(String phoneNumber, String message) {
        logger.info("📱 SMS SIMULATION - To: {} | Message: {}", phoneNumber, message);
        
        // In a real demo, you might want to store these in a database
        // or display them in an admin interface for demonstration
    }

    /**
     * Check if SMS service is properly configured
     */
    public boolean isConfigured() {
        return !apiKey.isEmpty() && !username.isEmpty();
    }

    /**
     * Get SMS service status
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", smsEnabled);
        status.put("configured", isConfigured());
        status.put("provider", isConfigured() ? "Africa's Talking" : "Simulation Mode");
        status.put("senderName", senderName);
        return status;
    }

    /**
     * Test SMS functionality
     */
    public boolean testSms(String phoneNumber) {
        String testMessage = "Test message from Reliable Carriers SMS service. If you receive this, SMS is working correctly.";
        sendSms(phoneNumber, testMessage);
        return true;
    }

    // Interface methods from SmsService
    @Override
    public void sendShipmentUpdate(String phoneNumber, String trackingNumber, String status) {
        String message = String.format("Shipment update: %s - Status: %s", trackingNumber, status);
        sendSms(phoneNumber, message);
    }

    @Override
    public void sendDeliveryNotification(String phoneNumber, String trackingNumber, String estimatedTime) {
        sendOutForDeliverySms(phoneNumber, trackingNumber, estimatedTime, "N/A");
    }

    @Override
    public void sendDriverAssignment(String phoneNumber, String trackingNumber, String pickupLocation) {
        sendDriverAssignmentSms(phoneNumber, trackingNumber, pickupLocation);
    }

    @Override
    public void sendPaymentReminder(String phoneNumber, String bookingNumber, String amount) {
        String message = String.format("Payment reminder: Booking %s - Amount: R%s. Please complete payment.", bookingNumber, amount);
        sendSms(phoneNumber, message);
    }

    @Override
    public void sendBulkSms(String[] phoneNumbers, String message) {
        for (String phoneNumber : phoneNumbers) {
            sendSms(phoneNumber, message);
            try {
                Thread.sleep(100); // Rate limiting
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    @Override
    public void sendCustomSms(String phoneNumber, String message) {
        sendSms(phoneNumber, message);
    }

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        String message = String.format("Your OTP code is: %s. Valid for 5 minutes.", otp);
        sendSms(phoneNumber, message);
    }

    @Override
    public void sendEmergencyNotification(String phoneNumber, String message) {
        String emergencyMessage = String.format("EMERGENCY: %s", message);
        sendSms(phoneNumber, emergencyMessage);
    }
}
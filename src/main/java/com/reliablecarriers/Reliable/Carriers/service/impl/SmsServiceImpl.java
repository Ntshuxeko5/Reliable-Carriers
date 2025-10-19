package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.provider:smsportal}")
    private String smsProvider;

    @Value("${app.notifications.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${sms.api.key:}")
    private String smsApiKey;

    @Value("${sms.api.secret:}")
    private String smsApiSecret;

    @Value("${sms.api.url:https://api.smsportal.com/v1}")
    private String smsApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendSms(String to, String message) {
        if (!smsEnabled) {
            // Log the SMS for development/testing
            logSmsToConsole(to, message, "SMS service disabled");
            return;
        }

        // Always log SMS content prominently for development
        logSmsToConsole(to, message, "SMS service enabled - attempting delivery");

        // In production, integrate with actual SMS provider (Twilio, AWS SNS, etc.)
        CompletableFuture.runAsync(() -> {
            try {
                switch (smsProvider.toLowerCase()) {
                    case "smsportal":
                        sendViaSmsPortal(to, message);
                        break;
                    case "twilio":
                        sendViaTwilio(to, message);
                        break;
                    case "aws":
                        sendViaAwsSns(to, message);
                        break;
                    default:
                        sendViaGenericProvider(to, message);
                }
            } catch (Exception e) {
                System.err.println("Failed to send SMS: " + e.getMessage());
                logSmsToConsole(to, message, "SMS delivery failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void sendShipmentUpdate(String to, String trackingNumber, String status) {
        String message = String.format(
            "Your shipment %s status: %s. Track at reliablecarriers.com/tracking/%s",
            trackingNumber, status, trackingNumber
        );
        sendSms(to, message);
    }

    @Override
    public void sendDeliveryNotification(String to, String trackingNumber, String estimatedTime) {
        String message = String.format(
            "Your package %s will be delivered around %s. Please ensure someone is available.",
            trackingNumber, estimatedTime
        );
        sendSms(to, message);
    }

    @Override
    public void sendDriverAssignment(String to, String trackingNumber, String pickupAddress) {
        String message = String.format(
            "New delivery assigned: %s. Pickup at: %s. Check your workboard for details.",
            trackingNumber, pickupAddress
        );
        sendSms(to, message);
    }

    @Override
    public void sendPaymentReminder(String to, String trackingNumber, String amount) {
        String message = String.format(
            "Payment reminder: %s for shipment %s. Pay at reliablecarriers.com/payment",
            amount, trackingNumber
        );
        sendSms(to, message);
    }

    @Override
    public void sendBulkSms(String[] recipients, String message) {
        for (String recipient : recipients) {
            sendSms(recipient, message);
        }
    }

    @Override
    public void sendCustomSms(String to, String message) {
        sendSms(to, message);
    }

    @Override
    public void sendOtp(String to, String otp) {
        String message = String.format(
            "Your verification code is: %s. Valid for 10 minutes. Do not share this code.",
            otp
        );
        sendSms(to, message);
    }

    @Override
    public void sendEmergencyNotification(String to, String message) {
        // High priority SMS for emergencies
        String emergencyMessage = "URGENT: " + message;
        sendSms(to, emergencyMessage);
    }

    // Private methods for different SMS providers
    private void sendViaSmsPortal(String to, String message) {
        try {
            System.out.println("=== SMS DEBUG START ===");
            System.out.println("API Key: " + smsApiKey);
            System.out.println("API Secret: " + (smsApiSecret != null ? "***" + smsApiSecret.substring(smsApiSecret.length()-4) : "null"));
            System.out.println("API URL: " + smsApiUrl);
            System.out.println("To: " + to);
            System.out.println("Message: " + message);
            
            // Prepare authentication header
            String auth = smsApiKey + ":" + smsApiSecret;
            String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedAuth);
            
            // Prepare SMS payload according to SMSPortal API v1
            Map<String, Object> smsData = Map.of(
                "destination", to,
                "content", message,
                "campaign", "Reliable Carriers"
            );
            
            Map<String, Object> payload = Map.of(
                "messages", java.util.List.of(smsData)
            );
            
            System.out.println("Payload: " + payload);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            String apiEndpoint = smsApiUrl + "/messages";
            System.out.println("API Endpoint: " + apiEndpoint);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                apiEndpoint, 
                request, 
                String.class
            );
            
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("SMS sent successfully via SMSPortal to " + to);
            } else {
                System.err.println("SMS failed via SMSPortal: " + response.getBody());
                // Enhanced fallback logging
                logSmsToConsole(to, message, "SMSPortal API failed");
            }
            System.out.println("=== SMS DEBUG END ===");
        } catch (Exception e) {
            System.err.println("Error sending SMS via SMSPortal: " + e.getMessage());
            e.printStackTrace();
            // Enhanced fallback logging
            logSmsToConsole(to, message, "SMSPortal API error: " + e.getMessage());
        }
    }
    
    private void logSmsToConsole(String to, String message, String reason) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📱 SMS NOTIFICATION (DEVELOPMENT MODE)");
        System.out.println("=".repeat(80));
        System.out.println("📞 TO: " + to);
        System.out.println("💬 MESSAGE: " + message);
        System.out.println("⚠️  REASON: " + reason);
        System.out.println("🕐 TIME: " + java.time.LocalDateTime.now());
        System.out.println("=".repeat(80));
        System.out.println("NOTE: In production, this would be sent via SMS provider");
        System.out.println("=".repeat(80) + "\n");
    }

    private void sendViaTwilio(String to, String message) {
        // Twilio integration would go here
        System.out.println("SMS via Twilio: To: " + to + ", Message: " + message);
    }

    private void sendViaAwsSns(String to, String message) {
        // AWS SNS integration would go here
        System.out.println("SMS via AWS SNS: To: " + to + ", Message: " + message);
    }

    private void sendViaGenericProvider(String to, String message) {
        // Generic SMS provider integration
        System.out.println("SMS via Generic Provider: To: " + to + ", Message: " + message);
    }
}

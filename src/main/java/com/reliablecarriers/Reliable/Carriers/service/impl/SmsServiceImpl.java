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
import org.springframework.context.annotation.Primary;

@Service
@Primary
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
            
            // Try Method 1: Bearer Token Authentication
            String authToken = getSmsPortalAuthToken();
            if (authToken != null) {
                try {
                    sendSmsWithBearerToken(to, message, authToken);
                    return;
                } catch (Exception e) {
                    System.err.println("Bearer token method failed: " + e.getMessage());
                    // Continue to Basic Auth fallback
                }
            }
            
            // Try Method 2: Basic Authentication (fallback with comprehensive testing)
            System.out.println("Bearer token failed, trying Basic auth with comprehensive testing...");
            sendSmsWithBasicAuth(to, message);
            
        } catch (Exception e) {
            System.err.println("Error sending SMS via SMSPortal: " + e.getMessage());
            e.printStackTrace();
            // Enhanced fallback logging
            logSmsToConsole(to, message, "SMSPortal API error: " + e.getMessage());
        }
    }
    
    private void sendSmsWithBearerToken(String to, String message, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            
            // Prepare SMS payload according to SMSPortal API v1
            Map<String, Object> smsData = Map.of(
                "destination", to,
                "content", message,
                "campaign", "Reliable Carriers"
            );
            
            Map<String, Object> payload = Map.of(
                "messages", java.util.List.of(smsData)
            );
            
            System.out.println("Bearer Token Payload: " + payload);
            
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
                logSmsToConsole(to, message, "SMSPortal API failed");
            }
        } catch (Exception e) {
            System.err.println("Error sending SMS with Bearer token: " + e.getMessage());
            throw e;
        }
    }
    
    private void sendSmsWithBasicAuth(String to, String message) {
        try {
            System.out.println("=== SMSPORTAL AUTHENTICATION DEBUG ===");
            System.out.println("API Key: " + smsApiKey);
            System.out.println("API Secret: " + (smsApiSecret != null ? "***" + smsApiSecret.substring(smsApiSecret.length()-4) : "null"));
            
            // SMSPortal uses Basic Authentication with ClientID:Secret format
            String credentials = smsApiKey + ":" + smsApiSecret;
            String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedCredentials);
            
            System.out.println("Using Basic Auth with credentials: " + encodedCredentials.substring(0, Math.min(20, encodedCredentials.length())) + "...");
            
            // SMSPortal API format - try different endpoints and payloads
            String[] endpoints = {
                "https://rest.smsportal.com/v1/bulkmessages",
                "https://rest.smsportal.com/v1/messages", 
                "https://rest.smsportal.com/bulkmessages",
                "https://rest.smsportal.com/messages"
            };
            
            // Try different payload formats for SMSPortal
            Map<String, Object> payload1 = Map.of(
                "destination", to,
                "content", message
            );
            
            Map<String, Object> payload2 = Map.of(
                "messages", java.util.List.of(Map.of(
                    "destination", to,
                    "content", message
                ))
            );
            
            Map<String, Object> payload3 = Map.of(
                "to", to,
                "message", message
            );
            
            // SMSPortal specific format based on documentation
            Map<String, Object> payload4 = Map.of(
                "messages", java.util.List.of(Map.of(
                    "destination", to,
                    "content", message,
                    "campaign", "Reliable Carriers"
                ))
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object>[] payloads = new Map[]{payload1, payload2, payload3, payload4};
            
            // Try each endpoint with each payload
            for (String endpoint : endpoints) {
                for (Map<String, Object> payload : payloads) {
                    try {
                        System.out.println("Trying endpoint: " + endpoint);
                        System.out.println("Trying payload format: " + payload);
                        
                        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                        
                        ResponseEntity<String> response = restTemplate.postForEntity(
                            endpoint, 
                            request, 
                            String.class
                        );
                        
                        System.out.println("Response Status: " + response.getStatusCode());
                        System.out.println("Response Body: " + response.getBody());
                        
                        if (response.getStatusCode().is2xxSuccessful()) {
                            System.out.println("SMS sent successfully via SMSPortal to " + to);
                            return; // Success, exit the method
                        } else if (response.getStatusCode().value() == 401) {
                            System.err.println("Authentication failed with endpoint " + endpoint + " and payload: " + payload);
                            // Try next combination
                            continue;
                        } else if (response.getStatusCode().value() == 404) {
                            System.err.println("Endpoint not found: " + endpoint);
                            // Try next endpoint
                            break;
                        } else {
                            System.err.println("SMS failed via SMSPortal: " + response.getBody());
                            logSmsToConsole(to, message, "SMSPortal API failed: " + response.getBody());
                            return;
                        }
                    } catch (Exception e) {
                        System.err.println("Error with endpoint " + endpoint + " and payload " + payload + ": " + e.getMessage());
                        // Continue to next combination
                    }
                }
            }
            
            // If all combinations failed
            System.err.println("All SMSPortal endpoint and payload combinations failed");
            logSmsToConsole(to, message, "All SMSPortal combinations failed - check API credentials and format");
            
        } catch (Exception e) {
            System.err.println("Error sending SMS with Basic auth: " + e.getMessage());
            throw e;
        }
    }
    
    private String getSmsPortalAuthToken() {
        try {
            System.out.println("=== SMSPORTAL AUTH DEBUG ===");
            System.out.println("Client ID: " + smsApiKey);
            System.out.println("Secret: " + (smsApiSecret != null ? "***" + smsApiSecret.substring(smsApiSecret.length()-4) : "null"));
            
            // SMSPortal uses Basic Authentication with Base64 encoded credentials
            String credentials = smsApiKey + ":" + smsApiSecret;
            String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodedCredentials);
            
            // SMSPortal doesn't require a separate auth endpoint - we use the credentials directly
            System.out.println("Using Basic Auth with encoded credentials");
            return "BASIC_AUTH"; // Return a marker to indicate we're using Basic Auth
            
        } catch (Exception e) {
            System.err.println("Error preparing SMSPortal auth: " + e.getMessage());
            e.printStackTrace();
            return null;
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

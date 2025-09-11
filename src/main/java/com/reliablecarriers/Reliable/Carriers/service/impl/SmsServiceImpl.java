package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class SmsServiceImpl implements SmsService {

    @Value("${sms.provider:twilio}")
    private String smsProvider;

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Override
    public void sendSms(String to, String message) {
        if (!smsEnabled) {
            // Log the SMS for development/testing
            System.out.println("SMS (DISABLED): To: " + to + ", Message: " + message);
            return;
        }

        // In production, integrate with actual SMS provider (Twilio, AWS SNS, etc.)
        CompletableFuture.runAsync(() -> {
            try {
                switch (smsProvider.toLowerCase()) {
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

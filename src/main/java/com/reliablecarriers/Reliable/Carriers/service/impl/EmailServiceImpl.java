package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Reliable Carriers}")
    private String appName;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("Email sent successfully to " + to + " with subject: " + subject);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            System.err.println("Email error details: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("Email error cause: " + e.getCause().getMessage());
            }
            
            // For development/testing, log the email content instead of throwing exception
            System.out.println("=== EMAIL CONTENT (DEVELOPMENT MODE) ===");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Message: " + text);
            System.out.println("=== END EMAIL CONTENT ===");
            
            // In production, you might want to throw the exception
            // throw new RuntimeException("Email sending failed", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendShipmentConfirmation(String to, String customerName, String trackingNumber, String estimatedDelivery) {
        Map<String, Object> variables = Map.of(
            "customerName", customerName,
            "trackingNumber", trackingNumber,
            "estimatedDelivery", estimatedDelivery
        );
        sendHtmlEmail(to, "Shipment Confirmation - " + trackingNumber, "email/shipment-confirmation", variables);
    }

    @Override
    public void sendDeliveryUpdate(String to, String customerName, String trackingNumber, String status, String location) {
        Map<String, Object> variables = Map.of(
            "customerName", customerName,
            "trackingNumber", trackingNumber,
            "status", status,
            "location", location
        );
        sendHtmlEmail(to, "Delivery Update - " + trackingNumber, "email/delivery-update", variables);
    }

    @Override
    public void sendDeliveryConfirmation(String to, String customerName, String trackingNumber, String deliveryDate) {
        Map<String, Object> variables = Map.of(
            "customerName", customerName,
            "trackingNumber", trackingNumber,
            "deliveryDate", deliveryDate
        );
        sendHtmlEmail(to, "Delivery Confirmation - " + trackingNumber, "email/delivery-confirmation", variables);
    }

    @Override
    public void sendDriverAssignment(String to, String driverName, String trackingNumber, String pickupAddress, String deliveryAddress) {
        Map<String, Object> variables = Map.of(
            "driverName", driverName,
            "trackingNumber", trackingNumber,
            "pickupAddress", pickupAddress,
            "deliveryAddress", deliveryAddress
        );
        sendHtmlEmail(to, "New Delivery Assignment - " + trackingNumber, "email/driver-assignment", variables);
    }

    @Override
    public void sendPaymentConfirmation(String to, String customerName, String trackingNumber, String amount, String paymentMethod) {
        Map<String, Object> variables = Map.of(
            "customerName", customerName,
            "trackingNumber", trackingNumber,
            "amount", amount,
            "paymentMethod", paymentMethod
        );
        sendHtmlEmail(to, "Payment Confirmation - " + trackingNumber, "email/payment-confirmation", variables);
    }

    @Override
    public void sendPasswordReset(String to, String resetToken) {
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;
        
        Map<String, Object> variables = Map.of(
            "resetToken", resetToken,
            "resetUrl", resetUrl,
            "appName", "Reliable Carriers"
        );
        sendHtmlEmail(to, "Password Reset Request", "email/password-reset", variables);
    }

    @Override
    public void sendWelcomeEmail(String to, String customerName) {
        Map<String, Object> variables = Map.of(
            "customerName", customerName
        );
        sendHtmlEmail(to, "Welcome to " + appName, "email/welcome", variables);
    }

    @Override
    public void sendAdminNotification(String subject, String message) {
        // Send to admin email
        sendSimpleEmail("admin@reliablecarriers.com", subject, message);
    }

    @Override
    public void sendBulkEmail(String[] recipients, String subject, String templateName, Map<String, Object> variables) {
        for (String recipient : recipients) {
            sendHtmlEmail(recipient, subject, templateName, variables);
        }
    }

    @Override
    public void sendBookingConfirmationEmail(String to, String customerName, String bookingNumber, String trackingNumber, 
                                           String serviceType, String totalAmount, String estimatedDelivery,
                                           String pickupAddress, String deliveryAddress, String weight, String description,
                                           String customerPickupCode, String customerDeliveryCode, String pickupContactName,
                                           String pickupContactPhone, String deliveryContactName, String deliveryContactPhone,
                                           String dimensions, String specialInstructions) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("bookingNumber", bookingNumber);
        variables.put("trackingNumber", trackingNumber);
        variables.put("serviceType", serviceType);
        variables.put("totalAmount", totalAmount);
        variables.put("estimatedDelivery", estimatedDelivery);
        variables.put("pickupAddress", pickupAddress);
        variables.put("deliveryAddress", deliveryAddress);
        variables.put("weight", weight);
        variables.put("description", description);
        variables.put("customerPickupCode", customerPickupCode);
        variables.put("customerDeliveryCode", customerDeliveryCode);
        variables.put("pickupContactName", pickupContactName);
        variables.put("pickupContactPhone", pickupContactPhone);
        variables.put("deliveryContactName", deliveryContactName);
        variables.put("deliveryContactPhone", deliveryContactPhone);
        variables.put("dimensions", dimensions);
        variables.put("specialInstructions", specialInstructions);
        variables.put("customerEmail", to);
        
        sendHtmlEmail(to, "Booking Confirmation - " + bookingNumber, "email/booking-confirmation", variables);
    }

    @Override
    public void sendDriverVerificationStatus(String to, String driverName, String documentType, boolean approved, String notes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("driverName", driverName);
        variables.put("documentType", documentType);
        variables.put("approved", approved);
        variables.put("notes", notes != null ? notes : "");
        variables.put("status", approved ? "approved" : "rejected");
        
        String subject = approved 
            ? "Document Verified - " + documentType
            : "Document Verification Rejected - " + documentType;
        
        sendHtmlEmail(to, subject, "email/verification-status", variables);
    }

    @Override
    public void sendBusinessDocumentVerificationStatus(String to, String businessName, String documentType, boolean approved, String notes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("businessName", businessName);
        variables.put("documentType", documentType);
        variables.put("approved", approved);
        variables.put("notes", notes != null ? notes : "");
        variables.put("status", approved ? "approved" : "rejected");
        
        String subject = approved 
            ? "Document Verified - " + documentType
            : "Document Verification Rejected - " + documentType;
        
        sendHtmlEmail(to, subject, "email/verification-status", variables);
    }

    @Override
    public void sendBusinessAccountVerificationStatus(String to, String businessName, boolean approved, String notes, String creditLimit, String paymentTerms) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("businessName", businessName);
        variables.put("approved", approved);
        variables.put("notes", notes != null ? notes : "");
        variables.put("creditLimit", creditLimit != null ? creditLimit : "N/A");
        variables.put("paymentTerms", paymentTerms != null ? paymentTerms + " days" : "N/A");
        variables.put("status", approved ? "approved" : "rejected");
        
        String subject = approved 
            ? "Business Account Approved - " + businessName
            : "Business Account Rejected - " + businessName;
        
        sendHtmlEmail(to, subject, "email/business-verification-status", variables);
    }

    @Override
    public void sendDocumentExpiryWarning(String to, String recipientName, String documentType, String expiryDate) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recipientName", recipientName);
        variables.put("documentType", documentType);
        variables.put("expiryDate", expiryDate);
        
        sendHtmlEmail(to, "Document Expiring Soon - " + documentType, "email/document-expiry-warning", variables);
    }
}

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
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Reliable Carriers}")
    private String appName;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
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
        Map<String, Object> variables = Map.of(
            "resetToken", resetToken,
            "resetUrl", "https://reliablecarriers.com/reset-password?token=" + resetToken
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
}

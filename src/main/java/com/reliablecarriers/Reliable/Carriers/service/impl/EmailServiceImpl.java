package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

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

    @Value("${app.debug.mode:false}")
    private boolean debugMode;
    
    // Mailgun configuration (optional - falls back to SMTP if not configured)
    @Value("${mailgun.api.key:}")
    private String mailgunApiKey;
    
    @Value("${mailgun.domain:}")
    private String mailgunDomain;
    
    @Value("${mailgun.from.email:}")
    private String mailgunFromEmail;
    
    @Value("${mailgun.enabled:false}")
    private boolean mailgunEnabled;
    
    private WebClient mailgunWebClient;
    private String mailgunFromAddress;
    
    /**
     * Initialize Mailgun configuration if enabled and credentials are provided
     */
    @PostConstruct
    public void initMailgun() {
        if (mailgunEnabled && mailgunApiKey != null && !mailgunApiKey.isEmpty() 
            && mailgunDomain != null && !mailgunDomain.isEmpty()) {
            try {
                // Create WebClient for Mailgun API
                mailgunWebClient = WebClient.builder()
                    .baseUrl("https://api.mailgun.net/v3/" + mailgunDomain)
                    .defaultHeader("Authorization", "Basic " + java.util.Base64.getEncoder()
                        .encodeToString(("api:" + mailgunApiKey).getBytes()))
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
                
                // Set from address
                mailgunFromAddress = (mailgunFromEmail != null && !mailgunFromEmail.isEmpty()) 
                    ? mailgunFromEmail 
                    : "noreply@" + mailgunDomain;
                
                logger.info("Mailgun email service initialized successfully for domain: {}", mailgunDomain);
            } catch (Exception e) {
                logger.error("Failed to initialize Mailgun: {}", e.getMessage(), e);
                mailgunEnabled = false;
            }
        } else {
            logger.info("Mailgun not configured, using SMTP fallback");
        }
    }

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        // Try Mailgun first if enabled
        if (mailgunEnabled && mailgunWebClient != null) {
            try {
                sendViaMailgun(to, subject, text, null);
                logger.info("Email sent successfully via Mailgun to {} with subject: {}", to, subject);
                return;
            } catch (Exception e) {
                logger.warn("Mailgun email failed, falling back to SMTP: {}", e.getMessage());
                // Fall through to SMTP fallback
            }
        }
        
        // Fallback to SMTP
        int maxRetries = 3;
        int retryDelay = 2000; // 2 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Validate email configuration
                if (fromEmail == null || fromEmail.isEmpty()) {
                    throw new IllegalStateException("Email sender (spring.mail.username) is not configured. Please set GMAIL_USERNAME environment variable.");
                }
                
                if (mailSender == null) {
                    throw new IllegalStateException("JavaMailSender is not configured. Please check email configuration.");
                }
                
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
                logger.info("Email sent successfully via SMTP to {} with subject: {}", to, subject);
                return; // Success, exit retry loop
            } catch (Exception e) {
                logger.warn("Failed to send email to {} (Attempt {}/{}): {}", to, attempt, maxRetries, e.getMessage());
                if (e.getCause() != null) {
                    logger.debug("Email error cause: {}", e.getCause().getMessage());
                }
                
                // If this is the last attempt, log full details and throw exception
                if (attempt == maxRetries) {
                    logger.error("Email sending failed after {} attempts to {}: {}", maxRetries, to, e.getMessage(), e);
                    
                    // For development/testing, log the email content if debug mode is enabled
                    if (debugMode) {
                        logger.debug("Email content (DEBUG MODE) - To: {}, Subject: {}, From: {}", to, subject, fromEmail);
                    }
                    
                    // Check if it's a connection timeout issue
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && (errorMessage.contains("timeout") || errorMessage.contains("Connect timed out") || errorMessage.contains("Couldn't connect"))) {
                        throw new RuntimeException("Email sending failed: Unable to connect to SMTP server. Please check:\n" +
                            "1. Network connectivity and firewall settings\n" +
                            "2. Gmail App Password is correct\n" +
                            "3. SMTP port 587 is not blocked\n" +
                            "4. Try using port 465 with SSL instead\n" +
                            "Original error: " + e.getMessage(), e);
                    }
                    
                    // Re-throw exception so caller knows email failed
                    throw new RuntimeException("Email sending failed after " + maxRetries + " attempts: " + e.getMessage(), e);
                } else {
                    // Wait before retrying (exponential backoff)
                    try {
                        Thread.sleep(retryDelay * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Email sending interrupted during retry", ie);
                    }
                }
            }
        }
    }
    
    /**
     * Send email via Mailgun REST API
     */
    private void sendViaMailgun(String to, String subject, String text, String html) {
        if (mailgunWebClient == null) {
            throw new IllegalStateException("Mailgun is not configured");
        }
        
        // Build form data for Mailgun API
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("from", mailgunFromAddress);
        formData.add("to", to);
        formData.add("subject", subject);
        
        if (html != null && !html.isEmpty()) {
            formData.add("html", html);
            if (text != null && !text.isEmpty()) {
                formData.add("text", text);
            }
        } else {
            formData.add("text", text != null ? text : "");
        }
        
        // Send request to Mailgun API
        try {
            String response = mailgunWebClient.post()
                .uri("/messages")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block(); // Block for synchronous operation
            
            logger.debug("Mailgun API response: {}", response);
        } catch (Exception e) {
            logger.error("Mailgun API error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email via Mailgun: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process(templateName, context);
            
            // Try Mailgun first if enabled
            if (mailgunEnabled && mailgunWebClient != null) {
                try {
                    sendViaMailgun(to, subject, null, htmlContent);
                    logger.info("HTML email sent successfully via Mailgun to {} with subject: {}", to, subject);
                    return;
                } catch (Exception e) {
                    logger.warn("Mailgun HTML email failed, falling back to SMTP: {}", e.getMessage());
                    // Fall through to SMTP fallback
                }
            }
            
            // Fallback to SMTP
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("HTML email sent successfully via SMTP to {} with subject: {}", to, subject);
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
    
    @Override
    public void sendQuoteSavedEmail(String to, String customerName, String quoteId, String serviceType, 
                                   String totalCost, String pickupAddress, String deliveryAddress, 
                                   String estimatedDelivery, String expiryDate) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("customerName", customerName);
        variables.put("quoteId", quoteId);
        variables.put("serviceType", serviceType);
        variables.put("totalCost", totalCost);
        variables.put("pickupAddress", pickupAddress);
        variables.put("deliveryAddress", deliveryAddress);
        variables.put("estimatedDelivery", estimatedDelivery);
        variables.put("expiryDate", expiryDate);
        
        sendHtmlEmail(to, "Quote Saved - " + quoteId, "email/quote-saved", variables);
    }
}

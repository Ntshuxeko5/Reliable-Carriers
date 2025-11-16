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
import reactor.core.publisher.Mono;

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
    
    @Value("${mailgun.region:us}")
    private String mailgunRegion;
    
    @Value("${mailgun.enabled:false}")
    private boolean mailgunEnabled;
    
    private WebClient mailgunWebClient;
    private String mailgunFromAddress;
    private int mailgunAuthFailureCount = 0;
    private static final int MAX_MAILGUN_AUTH_FAILURES = 3;
    
    /**
     * Initialize Mailgun configuration if enabled and credentials are provided
     */
    @PostConstruct
    public void initMailgun() {
        if (mailgunEnabled) {
            // Validate configuration
            if (mailgunApiKey == null || mailgunApiKey.isEmpty()) {
                logger.warn("Mailgun is enabled but API key is missing. Set MAILGUN_API_KEY environment variable. Falling back to SMTP.");
                mailgunEnabled = false;
                return;
            }
            
            if (mailgunDomain == null || mailgunDomain.isEmpty()) {
                logger.warn("Mailgun is enabled but domain is missing. Set MAILGUN_DOMAIN environment variable. Falling back to SMTP.");
                mailgunEnabled = false;
                return;
            }
            
            // Trim API key to remove any whitespace
            mailgunApiKey = mailgunApiKey.trim();
            mailgunDomain = mailgunDomain.trim();
            
            // Validate API key format (should not contain spaces and should be reasonable length)
            if (mailgunApiKey.length() < 20) {
                logger.warn("Mailgun API key appears to be invalid (too short: {} chars). Please check your MAILGUN_API_KEY. Falling back to SMTP.", 
                    mailgunApiKey.length());
                mailgunEnabled = false;
                return;
            }
            
            // Log configuration (without exposing full API key)
            String apiKeyPreview = mailgunApiKey.length() > 10 
                ? mailgunApiKey.substring(0, 10) + "..." + mailgunApiKey.substring(mailgunApiKey.length() - 4)
                : "***";
            logger.info("Initializing Mailgun with domain: {}, API key length: {} chars, preview: {}", 
                mailgunDomain, mailgunApiKey.length(), apiKeyPreview);
            
            // Validate API key format
            // Mailgun API keys should start with "key-" for Primary Account keys
            // Domain Sending Keys may not have the "key-" prefix
            boolean hasKeyPrefix = mailgunApiKey.startsWith("key-");
            if (!hasKeyPrefix) {
                logger.warn("Mailgun API key does not start with 'key-'. " +
                    "If you're using a Domain Sending Key, this is normal. " +
                    "If you're using a Primary Account API Key, it should start with 'key-'. " +
                    "Please verify you're using the correct API key from Mailgun dashboard.");
            }
            
            // Determine API endpoint based on region
            String apiBaseUrl;
            if ("eu".equalsIgnoreCase(mailgunRegion)) {
                apiBaseUrl = "https://api.eu.mailgun.net/v3/" + mailgunDomain;
                logger.info("Using Mailgun EU region endpoint");
            } else {
                apiBaseUrl = "https://api.mailgun.net/v3/" + mailgunDomain;
                logger.info("Using Mailgun US region endpoint (default)");
            }
            
            try {
                // Create WebClient for Mailgun API
                // Mailgun uses Basic Auth with format: "api:YOUR_API_KEY"
                // Note: The API key should be used as-is, even if it starts with "key-"
                String authString = "api:" + mailgunApiKey;
                String encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes());
                
                logger.debug("Mailgun base URL: {}", apiBaseUrl);
                
                mailgunWebClient = WebClient.builder()
                    .baseUrl(apiBaseUrl)
                    .defaultHeader("Authorization", "Basic " + encodedAuth)
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build();
                
                // Set from address
                mailgunFromAddress = (mailgunFromEmail != null && !mailgunFromEmail.isEmpty()) 
                    ? mailgunFromEmail.trim()
                    : "noreply@" + mailgunDomain;
                
                logger.info("Mailgun email service initialized successfully for domain: {} (from: {})", 
                    mailgunDomain, mailgunFromAddress);
            } catch (Exception e) {
                logger.error("Failed to initialize Mailgun: {}", e.getMessage(), e);
                mailgunEnabled = false;
            }
        } else {
            logger.debug("Mailgun not enabled, using SMTP fallback");
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
            logger.debug("Sending email via Mailgun to: {}, from: {}, subject: {}", to, mailgunFromAddress, subject);
            
            String response = mailgunWebClient.post()
                .uri("/messages")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                logger.error("Mailgun API error response ({}): {}", 
                                    clientResponse.statusCode(), errorBody);
                                logger.error("Mailgun request details - Domain: {}, From: {}, To: {}", 
                                    mailgunDomain, mailgunFromAddress, to);
                                String errorMsg = "Mailgun API error: " + clientResponse.statusCode();
                                
                                // Provide helpful error messages based on status code
                                if (clientResponse.statusCode().value() == 401) {
                                    mailgunAuthFailureCount++;
                                    
                                    // Log detailed diagnostic information
                                    logger.error("Mailgun 401 Unauthorized - Diagnostic Info:");
                                    logger.error("  - Domain: {}", mailgunDomain);
                                    logger.error("  - From Address: {}", mailgunFromAddress);
                                    logger.error("  - Region: {} (current endpoint)", mailgunRegion);
                                    logger.error("  - API Key Length: {} characters", mailgunApiKey.length());
                                    logger.error("  - API Key Format: {} (starts with 'key-': {})", 
                                        mailgunApiKey.contains("-") ? "Has hyphens" : "WARNING: No hyphens",
                                        mailgunApiKey.startsWith("key-") ? "Yes" : "No");
                                    logger.error("  - API Key Preview: {}...{}", 
                                        mailgunApiKey.length() > 10 ? mailgunApiKey.substring(0, 10) : "***",
                                        mailgunApiKey.length() > 4 ? mailgunApiKey.substring(mailgunApiKey.length() - 4) : "***");
                                    logger.error("  - Error Response: {}", errorBody);
                                    logger.error("  - Troubleshooting: If API key doesn't start with 'key-', try:");
                                    logger.error("    1. Get Primary Account API Key from Account Settings > API Security");
                                    logger.error("    2. Or verify you're using the correct region (US vs EU)");
                                    logger.error("    3. Check Mailgun dashboard: https://app.mailgun.com/app/domains");
                                    
                                    errorMsg += " UNAUTHORIZED - Unauthorized. Check API key.\n" +
                                        "Troubleshooting steps:\n" +
                                        "1. Verify MAILGUN_API_KEY environment variable is set correctly\n" +
                                        "2. Get PRIMARY ACCOUNT API KEY from Mailgun: Account Settings > API Security\n" +
                                        "3. API key should start with 'key-' for Primary Account keys\n" +
                                        "4. If using Domain Sending Key, ensure it matches domain '" + mailgunDomain + "'\n" +
                                        "5. Check Mailgun region: Set MAILGUN_REGION=eu if your account is EU region\n" +
                                        "6. Verify the domain '" + mailgunDomain + "' is verified in Mailgun dashboard\n" +
                                        "7. Check Mailgun dashboard at https://app.mailgun.com/app/domains\n" +
                                        "8. Verify domain status shows 'Active' or 'Verified'\n" +
                                        "9. Try regenerating the API key in Mailgun dashboard\n" +
                                        "Error details: " + errorBody;
                                    
                                    // Disable Mailgun after too many auth failures to prevent spam
                                    if (mailgunAuthFailureCount >= MAX_MAILGUN_AUTH_FAILURES) {
                                        logger.error("Mailgun authentication failed {} times. Disabling Mailgun and using SMTP only. " +
                                            "Please fix MAILGUN_API_KEY and MAILGUN_DOMAIN configuration.", mailgunAuthFailureCount);
                                        mailgunEnabled = false;
                                        mailgunWebClient = null;
                                    }
                                } else if (clientResponse.statusCode().value() == 403) {
                                    errorMsg += " FORBIDDEN - Authentication failed. Please check:\n" +
                                        "1. API key is correct and active\n" +
                                        "2. Domain is verified in Mailgun\n" +
                                        "3. IP whitelisting (if enabled) includes your server IPs\n" +
                                        "4. If using sandbox domain, recipient must be authorized\n" +
                                        "5. Check Mailgun account status and billing\n" +
                                        "Error details: " + errorBody;
                                }
                                
                                return Mono.error(new RuntimeException(errorMsg));
                            });
                    })
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block(); // Block for synchronous operation
            
            logger.debug("Mailgun API response: {}", response);
            // Reset failure counter on successful send
            mailgunAuthFailureCount = 0;
        } catch (Exception e) {
            logger.error("Mailgun API error: {}", e.getMessage(), e);
            
            // Check if it's a sandbox domain issue
            if (mailgunDomain != null && mailgunDomain.contains("sandbox")) {
                throw new RuntimeException("Mailgun sandbox domain detected. Sandbox domains can only send to authorized recipients. " +
                    "Please authorize the recipient email in Mailgun dashboard or use a verified domain. " +
                    "Original error: " + e.getMessage(), e);
            }
            
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

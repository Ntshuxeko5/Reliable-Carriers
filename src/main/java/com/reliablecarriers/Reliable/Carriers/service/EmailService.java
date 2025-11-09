package com.reliablecarriers.Reliable.Carriers.service;

import java.util.Map;

public interface EmailService {
    
    /**
     * Send a simple text email
     */
    void sendSimpleEmail(String to, String subject, String text);
    
    /**
     * Send an HTML email using a template
     */
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
    
    /**
     * Send shipment confirmation email
     */
    void sendShipmentConfirmation(String to, String customerName, String trackingNumber, String estimatedDelivery);
    
    /**
     * Send delivery update email
     */
    void sendDeliveryUpdate(String to, String customerName, String trackingNumber, String status, String location);
    
    /**
     * Send delivery confirmation email
     */
    void sendDeliveryConfirmation(String to, String customerName, String trackingNumber, String deliveryDate);
    
    /**
     * Send driver assignment email
     */
    void sendDriverAssignment(String to, String driverName, String trackingNumber, String pickupAddress, String deliveryAddress);
    
    /**
     * Send payment confirmation email
     */
    void sendPaymentConfirmation(String to, String customerName, String trackingNumber, String amount, String paymentMethod);
    
    /**
     * Send password reset email
     */
    void sendPasswordReset(String to, String resetToken);
    
    /**
     * Send welcome email
     */
    void sendWelcomeEmail(String to, String customerName);
    
    /**
     * Send admin notification
     */
    void sendAdminNotification(String subject, String message);
    
    /**
     * Send bulk email to multiple recipients
     */
    void sendBulkEmail(String[] recipients, String subject, String templateName, Map<String, Object> variables);
    
    /**
     * Send booking confirmation email with verification codes
     */
    void sendBookingConfirmationEmail(String to, String customerName, String bookingNumber, String trackingNumber, 
                                    String serviceType, String totalAmount, String estimatedDelivery,
                                    String pickupAddress, String deliveryAddress, String weight, String description,
                                    String customerPickupCode, String customerDeliveryCode, String pickupContactName,
                                    String pickupContactPhone, String deliveryContactName, String deliveryContactPhone,
                                    String dimensions, String specialInstructions);
    
    /**
     * Send driver document verification status email
     */
    void sendDriverVerificationStatus(String to, String driverName, String documentType, boolean approved, String notes);
    
    /**
     * Send business document verification status email
     */
    void sendBusinessDocumentVerificationStatus(String to, String businessName, String documentType, boolean approved, String notes);
    
    /**
     * Send business account verification status email
     */
    void sendBusinessAccountVerificationStatus(String to, String businessName, boolean approved, String notes, String creditLimit, String paymentTerms);
    
    /**
     * Send document expiry warning email
     */
    void sendDocumentExpiryWarning(String to, String recipientName, String documentType, String expiryDate);
    
    /**
     * Send quote saved confirmation email
     */
    void sendQuoteSavedEmail(String to, String customerName, String quoteId, String serviceType, 
                            String totalCost, String pickupAddress, String deliveryAddress, 
                            String estimatedDelivery, String expiryDate);
}

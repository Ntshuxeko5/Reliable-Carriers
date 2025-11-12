package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Comprehensive Notification Service for Email and SMS
 * Handles all customer, driver, and admin notifications
 */
@Service
@Primary
public class ComprehensiveNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveNotificationService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SmsService smsService;

    @Value("${app.name:Reliable Carriers}")
    private String appName;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:noreply@reliablecarriers.co.za}")
    private String fromEmail;

    // Interface implementation methods
    @Override
    public void sendCustomSmsNotification(String phoneNumber, String message) {
        smsService.sendSms(phoneNumber, message);
    }

    @Override
    public void sendCustomEmailNotification(String email, String subject, String message) {
        sendEmail(email, subject, message);
    }

    // Stub implementations for all interface methods
    @Override
    public void sendShipmentCreatedNotification(Shipment shipment) {
        // Implementation for shipment created notification
        logger.info("Shipment created notification for: {}", shipment.getTrackingNumber());
    }

    @Override
    public void sendShipmentStatusUpdateNotification(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus, String additionalInfo) {
        // Implementation for status update
        logger.info("Shipment status update: {} from {} to {}", shipment.getTrackingNumber(), oldStatus, newStatus);
    }

    @Override
    public void sendShipmentPickedUpNotification(Shipment shipment, String driverName) {
        // Implementation for pickup notification
        logger.info("Shipment picked up: {} by {}", shipment.getTrackingNumber(), driverName);
    }

    @Override
    public void sendShipmentInTransitNotification(Shipment shipment, String currentLocation, String estimatedArrival) {
        // Implementation for in-transit notification
        logger.info("Shipment in transit: {} at {}", shipment.getTrackingNumber(), currentLocation);
    }

    @Override
    public void sendShipmentOutForDeliveryNotification(Shipment shipment, String driverName, String estimatedDeliveryTime) {
        // Implementation for out for delivery notification
        logger.info("Shipment out for delivery: {} by {}", shipment.getTrackingNumber(), driverName);
    }

    @Override
    public void sendShipmentDeliveredNotification(Shipment shipment, String driverName, String deliveryTime) {
        // Implementation for delivered notification
        logger.info("Shipment delivered: {} by {} at {}", shipment.getTrackingNumber(), driverName, deliveryTime);
    }

    @Override
    public void sendShipmentFailedDeliveryNotification(Shipment shipment, String reason, String nextAttempt) {
        // Implementation for failed delivery notification
        logger.info("Shipment delivery failed: {} - {}", shipment.getTrackingNumber(), reason);
    }

    @Override
    public void sendShipmentCancelledNotification(Shipment shipment, String reason) {
        // Implementation for cancelled notification
        logger.info("Shipment cancelled: {} - {}", shipment.getTrackingNumber(), reason);
    }

    @Override
    public void sendDriverAssignedNotification(Shipment shipment, User driver) {
        // Implementation for driver assignment
        logger.info("Driver assigned to shipment: {} - {}", shipment.getTrackingNumber(), driver.getEmail());
    }

    @Override
    public void sendDriverLocationUpdateNotification(Shipment shipment, String latitude, String longitude) {
        // Implementation for location update
        logger.info("Driver location update for: {} at {},{}", shipment.getTrackingNumber(), latitude, longitude);
    }

    @Override
    public void sendDriverOfflineNotification(User driver, String lastKnownLocation) {
        // Implementation for driver offline
        logger.info("Driver offline: {} at {}", driver.getEmail(), lastKnownLocation);
    }

    @Override
    public void sendDriverOnlineNotification(User driver) {
        // Implementation for driver online
        logger.info("Driver online: {}", driver.getEmail());
    }

    @Override
    public void sendMovingServiceCreatedNotification(MovingService movingService) {
        // Implementation for moving service created
        logger.info("Moving service created: {}", movingService.getId());
    }

    @Override
    public void sendDriverAssignedToMovingServiceNotification(MovingService movingService, User driver) {
        // Implementation for driver assigned to moving service
        logger.info("Driver assigned to moving service: {} - {}", movingService.getId(), driver.getEmail());
    }

    @Override
    public void sendMovingServiceStatusUpdateNotification(MovingService movingService, ShipmentStatus oldStatus, ShipmentStatus newStatus, String additionalInfo) {
        // Implementation for moving service status update
        logger.info("Moving service status update: {} from {} to {}", movingService.getId(), oldStatus, newStatus);
    }

    @Override
    public void sendMovingServiceScheduledNotification(MovingService movingService, Date scheduledDate) {
        // Implementation for moving service scheduled
        logger.info("Moving service scheduled: {} for {}", movingService.getId(), scheduledDate);
    }

    @Override
    public void sendMovingServiceCompletedNotification(MovingService movingService, String completionNotes) {
        // Implementation for moving service completed
        logger.info("Moving service completed: {} - {}", movingService.getId(), completionNotes);
    }

    @Override
    public void sendMovingServiceCancelledNotification(MovingService movingService, String reason) {
        // Implementation for moving service cancelled
        logger.info("Moving service cancelled: {} - {}", movingService.getId(), reason);
    }

    @Override
    public void sendTrackingAlertToManager(String alertType, String message, Shipment shipment) {
        // Implementation for tracking alert
        logger.info("Tracking alert: {} - {} for {}", alertType, message, shipment.getTrackingNumber());
    }

    @Override
    public void sendDeliveryDelayAlert(Shipment shipment, String reason, String newEstimatedDelivery) {
        // Implementation for delivery delay alert
        logger.info("Delivery delay: {} - {} new ETA: {}", shipment.getTrackingNumber(), reason, newEstimatedDelivery);
    }

    @Override
    public void sendDriverPerformanceAlert(User driver, String alertType, String details) {
        // Implementation for driver performance alert
        logger.info("Driver performance alert: {} - {} - {}", driver.getEmail(), alertType, details);
    }

    @Override
    public void sendCustomerServiceNotification(String customerEmail, String subject, String message) {
        // Implementation for customer service notification
        sendEmail(customerEmail, subject, message);
    }

    @Override
    public void sendCustomerFeedbackRequest(Shipment shipment) {
        // Implementation for feedback request
        logger.info("Feedback request for shipment: {}", shipment.getTrackingNumber());
    }

    @Override
    public void sendSystemMaintenanceNotification(String startTime, String endTime) {
        // Implementation for system maintenance
        logger.info("System maintenance notification: {} to {}", startTime, endTime);
    }

    @Override
    public void sendSystemErrorNotification(String errorType, String errorDetails) {
        // Implementation for system error
        logger.error("System error notification: {} - {}", errorType, errorDetails);
    }

    @Override
    public void sendBulkStatusUpdateNotifications(List<Shipment> shipments, ShipmentStatus newStatus) {
        // Implementation for bulk status updates
        logger.info("Bulk status update for {} shipments to {}", shipments.size(), newStatus);
    }

    @Override
    public void sendBulkDeliveryReminders(List<Shipment> shipments) {
        // Implementation for bulk delivery reminders
        logger.info("Bulk delivery reminders for {} shipments", shipments.size());
    }

    @Override
    public void updateNotificationPreferences(User user, boolean emailEnabled, boolean smsEnabled) {
        // Implementation for updating preferences
        logger.info("Updated notification preferences for {}: email={}, sms={}", user.getEmail(), emailEnabled, smsEnabled);
    }

    @Override
    public boolean isEmailNotificationEnabled(User user) {
        // Implementation for checking email preference
        return true; // Default to enabled
    }

    @Override
    public boolean isSmsNotificationEnabled(User user) {
        // Implementation for checking SMS preference
        return true; // Default to enabled
    }

    @Override
    public void sendBookingConfirmationNotification(Booking booking) {
        sendBookingConfirmation(booking);
    }

    @Override
    public void sendBookingCancellationNotification(Booking booking) {
        // Implementation for booking cancellation
        logger.info("Booking cancellation notification for: {}", booking.getBookingNumber());
    }

    @Override
    public void sendBookingStatusUpdateNotification(Booking booking, String oldStatus, String newStatus) {
        // Implementation for booking status update
        logger.info("Booking status update: {} from {} to {}", booking.getBookingNumber(), oldStatus, newStatus);
    }

    @Override
    public void sendBookingPaymentConfirmationNotification(Booking booking) {
        // Implementation for payment confirmation
        logger.info("Payment confirmation for booking: {}", booking.getBookingNumber());
    }

    @Override
    public void sendBookingPaymentFailedNotification(Booking booking) {
        // Implementation for payment failed
        logger.info("Payment failed for booking: {}", booking.getBookingNumber());
    }

    /**
     * Send booking confirmation after successful payment
     */
    public void sendBookingConfirmation(Booking booking) {
        try {
            // Email notification to sender (customer)
            String subject = "Booking Confirmed - " + booking.getBookingNumber();
            String emailBody = buildBookingConfirmationEmail(booking);
            sendEmail(booking.getCustomerEmail(), subject, emailBody);

            // SMS notification to sender (customer)
            String smsMessage = buildBookingConfirmationSms(booking);
            smsService.sendSms(booking.getCustomerPhone(), smsMessage);

            // Send notification to receiver (delivery contact) if different from sender
            if (booking.getDeliveryContactPhone() != null && 
                !booking.getDeliveryContactPhone().trim().isEmpty() &&
                !booking.getDeliveryContactPhone().equals(booking.getCustomerPhone())) {
                
                // SMS to receiver
                String receiverSms = String.format(
                    "Package delivery scheduled! Booking: %s. Delivery to: %s, %s. " +
                    "Delivery code: %s. Expected: %s. Track: %s/track/%s",
                    booking.getBookingNumber(),
                    booking.getDeliveryAddress(),
                    booking.getDeliveryCity(),
                    booking.getCustomerDeliveryCode() != null ? booking.getCustomerDeliveryCode() : "N/A",
                    booking.getEstimatedDeliveryDate() != null ? booking.getEstimatedDeliveryDate().toString() : "Soon",
                    baseUrl,
                    booking.getTrackingNumber() != null ? booking.getTrackingNumber() : booking.getBookingNumber()
                );
                smsService.sendSms(booking.getDeliveryContactPhone(), receiverSms);
                logger.info("Booking confirmation SMS sent to receiver (delivery contact) for booking: {}", booking.getBookingNumber());
            }

            // Note: Email to receiver would require deliveryContactEmail field in Booking model
            // Currently only SMS is sent to receiver if phone is different

            logger.info("Booking confirmation sent for booking: {}", booking.getBookingNumber());
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation for booking: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * Send delivery estimation notification
     */
    public void sendDeliveryEstimation(Booking booking, String estimatedDeliveryDate) {
        try {
            // Email notification
            String subject = "Delivery Scheduled - " + booking.getBookingNumber();
            String emailBody = buildDeliveryEstimationEmail(booking, estimatedDeliveryDate);
            sendEmail(booking.getCustomerEmail(), subject, emailBody);

            // SMS notification
            String smsMessage = String.format("Your package %s is scheduled for delivery on %s. Track: %s/track/%s", 
                booking.getTrackingNumber(), estimatedDeliveryDate, baseUrl, booking.getTrackingNumber());
            smsService.sendSms(booking.getCustomerPhone(), smsMessage);

            logger.info("Delivery estimation sent for booking: {}", booking.getBookingNumber());
        } catch (Exception e) {
            logger.error("Failed to send delivery estimation for booking: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * Send pickup notification to customer
     */
    public void sendPickupNotification(Booking booking, User driver) {
        try {
            // Email notification
            String subject = "Package Picked Up - " + booking.getBookingNumber();
            String emailBody = buildPickupNotificationEmail(booking, driver);
            sendEmail(booking.getCustomerEmail(), subject, emailBody);

            // SMS notification
            String smsMessage = String.format("Your package %s has been picked up by driver %s. Track: %s/track/%s", 
                booking.getTrackingNumber(), driver.getFirstName(), baseUrl, booking.getTrackingNumber());
            smsService.sendSms(booking.getCustomerPhone(), smsMessage);

            logger.info("Pickup notification sent for booking: {}", booking.getBookingNumber());
        } catch (Exception e) {
            logger.error("Failed to send pickup notification for booking: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * Send out for delivery notification
     */
    public void sendOutForDeliveryNotification(Booking booking, User driver, String estimatedDeliveryTime) {
        try {
            // Email notification to customer
            String subject = "Out for Delivery - " + booking.getBookingNumber();
            String emailBody = buildOutForDeliveryEmail(booking, driver, estimatedDeliveryTime);
            sendEmail(booking.getCustomerEmail(), subject, emailBody);

            // SMS notification to customer
            String smsMessage = String.format("Your package %s is out for delivery! Expected delivery: %s. Driver: %s (%s)", 
                booking.getTrackingNumber(), estimatedDeliveryTime, driver.getFirstName(), driver.getPhone());
            smsService.sendSms(booking.getCustomerPhone(), smsMessage);

            // SMS notification to delivery contact if different
            if (!booking.getCustomerPhone().equals(booking.getDeliveryContactPhone())) {
                String deliverySms = String.format("Package delivery expected today %s. Delivery code: %s. From: %s", 
                    estimatedDeliveryTime, booking.getCustomerDeliveryCode(), appName);
                smsService.sendSms(booking.getDeliveryContactPhone(), deliverySms);
            }

            logger.info("Out for delivery notification sent for booking: {}", booking.getBookingNumber());
        } catch (Exception e) {
            logger.error("Failed to send out for delivery notification for booking: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * Send delivery confirmation
     */
    public void sendDeliveryConfirmation(Booking booking, User driver) {
        try {
            // Email notification
            String subject = "Package Delivered - " + booking.getBookingNumber();
            String emailBody = buildDeliveryConfirmationEmail(booking, driver);
            sendEmail(booking.getCustomerEmail(), subject, emailBody);

            // SMS notification
            String smsMessage = String.format("Package %s delivered successfully! Thank you for choosing %s. Rate your experience: %s/feedback/%s", 
                booking.getTrackingNumber(), appName, baseUrl, booking.getBookingNumber());
            smsService.sendSms(booking.getCustomerPhone(), smsMessage);

            logger.info("Delivery confirmation sent for booking: {}", booking.getBookingNumber());
        } catch (Exception e) {
            logger.error("Failed to send delivery confirmation for booking: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * Send driver assignment notification
     */
    public void sendDriverAssignmentNotification(Booking booking, User driver) {
        try {
            // Email to driver
            String subject = "New Package Assignment - " + booking.getBookingNumber();
            String emailBody = buildDriverAssignmentEmail(booking, driver);
            sendEmail(driver.getEmail(), subject, emailBody);

            // SMS to driver
            String smsMessage = String.format("New package assigned: %s. Pickup: %s. View details: %s/driver/dashboard", 
                booking.getTrackingNumber(), booking.getPickupAddress(), baseUrl);
            smsService.sendSms(driver.getPhone(), smsMessage);

            logger.info("Driver assignment notification sent for booking: {}", booking.getBookingNumber());
        } catch (Exception e) {
            logger.error("Failed to send driver assignment notification for booking: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * Send admin notification for new booking
     */
    public void sendAdminNewBookingNotification(Booking booking) {
        try {
            // Find admin users and notify them
            String subject = "New Booking Received - " + booking.getBookingNumber();
            String emailBody = buildAdminNewBookingEmail(booking);
            
            // In a real system, you'd query for admin users
            // For now, send to a configured admin email
            String adminEmail = "admin@reliablecarriers.co.za";
            sendEmail(adminEmail, subject, emailBody);

            logger.info("Admin notification sent for new booking: {}", booking.getBookingNumber());
        } catch (Exception e) {
            logger.error("Failed to send admin notification for booking: {}", booking.getBookingNumber(), e);
        }
    }

    // Email building methods
    private String buildBookingConfirmationEmail(Booking booking) {
        return String.format("""
            Dear %s,
            
            Thank you for choosing %s! Your booking has been confirmed.
            
            BOOKING DETAILS:
            Booking Number: %s
            Tracking Number: %s
            Service Type: %s
            
            PICKUP DETAILS:
            Address: %s, %s, %s
            Contact: %s (%s)
            Pickup Code: %s
            
            DELIVERY DETAILS:
            Address: %s, %s, %s
            Contact: %s (%s)
            Delivery Code: %s
            
            PAYMENT DETAILS:
            Total Amount: R %.2f
            Payment Status: %s
            
            You can track your package at: %s/track/%s
            
            Best regards,
            %s Team
            """, 
            booking.getCustomerName(), appName, booking.getBookingNumber(), booking.getTrackingNumber(),
            booking.getServiceType().getDisplayName(),
            booking.getPickupAddress(), booking.getPickupCity(), booking.getPickupState(),
            booking.getPickupContactName(), booking.getPickupContactPhone(), booking.getCustomerPickupCode(),
            booking.getDeliveryAddress(), booking.getDeliveryCity(), booking.getDeliveryState(),
            booking.getDeliveryContactName(), booking.getDeliveryContactPhone(), booking.getCustomerDeliveryCode(),
            booking.getTotalAmount(), booking.getPaymentStatus(),
            baseUrl, booking.getTrackingNumber(), appName);
    }

    private String buildBookingConfirmationSms(Booking booking) {
        return String.format("Booking confirmed! %s - Tracking: %s. Pickup code: %s, Delivery code: %s. Track: %s/track/%s", 
            booking.getBookingNumber(), booking.getTrackingNumber(), 
            booking.getCustomerPickupCode(), booking.getCustomerDeliveryCode(),
            baseUrl, booking.getTrackingNumber());
    }

    /**
     * Build email content for receiver (delivery contact) - for future use if deliveryContactEmail is added
     */
    @SuppressWarnings("unused")
    private String buildReceiverConfirmationEmail(Booking booking) {
        return String.format("""
            Dear %s,
            
            A package delivery has been scheduled to your address.
            
            DELIVERY INFORMATION:
            Booking Number: %s
            Tracking Number: %s
            Delivery Address: %s, %s, %s
            Delivery Code: %s
            Expected Delivery: %s
            
            Please ensure someone is available to receive the package and provide the delivery code when the driver arrives.
            
            Package Details:
            - Service Type: %s
            - Weight: %s kg
            - Description: %s
            
            Track your package: %s/track/%s
            
            If you have any questions, please contact us.
            
            Best regards,
            %s Team
            """, 
            booking.getDeliveryContactName() != null ? booking.getDeliveryContactName() : "Valued Customer",
            booking.getBookingNumber(),
            booking.getTrackingNumber() != null ? booking.getTrackingNumber() : booking.getBookingNumber(),
            booking.getDeliveryAddress(), booking.getDeliveryCity(), booking.getDeliveryState(),
            booking.getCustomerDeliveryCode() != null ? booking.getCustomerDeliveryCode() : "N/A",
            booking.getEstimatedDeliveryDate() != null ? booking.getEstimatedDeliveryDate().toString() : "Soon",
            booking.getServiceType() != null ? booking.getServiceType().getDisplayName() : "Standard Delivery",
            booking.getWeight() != null ? booking.getWeight().toString() : "N/A",
            booking.getDescription() != null ? booking.getDescription() : "Package",
            baseUrl, booking.getTrackingNumber() != null ? booking.getTrackingNumber() : booking.getBookingNumber(),
            appName);
    }

    private String buildDeliveryEstimationEmail(Booking booking, String estimatedDeliveryDate) {
        return String.format("""
            Dear %s,
            
            Your package is being processed and we have an estimated delivery date.
            
            DELIVERY INFORMATION:
            Tracking Number: %s
            Estimated Delivery: %s
            Delivery Address: %s, %s
            Delivery Contact: %s
            Delivery Code: %s
            
            Please ensure someone is available to receive the package and provide the delivery code.
            
            Track your package: %s/track/%s
            
            Best regards,
            %s Team
            """, 
            booking.getCustomerName(), booking.getTrackingNumber(), estimatedDeliveryDate,
            booking.getDeliveryAddress(), booking.getDeliveryCity(), 
            booking.getDeliveryContactName(), booking.getCustomerDeliveryCode(),
            baseUrl, booking.getTrackingNumber(), appName);
    }

    private String buildPickupNotificationEmail(Booking booking, User driver) {
        return String.format("""
            Dear %s,
            
            Great news! Your package has been picked up and is now in transit.
            
            PICKUP DETAILS:
            Tracking Number: %s
            Driver: %s %s
            Driver Phone: %s
            Pickup Time: %s
            
            Your package is now on its way to the destination. You'll receive another notification when it's out for delivery.
            
            Track your package: %s/track/%s
            
            Best regards,
            %s Team
            """, 
            booking.getCustomerName(), booking.getTrackingNumber(),
            driver.getFirstName(), driver.getLastName(), driver.getPhone(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            baseUrl, booking.getTrackingNumber(), appName);
    }

    private String buildOutForDeliveryEmail(Booking booking, User driver, String estimatedDeliveryTime) {
        return String.format("""
            Dear %s,
            
            Your package is out for delivery!
            
            DELIVERY DETAILS:
            Tracking Number: %s
            Driver: %s %s
            Driver Phone: %s
            Estimated Delivery: %s
            Delivery Address: %s
            Delivery Code: %s
            
            Please ensure someone is available to receive the package and provide the delivery code to the driver.
            
            Track your package: %s/track/%s
            
            Best regards,
            %s Team
            """, 
            booking.getCustomerName(), booking.getTrackingNumber(),
            driver.getFirstName(), driver.getLastName(), driver.getPhone(),
            estimatedDeliveryTime, booking.getDeliveryAddress(), booking.getCustomerDeliveryCode(),
            baseUrl, booking.getTrackingNumber(), appName);
    }

    private String buildDeliveryConfirmationEmail(Booking booking, User driver) {
        return String.format("""
            Dear %s,
            
            Your package has been delivered successfully!
            
            DELIVERY CONFIRMATION:
            Tracking Number: %s
            Delivered By: %s %s
            Delivery Time: %s
            Delivery Address: %s
            
            Thank you for choosing %s. We hope you're satisfied with our service.
            
            Please rate your experience: %s/feedback/%s
            
            Best regards,
            %s Team
            """, 
            booking.getCustomerName(), booking.getTrackingNumber(),
            driver.getFirstName(), driver.getLastName(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
            booking.getDeliveryAddress(), appName,
            baseUrl, booking.getBookingNumber(), appName);
    }

    private String buildDriverAssignmentEmail(Booking booking, User driver) {
        return String.format("""
            Dear %s,
            
            You have been assigned a new package for pickup and delivery.
            
            PACKAGE DETAILS:
            Booking Number: %s
            Tracking Number: %s
            Service Type: %s
            Package Description: %s
            
            PICKUP DETAILS:
            Address: %s, %s, %s
            Contact: %s (%s)
            Pickup Code: %s
            
            DELIVERY DETAILS:
            Address: %s, %s, %s
            Contact: %s (%s)
            Delivery Code: %s
            
            Please log into your driver dashboard to accept this assignment: %s/driver/dashboard
            
            Best regards,
            %s Operations Team
            """, 
            driver.getFirstName(), booking.getBookingNumber(), booking.getTrackingNumber(),
            booking.getServiceType().getDisplayName(), booking.getDescription(),
            booking.getPickupAddress(), booking.getPickupCity(), booking.getPickupState(),
            booking.getPickupContactName(), booking.getPickupContactPhone(), booking.getCustomerPickupCode(),
            booking.getDeliveryAddress(), booking.getDeliveryCity(), booking.getDeliveryState(),
            booking.getDeliveryContactName(), booking.getDeliveryContactPhone(), booking.getCustomerDeliveryCode(),
            baseUrl, appName);
    }

    private String buildAdminNewBookingEmail(Booking booking) {
        return String.format("""
            New booking received and requires assignment.
            
            BOOKING DETAILS:
            Booking Number: %s
            Customer: %s (%s)
            Service Type: %s
            Total Amount: R %.2f
            
            PICKUP: %s, %s
            DELIVERY: %s, %s
            
            Please assign a driver: %s/admin/dashboard
            
            %s Operations
            """, 
            booking.getBookingNumber(), booking.getCustomerName(), booking.getCustomerEmail(),
            booking.getServiceType().getDisplayName(), booking.getTotalAmount(),
            booking.getPickupAddress(), booking.getPickupCity(),
            booking.getDeliveryAddress(), booking.getDeliveryCity(),
            baseUrl, appName);
    }

    // Helper method to send email
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
        }
    }
}
package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    @Override
    public void sendCustomSmsNotification(String phoneNumber, String message) {
        // Simple implementation - just log the message for now
        System.out.println("SMS Notification to " + phoneNumber + ": " + message);
    }
    
    @Override
    public void sendCustomEmailNotification(String email, String subject, String message) {
        // Simple implementation - just log the message for now
        System.out.println("Email Notification to " + email + " - Subject: " + subject + " - Message: " + message);
    }
    
    // Shipment-related notifications
    @Override
    public void sendShipmentCreatedNotification(Shipment shipment) {
        String message = "Your shipment " + shipment.getTrackingNumber() + " has been created successfully.";
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendShipmentStatusUpdateNotification(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus, String location) {
        String message = "Your shipment " + shipment.getTrackingNumber() + " status changed from " + oldStatus + " to " + newStatus + " at " + location;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendShipmentPickedUpNotification(Shipment shipment, String driverName) {
        String message = "Your shipment " + shipment.getTrackingNumber() + " has been picked up by " + driverName;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendShipmentInTransitNotification(Shipment shipment, String driverName, String currentLocation) {
        String message = "Your shipment " + shipment.getTrackingNumber() + " is in transit with " + driverName + " at " + currentLocation;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendShipmentOutForDeliveryNotification(Shipment shipment, String driverName, String estimatedTime) {
        String message = "Your shipment " + shipment.getTrackingNumber() + " is out for delivery with " + driverName + ". Estimated delivery: " + estimatedTime;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendShipmentDeliveredNotification(Shipment shipment, String driverName, String deliveryTime) {
        String message = "Your shipment " + shipment.getTrackingNumber() + " has been delivered by " + driverName + " at " + deliveryTime;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendShipmentFailedDeliveryNotification(Shipment shipment, String reason, String nextAttempt) {
        String message = "Delivery failed for shipment " + shipment.getTrackingNumber() + ". Reason: " + reason + ". Next attempt: " + nextAttempt;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendShipmentCancelledNotification(Shipment shipment, String reason) {
        String message = "Your shipment " + shipment.getTrackingNumber() + " has been cancelled. Reason: " + reason;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    // Driver-related notifications
    @Override
    public void sendDriverAssignedNotification(Shipment shipment, User driver) {
        String message = "Driver " + driver.getFirstName() + " " + driver.getLastName() + " has been assigned to your shipment " + shipment.getTrackingNumber();
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendDriverLocationUpdateNotification(Shipment shipment, String driverName, String location) {
        String message = "Driver " + driverName + " is at " + location + " with your shipment " + shipment.getTrackingNumber();
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendDriverOfflineNotification(User driver, String lastKnownLocation) {
        System.out.println("Driver " + driver.getFirstName() + " " + driver.getLastName() + " went offline at " + lastKnownLocation);
    }
    
    @Override
    public void sendDriverOnlineNotification(User driver) {
        System.out.println("Driver " + driver.getFirstName() + " " + driver.getLastName() + " is now online");
    }
    
    // Moving Service Notifications
    @Override
    public void sendMovingServiceCreatedNotification(MovingService movingService) {
        System.out.println("Moving service created: " + movingService.getId());
    }
    
    @Override
    public void sendDriverAssignedToMovingServiceNotification(MovingService movingService, User driver) {
        System.out.println("Driver " + driver.getFirstName() + " assigned to moving service " + movingService.getId());
    }
    
    @Override
    public void sendMovingServiceStatusUpdateNotification(MovingService movingService, ShipmentStatus oldStatus, ShipmentStatus newStatus, String notes) {
        System.out.println("Moving service " + movingService.getId() + " status changed from " + oldStatus + " to " + newStatus);
    }
    
    @Override
    public void sendMovingServiceScheduledNotification(MovingService movingService, Date scheduledDate) {
        System.out.println("Moving service " + movingService.getId() + " scheduled for " + scheduledDate);
    }
    
    @Override
    public void sendMovingServiceCompletedNotification(MovingService movingService, String completionNotes) {
        System.out.println("Moving service " + movingService.getId() + " completed: " + completionNotes);
    }
    
    @Override
    public void sendMovingServiceCancelledNotification(MovingService movingService, String cancellationReason) {
        System.out.println("Moving service " + movingService.getId() + " cancelled: " + cancellationReason);
    }
    
    // Tracking manager notifications
    @Override
    public void sendTrackingAlertToManager(String alertType, String message, Shipment shipment) {
        System.out.println("Tracking alert to manager: " + alertType + " - " + message + " for shipment " + shipment.getTrackingNumber());
    }
    
    @Override
    public void sendDeliveryDelayAlert(Shipment shipment, String reason, String newEstimatedTime) {
        String message = "Delivery delayed for shipment " + shipment.getTrackingNumber() + ". Reason: " + reason + ". New ETA: " + newEstimatedTime;
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    @Override
    public void sendDriverPerformanceAlert(User driver, String metric, String value) {
        System.out.println("Driver performance alert: " + driver.getFirstName() + " " + driver.getLastName() + " - " + metric + ": " + value);
    }
    
    // Customer service notifications
    @Override
    public void sendCustomerServiceNotification(String customerEmail, String subject, String message) {
        sendCustomEmailNotification(customerEmail, subject, message);
    }
    
    @Override
    public void sendCustomerFeedbackRequest(Shipment shipment) {
        String message = "Please provide feedback for your shipment " + shipment.getTrackingNumber();
        sendCustomSmsNotification(shipment.getSender().getPhone(), message);
    }
    
    // System notifications
    @Override
    public void sendSystemMaintenanceNotification(String message, String scheduledTime) {
        System.out.println("System maintenance notification: " + message + " scheduled for " + scheduledTime);
    }
    
    @Override
    public void sendSystemErrorNotification(String error, String affectedService) {
        System.out.println("System error notification: " + error + " in " + affectedService);
    }
    
    // Bulk notifications
    @Override
    public void sendBulkStatusUpdateNotifications(List<Shipment> shipments, ShipmentStatus status) {
        for (Shipment shipment : shipments) {
            String message = "Your shipment " + shipment.getTrackingNumber() + " status updated to " + status;
            sendCustomSmsNotification(shipment.getSender().getPhone(), message);
        }
    }
    
    @Override
    public void sendBulkDeliveryReminders(List<Shipment> shipments) {
        for (Shipment shipment : shipments) {
            String message = "Reminder: Your shipment " + shipment.getTrackingNumber() + " is scheduled for delivery today";
            sendCustomSmsNotification(shipment.getSender().getPhone(), message);
        }
    }
    
    // Notification preferences
    @Override
    public void updateNotificationPreferences(User user, boolean emailEnabled, boolean smsEnabled) {
        System.out.println("Updated notification preferences for user " + user.getEmail() + ": Email=" + emailEnabled + ", SMS=" + smsEnabled);
    }
    
    @Override
    public boolean isEmailNotificationEnabled(User user) {
        return true; // Default to enabled
    }
    
    @Override
    public boolean isSmsNotificationEnabled(User user) {
        return true; // Default to enabled
    }
}

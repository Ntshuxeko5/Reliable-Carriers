package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.model.Booking;

import java.util.Date;

public interface NotificationService {
    
    // Shipment-related notifications
    void sendShipmentCreatedNotification(Shipment shipment);
    void sendShipmentStatusUpdateNotification(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus, String location);
    void sendShipmentPickedUpNotification(Shipment shipment, String driverName);
    void sendShipmentInTransitNotification(Shipment shipment, String driverName, String currentLocation);
    void sendShipmentOutForDeliveryNotification(Shipment shipment, String driverName, String estimatedTime);
    void sendShipmentDeliveredNotification(Shipment shipment, String driverName, String deliveryTime);
    void sendShipmentFailedDeliveryNotification(Shipment shipment, String reason, String nextAttempt);
    void sendShipmentCancelledNotification(Shipment shipment, String reason);
    
    // Driver-related notifications
    void sendDriverAssignedNotification(Shipment shipment, User driver);
    void sendDriverLocationUpdateNotification(Shipment shipment, String driverName, String location);
    void sendDriverOfflineNotification(User driver, String lastKnownLocation);
    void sendDriverOnlineNotification(User driver);
    
    // Moving Service Notifications
    void sendMovingServiceCreatedNotification(MovingService movingService);
    void sendDriverAssignedToMovingServiceNotification(MovingService movingService, User driver);
    void sendMovingServiceStatusUpdateNotification(MovingService movingService, ShipmentStatus oldStatus, ShipmentStatus newStatus, String notes);
    void sendMovingServiceScheduledNotification(MovingService movingService, Date scheduledDate);
    void sendMovingServiceCompletedNotification(MovingService movingService, String completionNotes);
    void sendMovingServiceCancelledNotification(MovingService movingService, String cancellationReason);
    
    // Tracking manager notifications
    void sendTrackingAlertToManager(String alertType, String message, Shipment shipment);
    void sendDeliveryDelayAlert(Shipment shipment, String reason, String newEstimatedTime);
    void sendDriverPerformanceAlert(User driver, String metric, String value);
    
    // Customer service notifications
    void sendCustomerServiceNotification(String customerEmail, String subject, String message);
    void sendCustomerFeedbackRequest(Shipment shipment);
    
    // System notifications
    void sendSystemMaintenanceNotification(String message, String scheduledTime);
    void sendSystemErrorNotification(String error, String affectedService);
    
    // Bulk notifications
    void sendBulkStatusUpdateNotifications(java.util.List<Shipment> shipments, ShipmentStatus status);
    void sendBulkDeliveryReminders(java.util.List<Shipment> shipments);
    
    // Custom notifications
    void sendCustomEmailNotification(String to, String subject, String message);
    void sendCustomSmsNotification(String phoneNumber, String message);
    
    // Booking-related notifications
    void sendBookingConfirmationNotification(Booking booking);
    void sendBookingCancellationNotification(Booking booking);
    void sendBookingStatusUpdateNotification(Booking booking, String oldStatus, String newStatus);
    void sendBookingPaymentConfirmationNotification(Booking booking);
    void sendBookingPaymentFailedNotification(Booking booking);
    
    // Notification preferences
    void updateNotificationPreferences(User user, boolean emailEnabled, boolean smsEnabled);
    boolean isEmailNotificationEnabled(User user);
    boolean isSmsNotificationEnabled(User user);
}

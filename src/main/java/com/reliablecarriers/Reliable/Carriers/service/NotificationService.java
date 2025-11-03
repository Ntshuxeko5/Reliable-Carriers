package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.MovingService;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.util.Date;
import java.util.List;

/**
 * Notification Service Interface
 * Simplified interface matching existing usage patterns
 */
public interface NotificationService {
    
    // Basic notifications
    void sendCustomSmsNotification(String phoneNumber, String message);
    void sendCustomEmailNotification(String email, String subject, String message);
    
    // Existing method signatures from NotificationServiceImpl
    void sendShipmentCreatedNotification(Shipment shipment);
    void sendShipmentStatusUpdateNotification(Shipment shipment, ShipmentStatus oldStatus, ShipmentStatus newStatus, String additionalInfo);
    void sendShipmentPickedUpNotification(Shipment shipment, String driverName);
    void sendShipmentInTransitNotification(Shipment shipment, String currentLocation, String estimatedArrival);
    void sendShipmentOutForDeliveryNotification(Shipment shipment, String driverName, String estimatedDeliveryTime);
    void sendShipmentDeliveredNotification(Shipment shipment, String driverName, String deliveryTime);
    void sendShipmentFailedDeliveryNotification(Shipment shipment, String reason, String nextAttempt);
    void sendShipmentCancelledNotification(Shipment shipment, String reason);
    
    // Driver notifications
    void sendDriverAssignedNotification(Shipment shipment, User driver);
    void sendDriverLocationUpdateNotification(Shipment shipment, String latitude, String longitude);
    void sendDriverOfflineNotification(User driver, String lastKnownLocation);
    void sendDriverOnlineNotification(User driver);
    
    // Moving service notifications
    void sendMovingServiceCreatedNotification(MovingService movingService);
    void sendDriverAssignedToMovingServiceNotification(MovingService movingService, User driver);
    void sendMovingServiceStatusUpdateNotification(MovingService movingService, ShipmentStatus oldStatus, ShipmentStatus newStatus, String additionalInfo);
    void sendMovingServiceScheduledNotification(MovingService movingService, Date scheduledDate);
    void sendMovingServiceCompletedNotification(MovingService movingService, String completionNotes);
    void sendMovingServiceCancelledNotification(MovingService movingService, String reason);
    
    // Admin and tracking notifications
    void sendTrackingAlertToManager(String alertType, String message, Shipment shipment);
    void sendDeliveryDelayAlert(Shipment shipment, String reason, String newEstimatedDelivery);
    void sendDriverPerformanceAlert(User driver, String alertType, String details);
    void sendCustomerServiceNotification(String customerEmail, String subject, String message);
    void sendCustomerFeedbackRequest(Shipment shipment);
    void sendSystemMaintenanceNotification(String startTime, String endTime);
    void sendSystemErrorNotification(String errorType, String errorDetails);
    
    // Bulk operations
    void sendBulkStatusUpdateNotifications(List<Shipment> shipments, ShipmentStatus newStatus);
    void sendBulkDeliveryReminders(List<Shipment> shipments);
    
    // Preferences
    void updateNotificationPreferences(User user, boolean emailEnabled, boolean smsEnabled);
    boolean isEmailNotificationEnabled(User user);
    boolean isSmsNotificationEnabled(User user);
    
    // Booking notifications
    void sendBookingConfirmationNotification(Booking booking);
    void sendBookingCancellationNotification(Booking booking);
    void sendBookingStatusUpdateNotification(Booking booking, String oldStatus, String newStatus);
    void sendBookingPaymentConfirmationNotification(Booking booking);
    void sendBookingPaymentFailedNotification(Booking booking);
}

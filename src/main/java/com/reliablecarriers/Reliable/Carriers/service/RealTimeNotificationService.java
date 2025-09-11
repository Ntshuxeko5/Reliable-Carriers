package com.reliablecarriers.Reliable.Carriers.service;

import java.util.Map;

public interface RealTimeNotificationService {
    
    /**
     * Send notification to a specific user
     */
    void sendNotificationToUser(Long userId, String type, String message, Map<String, Object> data);
    
    /**
     * Send notification to all users with a specific role
     */
    void sendNotificationToRole(String role, String type, String message, Map<String, Object> data);
    
    /**
     * Send notification to all online users
     */
    void sendNotificationToAll(String type, String message, Map<String, Object> data);
    
    /**
     * Send shipment update notification
     */
    void sendShipmentUpdateNotification(String trackingNumber, String status, String customerEmail);
    
    /**
     * Send driver assignment notification
     */
    void sendDriverAssignmentNotification(Long driverId, String trackingNumber, String pickupAddress);
    
    /**
     * Send delivery notification
     */
    void sendDeliveryNotification(String trackingNumber, String customerEmail, String estimatedTime);
    
    /**
     * Send payment notification
     */
    void sendPaymentNotification(String trackingNumber, String customerEmail, String amount);
    
    /**
     * Send admin alert
     */
    void sendAdminAlert(String type, String message, Map<String, Object> data);
    
    /**
     * Send emergency notification
     */
    void sendEmergencyNotification(String message, String[] recipients);
    
    /**
     * Check if user is online
     */
    boolean isUserOnline(Long userId);
    
    /**
     * Get online users count
     */
    int getOnlineUsersCount();
}

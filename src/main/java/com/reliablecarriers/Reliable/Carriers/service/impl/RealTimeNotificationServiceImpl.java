package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.RealTimeNotificationService;
import com.reliablecarriers.Reliable.Carriers.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RealTimeNotificationServiceImpl implements RealTimeNotificationService {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;

    // In-memory storage for online users (in production, use Redis)
    private final ConcurrentHashMap<Long, LocalDateTime> onlineUsers = new ConcurrentHashMap<>();
    private final AtomicInteger onlineCount = new AtomicInteger(0);

    @Override
    public void sendNotificationToUser(Long userId, String type, String message, Map<String, Object> data) {
        // Check if user is online
        if (isUserOnline(userId)) {
            // In production, send via WebSocket
            System.out.println("Real-time notification to user " + userId + ": " + message);
        } else {
            // Send via email/SMS as fallback
            sendFallbackNotification(userId, type, message, data);
        }
    }

    @Override
    public void sendNotificationToRole(String role, String type, String message, Map<String, Object> data) {
        // In production, get all users with this role and send notifications
        System.out.println("Notification to role " + role + ": " + message);
    }

    @Override
    public void sendNotificationToAll(String type, String message, Map<String, Object> data) {
        // In production, broadcast to all online users
        System.out.println("Broadcast notification: " + message);
    }

    @Override
    public void sendShipmentUpdateNotification(String trackingNumber, String status, String customerEmail) {
        Map<String, Object> data = new HashMap<>();
        data.put("trackingNumber", trackingNumber);
        data.put("status", status);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Send email notification
        emailService.sendDeliveryUpdate(customerEmail, "Customer", trackingNumber, status, "Current Location");
        
        // In production, also send real-time notification
        System.out.println("Shipment update notification sent for " + trackingNumber);
    }

    @Override
    public void sendDriverAssignmentNotification(Long driverId, String trackingNumber, String pickupAddress) {
        Map<String, Object> data = new HashMap<>();
        data.put("trackingNumber", trackingNumber);
        data.put("pickupAddress", pickupAddress);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Send SMS notification to driver
        smsService.sendDriverAssignment("+27123456788", trackingNumber, pickupAddress);
        
        // Send real-time notification if driver is online
        sendNotificationToUser(driverId, "DRIVER_ASSIGNMENT", 
            "New delivery assigned: " + trackingNumber, data);
    }

    @Override
    public void sendDeliveryNotification(String trackingNumber, String customerEmail, String estimatedTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("trackingNumber", trackingNumber);
        data.put("estimatedTime", estimatedTime);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Send SMS notification
        smsService.sendDeliveryNotification("+27123456787", trackingNumber, estimatedTime);
        
        // Send email notification
        emailService.sendDeliveryUpdate(customerEmail, "Customer", trackingNumber, "OUT_FOR_DELIVERY", "En route");
        
        System.out.println("Delivery notification sent for " + trackingNumber);
    }

    @Override
    public void sendPaymentNotification(String trackingNumber, String customerEmail, String amount) {
        Map<String, Object> data = new HashMap<>();
        data.put("trackingNumber", trackingNumber);
        data.put("amount", amount);
        data.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Send email notification
        emailService.sendPaymentConfirmation(customerEmail, "Customer", trackingNumber, amount, "PAYSTACK");
        
        System.out.println("Payment notification sent for " + trackingNumber);
    }

    @Override
    public void sendAdminAlert(String type, String message, Map<String, Object> data) {
        // Send to all admin users
        sendNotificationToRole("ADMIN", type, message, data);
        
        // Send email to admin
        emailService.sendAdminNotification(type, message);
        
        System.out.println("Admin alert sent: " + message);
    }

    @Override
    public void sendEmergencyNotification(String message, String[] recipients) {
        // Send emergency SMS to all recipients
        smsService.sendEmergencyNotification("+27123456789", message);
        
        // Send email to admin
        emailService.sendAdminNotification("EMERGENCY", message);
        
        System.out.println("Emergency notification sent: " + message);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        LocalDateTime lastSeen = onlineUsers.get(userId);
        if (lastSeen == null) {
            return false;
        }
        
        // Consider user online if last seen within 5 minutes
        return lastSeen.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    @Override
    public int getOnlineUsersCount() {
        return onlineCount.get();
    }

    // Helper methods
    public void userConnected(Long userId) {
        onlineUsers.put(userId, LocalDateTime.now());
        onlineCount.incrementAndGet();
        System.out.println("User " + userId + " connected. Online users: " + onlineCount.get());
    }

    public void userDisconnected(Long userId) {
        onlineUsers.remove(userId);
        onlineCount.decrementAndGet();
        System.out.println("User " + userId + " disconnected. Online users: " + onlineCount.get());
    }

    public void userHeartbeat(Long userId) {
        onlineUsers.put(userId, LocalDateTime.now());
    }

    private void sendFallbackNotification(Long userId, String type, String message, Map<String, Object> data) {
        // In production, send via email or SMS as fallback
        System.out.println("Fallback notification to user " + userId + ": " + message);
    }
}

package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for real-time updates
 */
@Component
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send package status update to all subscribers
     */
    public void sendPackageStatusUpdate(String trackingNumber, ShipmentStatus status, String location) {
        Map<String, Object> update = new HashMap<>();
        update.put("trackingNumber", trackingNumber);
        update.put("status", status.toString());
        update.put("location", location);
        update.put("timestamp", System.currentTimeMillis());

        // Broadcast to all package update subscribers
        messagingTemplate.convertAndSend("/topic/package-updates", update);

        // Send to specific tracking number subscribers
        messagingTemplate.convertAndSend("/topic/tracking/" + trackingNumber, update);
    }

    /**
     * Send package update to specific customer
     */
    public void sendCustomerPackageUpdate(String customerEmail, Map<String, Object> packageData) {
        messagingTemplate.convertAndSend("/topic/customer-packages/" + customerEmail, packageData);
    }

    /**
     * Send driver location update
     */
    public void sendDriverLocationUpdate(Long driverId, Map<String, Object> locationData) {
        messagingTemplate.convertAndSend("/topic/driver-locations/" + driverId, locationData);
        messagingTemplate.convertAndSend("/topic/tracking-manager/driver-locations", locationData);
    }

    /**
     * Send driver assignment notification
     */
    public void sendDriverAssignment(Long driverId, Map<String, Object> assignmentData) {
        messagingTemplate.convertAndSend("/topic/workboard/" + driverId, assignmentData);
    }

    /**
     * Send live tracking update (driver location + package status)
     */
    public void sendLiveTrackingUpdate(String trackingNumber, Map<String, Object> trackingData) {
        // Send to tracking subscribers
        messagingTemplate.convertAndSend("/topic/live-tracking/" + trackingNumber, trackingData);
        
        // Send general update
        messagingTemplate.convertAndSend("/topic/package-updates", trackingData);
    }
}

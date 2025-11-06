package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Web Push Notification Controller
 * Handles push notification subscription and VAPID key distribution
 */
@RestController
@RequestMapping("/api/push")
@CrossOrigin(origins = "*")
public class WebPushController {

    @Value("${web.push.vapid.public.key:YOUR_VAPID_PUBLIC_KEY_HERE}")
    private String vapidPublicKey;

    /**
     * Get VAPID public key for client-side subscription
     */
    @GetMapping("/vapid-public-key")
    public ResponseEntity<?> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }

    /**
     * Subscribe to push notifications
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> subscriptionData) {
        try {
            // Store subscription in database (associate with user)
            // For now, just return success
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully subscribed to push notifications"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Failed to subscribe: " + e.getMessage()));
        }
    }

    /**
     * Unsubscribe from push notifications
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, Object> subscriptionData) {
        try {
            // Remove subscription from database
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Successfully unsubscribed from push notifications"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Failed to unsubscribe: " + e.getMessage()));
        }
    }
}

package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.model.ShipmentStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.NotificationService;
import com.reliablecarriers.Reliable.Carriers.service.ShipmentService;
import com.reliablecarriers.Reliable.Carriers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
public class NotificationController {

    private final NotificationService notificationService;
    private final ShipmentService shipmentService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, 
                                ShipmentService shipmentService,
                                UserService userService) {
        this.notificationService = notificationService;
        this.shipmentService = shipmentService;
        this.userService = userService;
    }

    /**
     * Send custom email notification
     */
    @PostMapping("/email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendCustomEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String message = request.get("message");
            
            if (to == null || subject == null || message == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: to, subject, message"));
            }
            
            notificationService.sendCustomEmailNotification(to, subject, message);
            return ResponseEntity.ok(Map.of("message", "Email notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }
    }

    /**
     * Send custom SMS notification
     */
    @PostMapping("/sms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendCustomSms(@RequestBody Map<String, String> request) {
        try {
            String phoneNumber = request.get("phoneNumber");
            String message = request.get("message");
            
            if (phoneNumber == null || message == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: phoneNumber, message"));
            }
            
            notificationService.sendCustomSmsNotification(phoneNumber, message);
            return ResponseEntity.ok(Map.of("message", "SMS notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send SMS: " + e.getMessage()));
        }
    }

    /**
     * Send shipment status update notification
     */
    @PostMapping("/shipment/{shipmentId}/status-update")
    public ResponseEntity<Map<String, String>> sendShipmentStatusUpdate(
            @PathVariable Long shipmentId,
            @RequestBody Map<String, String> request) {
        try {
            Shipment shipment = shipmentService.getShipmentById(shipmentId);
            String oldStatus = request.get("oldStatus");
            String newStatus = request.get("newStatus");
            String location = request.get("location");
            
            ShipmentStatus oldStatusEnum = oldStatus != null ? ShipmentStatus.valueOf(oldStatus) : null;
            ShipmentStatus newStatusEnum = newStatus != null ? ShipmentStatus.valueOf(newStatus) : null;
            
            notificationService.sendShipmentStatusUpdateNotification(shipment, oldStatusEnum, newStatusEnum, location);
            return ResponseEntity.ok(Map.of("message", "Status update notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send status update: " + e.getMessage()));
        }
    }

    /**
     * Send delivery reminder notifications
     */
    @PostMapping("/delivery-reminders")
    public ResponseEntity<Map<String, String>> sendDeliveryReminders(@RequestBody List<Long> shipmentIds) {
        try {
            List<Shipment> shipments = shipmentIds.stream()
                .map(shipmentService::getShipmentById)
                .toList();
            
            notificationService.sendBulkDeliveryReminders(shipments);
            return ResponseEntity.ok(Map.of("message", "Delivery reminders sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send delivery reminders: " + e.getMessage()));
        }
    }

    /**
     * Send tracking alert to managers
     */
    @PostMapping("/tracking-alert")
    public ResponseEntity<Map<String, String>> sendTrackingAlert(@RequestBody Map<String, String> request) {
        try {
            String alertType = request.get("alertType");
            String message = request.get("message");
            Long shipmentId = request.get("shipmentId") != null ? Long.valueOf(request.get("shipmentId")) : null;
            
            if (alertType == null || message == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: alertType, message"));
            }
            
            Shipment shipment = shipmentId != null ? shipmentService.getShipmentById(shipmentId) : null;
            notificationService.sendTrackingAlertToManager(alertType, message, shipment);
            return ResponseEntity.ok(Map.of("message", "Tracking alert sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send tracking alert: " + e.getMessage()));
        }
    }

    /**
     * Send driver performance alert
     */
    @PostMapping("/driver/{driverId}/performance-alert")
    public ResponseEntity<Map<String, String>> sendDriverPerformanceAlert(
            @PathVariable Long driverId,
            @RequestBody Map<String, String> request) {
        try {
            User driver = userService.getUserById(driverId);
            String metric = request.get("metric");
            String value = request.get("value");
            
            if (metric == null || value == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: metric, value"));
            }
            
            notificationService.sendDriverPerformanceAlert(driver, metric, value);
            return ResponseEntity.ok(Map.of("message", "Driver performance alert sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send performance alert: " + e.getMessage()));
        }
    }

    /**
     * Send customer service notification
     */
    @PostMapping("/customer-service")
    public ResponseEntity<Map<String, String>> sendCustomerServiceNotification(@RequestBody Map<String, String> request) {
        try {
            String customerEmail = request.get("customerEmail");
            String subject = request.get("subject");
            String message = request.get("message");
            
            if (customerEmail == null || subject == null || message == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: customerEmail, subject, message"));
            }
            
            notificationService.sendCustomerServiceNotification(customerEmail, subject, message);
            return ResponseEntity.ok(Map.of("message", "Customer service notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send customer service notification: " + e.getMessage()));
        }
    }

    /**
     * Send system maintenance notification
     */
    @PostMapping("/system/maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendSystemMaintenanceNotification(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String scheduledTime = request.get("scheduledTime");
            
            if (message == null || scheduledTime == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: message, scheduledTime"));
            }
            
            notificationService.sendSystemMaintenanceNotification(message, scheduledTime);
            return ResponseEntity.ok(Map.of("message", "System maintenance notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send system maintenance notification: " + e.getMessage()));
        }
    }

    /**
     * Send system error notification
     */
    @PostMapping("/system/error")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendSystemErrorNotification(@RequestBody Map<String, String> request) {
        try {
            String error = request.get("error");
            String affectedService = request.get("affectedService");
            
            if (error == null || affectedService == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: error, affectedService"));
            }
            
            notificationService.sendSystemErrorNotification(error, affectedService);
            return ResponseEntity.ok(Map.of("message", "System error notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send system error notification: " + e.getMessage()));
        }
    }

    /**
     * Update user notification preferences
     */
    @PutMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, String>> updateNotificationPreferences(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {
        try {
            User user = userService.getUserById(userId);
            Boolean emailEnabled = request.get("emailEnabled");
            Boolean smsEnabled = request.get("smsEnabled");
            
            if (emailEnabled == null || smsEnabled == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: emailEnabled, smsEnabled"));
            }
            
            notificationService.updateNotificationPreferences(user, emailEnabled, smsEnabled);
            return ResponseEntity.ok(Map.of("message", "Notification preferences updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update preferences: " + e.getMessage()));
        }
    }

    /**
     * Get user notification preferences
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationPreferences(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            Map<String, Object> preferences = new HashMap<>();
            preferences.put("emailEnabled", notificationService.isEmailNotificationEnabled(user));
            preferences.put("smsEnabled", notificationService.isSmsNotificationEnabled(user));
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get preferences: " + e.getMessage()));
        }
    }

    /**
     * Test notification endpoint
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> testNotification(@RequestBody Map<String, String> request) {
        try {
            String type = request.get("type"); // "email" or "sms"
            String recipient = request.get("recipient"); // email or phone number
            String message = request.get("message");
            
            if (type == null || recipient == null || message == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields: type, recipient, message"));
            }
            
            switch (type.toLowerCase()) {
                case "email":
                    notificationService.sendCustomEmailNotification(recipient, "Test Notification", message);
                    break;
                case "sms":
                    notificationService.sendCustomSmsNotification(recipient, message);
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid type. Use 'email' or 'sms'"));
            }
            
            return ResponseEntity.ok(Map.of("message", "Test notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send test notification: " + e.getMessage()));
        }
    }
}

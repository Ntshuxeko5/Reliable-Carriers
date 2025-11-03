package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.service.EmailService;
import com.reliablecarriers.Reliable.Carriers.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;


    @PostMapping("/email")
    public ResponseEntity<?> testEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String message) {
        try {
            emailService.sendSimpleEmail(to, subject, message);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email sent successfully to " + to
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/sms")
    public ResponseEntity<?> testSms(@RequestParam String to, @RequestParam String message) {
        try {
            smsService.sendSms(to, message);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "SMS sent successfully to " + to
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
            "email_service", "configured",
            "sms_service", "configured",
            "2fa_service", "configured",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/debug/services")
    public ResponseEntity<?> debugServices() {
        Map<String, Object> debug = new java.util.HashMap<>();
        
        // Test email service
        try {
            emailService.sendSimpleEmail("test@example.com", "Debug Test", "This is a debug test email");
            debug.put("email_test", "SUCCESS - Email sent");
        } catch (Exception e) {
            debug.put("email_test", "FAILED - " + e.getMessage());
            debug.put("email_error_details", e.getClass().getSimpleName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                debug.put("email_cause", e.getCause().getMessage());
            }
        }
        
        // Test SMS service
        try {
            smsService.sendSms("+1234567890", "Debug test SMS");
            debug.put("sms_test", "SUCCESS - SMS sent");
        } catch (Exception e) {
            debug.put("sms_test", "FAILED - " + e.getMessage());
            debug.put("sms_error_details", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        
        return ResponseEntity.ok(debug);
    }
}

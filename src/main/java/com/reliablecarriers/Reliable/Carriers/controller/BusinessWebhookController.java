package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.Webhook;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import com.reliablecarriers.Reliable.Carriers.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/business/webhooks")
@CrossOrigin(origins = "*")
public class BusinessWebhookController {
    
    @Autowired
    private WebhookService webhookService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create webhook
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createWebhook(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            String url = (String) request.get("url");
            @SuppressWarnings("unchecked")
            List<String> events = (List<String>) request.getOrDefault("events", List.of("*"));
            String description = (String) request.getOrDefault("description", "");
            String secret = (String) request.getOrDefault("secret", null);
            
            Webhook webhook = webhookService.createWebhook(businessUser, url, events, description, secret);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook created successfully",
                "data", Map.of(
                    "id", webhook.getId(),
                    "url", webhook.getUrl(),
                    "secret", webhook.getSecret(),
                    "events", Arrays.asList(webhook.getEvents().split(",")),
                    "status", webhook.getStatus().toString()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Get all webhooks
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWebhooks(Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            List<Webhook> webhooks = webhookService.getUserWebhooks(businessUser);
            
            List<Map<String, Object>> webhooksData = webhooks.stream()
                .map(w -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", w.getId());
                    data.put("url", w.getUrl());
                    data.put("status", w.getStatus().toString());
                    data.put("events", Arrays.asList(w.getEvents().split(",")));
                    data.put("description", w.getDescription());
                    data.put("lastTriggeredAt", w.getLastTriggeredAt());
                    data.put("successCount", w.getSuccessCount());
                    data.put("failureCount", w.getFailureCount());
                    data.put("createdAt", w.getCreatedAt());
                    return data;
                })
                .toList();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", webhooksData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Update webhook
     */
    @PutMapping("/{webhookId}")
    public ResponseEntity<Map<String, Object>> updateWebhook(
            @PathVariable Long webhookId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            
            String url = (String) request.get("url");
            @SuppressWarnings("unchecked")
            List<String> events = (List<String>) request.getOrDefault("events", List.of("*"));
            String description = (String) request.getOrDefault("description", "");
            
            webhookService.updateWebhook(webhookId, businessUser, url, events, description);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Webhook updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Delete webhook
     */
    @DeleteMapping("/{webhookId}")
    public ResponseEntity<Map<String, Object>> deleteWebhook(
            @PathVariable Long webhookId,
            Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            boolean deleted = webhookService.deleteWebhook(webhookId, businessUser);
            
            return ResponseEntity.ok(Map.of(
                "success", deleted,
                "message", "Webhook deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * Test webhook
     */
    @PostMapping("/{webhookId}/test")
    public ResponseEntity<Map<String, Object>> testWebhook(
            @PathVariable Long webhookId,
            Authentication authentication) {
        try {
            User businessUser = getAuthenticatedBusinessUser(authentication);
            boolean success = webhookService.testWebhook(webhookId, businessUser);
            
            return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Webhook test successful" : "Webhook test failed"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
    
    private User getAuthenticatedBusinessUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("Not authenticated");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new SecurityException("User not found"));
        
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new SecurityException("Webhooks are only available for business accounts");
        }
        
        return user;
    }
}


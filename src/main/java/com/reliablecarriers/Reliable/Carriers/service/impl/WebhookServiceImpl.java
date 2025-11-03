package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliablecarriers.Reliable.Carriers.model.*;
import com.reliablecarriers.Reliable.Carriers.repository.WebhookRepository;
import com.reliablecarriers.Reliable.Carriers.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class WebhookServiceImpl implements WebhookService {
    
    @Autowired
    private WebhookRepository webhookRepository;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final SecureRandom random = new SecureRandom();
    
    @Override
    @Transactional
    public Webhook createWebhook(User user, String url, List<String> events, String description, String secret) {
        if (user.getIsBusiness() == null || !user.getIsBusiness()) {
            throw new IllegalArgumentException("Webhooks are only available for business accounts");
        }
        
        // Generate secret if not provided
        if (secret == null || secret.isEmpty()) {
            secret = generateWebhookSecret();
        }
        
        Webhook webhook = new Webhook();
        webhook.setUser(user);
        webhook.setUrl(url);
        webhook.setSecret(secret);
        webhook.setStatus(WebhookStatus.ACTIVE);
        webhook.setEvents(String.join(",", events));
        webhook.setDescription(description);
        
        return webhookRepository.save(webhook);
    }
    
    @Override
    @Transactional
    public Webhook updateWebhook(Long webhookId, User user, String url, List<String> events, String description) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        
        if (!webhook.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to update this webhook");
        }
        
        webhook.setUrl(url);
        webhook.setEvents(String.join(",", events));
        webhook.setDescription(description);
        
        return webhookRepository.save(webhook);
    }
    
    @Override
    @Transactional
    public boolean deleteWebhook(Long webhookId, User user) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        
        if (!webhook.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to delete this webhook");
        }
        
        webhookRepository.delete(webhook);
        return true;
    }
    
    @Override
    public List<Webhook> getUserWebhooks(User user) {
        return webhookRepository.findByUser(user);
    }
    
    @Override
    @Async
    public void triggerWebhook(String eventType, Map<String, Object> payload, User businessUser) {
        List<Webhook> webhooks = webhookRepository.findByUserAndStatus(businessUser, WebhookStatus.ACTIVE);
        
        for (Webhook webhook : webhooks) {
            List<String> subscribedEvents = List.of(webhook.getEvents().split(","));
            if (subscribedEvents.contains(eventType) || subscribedEvents.contains("*")) {
                sendWebhook(webhook, eventType, payload);
            }
        }
    }
    
    @Override
    public boolean testWebhook(Long webhookId, User user) {
        Webhook webhook = webhookRepository.findById(webhookId)
            .orElseThrow(() -> new IllegalArgumentException("Webhook not found"));
        
        if (!webhook.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You don't have permission to test this webhook");
        }
        
        Map<String, Object> testPayload = Map.of(
            "event", "webhook.test",
            "message", "This is a test webhook",
            "timestamp", new Date().toString()
        );
        
        return sendWebhook(webhook, "webhook.test", testPayload);
    }
    
    /**
     * Send webhook HTTP request
     */
    private boolean sendWebhook(Webhook webhook, String eventType, Map<String, Object> payload) {
        try {
            // Build webhook payload
            Map<String, Object> webhookPayload = Map.of(
                "event", eventType,
                "timestamp", new Date().toString(),
                "data", payload
            );
            
            String payloadJson = objectMapper.writeValueAsString(webhookPayload);
            
            // Generate signature
            String signature = generateSignature(payloadJson, webhook.getSecret());
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Signature", signature);
            headers.set("X-Webhook-Event", eventType);
            
            HttpEntity<String> entity = new HttpEntity<>(payloadJson, headers);
            
            // Send request
            ResponseEntity<String> response = restTemplate.exchange(
                webhook.getUrl(),
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Update statistics
            if (response.getStatusCode().is2xxSuccessful()) {
                webhook.setSuccessCount(webhook.getSuccessCount() + 1);
            } else {
                webhook.setFailureCount(webhook.getFailureCount() + 1);
            }
            webhook.setLastTriggeredAt(new Date());
            webhookRepository.save(webhook);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            // Update failure count
            webhook.setFailureCount(webhook.getFailureCount() + 1);
            webhookRepository.save(webhook);
            return false;
        }
    }
    
    /**
     * Generate HMAC signature for webhook
     */
    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate webhook signature", e);
        }
    }
    
    /**
     * Generate random webhook secret
     */
    private String generateWebhookSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}


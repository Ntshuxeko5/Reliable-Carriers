package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.*;
import java.util.List;
import java.util.Map;

public interface WebhookService {
    
    /**
     * Create a new webhook
     */
    Webhook createWebhook(User user, String url, List<String> events, String description, String secret);
    
    /**
     * Update webhook
     */
    Webhook updateWebhook(Long webhookId, User user, String url, List<String> events, String description);
    
    /**
     * Delete webhook
     */
    boolean deleteWebhook(Long webhookId, User user);
    
    /**
     * Get all webhooks for a user
     */
    List<Webhook> getUserWebhooks(User user);
    
    /**
     * Trigger webhook for an event
     */
    void triggerWebhook(String eventType, Map<String, Object> payload, User businessUser);
    
    /**
     * Test webhook
     */
    boolean testWebhook(Long webhookId, User user);
}






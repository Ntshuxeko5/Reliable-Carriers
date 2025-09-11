package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.IntegrationWebhook;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;

import java.util.List;
import java.util.Map;

public interface IntegrationWebhookService {
    
    /**
     * Create new webhook integration
     */
    IntegrationWebhook createWebhook(IntegrationWebhook webhook);
    
    /**
     * Update webhook integration
     */
    IntegrationWebhook updateWebhook(Long webhookId, IntegrationWebhook webhook);
    
    /**
     * Delete webhook integration
     */
    void deleteWebhook(Long webhookId);
    
    /**
     * Get webhook by ID
     */
    IntegrationWebhook getWebhookById(Long webhookId);
    
    /**
     * Get all active webhooks
     */
    List<IntegrationWebhook> getAllActiveWebhooks();
    
    /**
     * Get webhooks by integration type
     */
    List<IntegrationWebhook> getWebhooksByType(String integrationType);
    
    /**
     * Get webhooks by platform
     */
    List<IntegrationWebhook> getWebhooksByPlatform(String platformName);
    
    /**
     * Enable/disable webhook
     */
    IntegrationWebhook toggleWebhook(Long webhookId, Boolean isActive);
    
    /**
     * Test webhook connection
     */
    Map<String, Object> testWebhookConnection(Long webhookId);
    
    /**
     * Trigger webhook for shipment event
     */
    void triggerWebhookForShipment(Shipment shipment, String eventType);
    
    /**
     * Trigger webhook for custom event
     */
    void triggerWebhookForEvent(String eventType, Map<String, Object> payload);
    
    /**
     * Get webhook statistics
     */
    Map<String, Object> getWebhookStatistics();
    
    /**
     * Get webhook performance metrics
     */
    Map<String, Object> getWebhookPerformanceMetrics(Long webhookId);
    
    /**
     * Retry failed webhook
     */
    void retryFailedWebhook(Long webhookId);
    
    /**
     * Get webhook logs
     */
    List<Map<String, Object>> getWebhookLogs(Long webhookId, String startDate, String endDate);
    
    /**
     * Validate webhook configuration
     */
    Map<String, Object> validateWebhookConfiguration(IntegrationWebhook webhook);
    
    /**
     * Get webhook event types
     */
    List<String> getAvailableEventTypes();
    
    /**
     * Get webhook templates for common platforms
     */
    Map<String, IntegrationWebhook> getWebhookTemplates();
    
    /**
     * Create webhook from template
     */
    IntegrationWebhook createWebhookFromTemplate(String templateName, String webhookUrl, String apiKey);
    
    /**
     * Get webhook health status
     */
    Map<String, Object> getWebhookHealthStatus(Long webhookId);
    
    /**
     * Bulk trigger webhooks for multiple shipments
     */
    void bulkTriggerWebhooks(List<Long> shipmentIds, String eventType);
    
    /**
     * Get webhook error logs
     */
    List<Map<String, Object>> getWebhookErrorLogs(Long webhookId);
    
    /**
     * Update webhook authentication
     */
    IntegrationWebhook updateWebhookAuthentication(Long webhookId, String apiKey, String secretKey);
    
    /**
     * Get webhook payload template
     */
    String getWebhookPayloadTemplate(Long webhookId, String eventType);
    
    /**
     * Update webhook payload template
     */
    IntegrationWebhook updateWebhookPayloadTemplate(Long webhookId, String eventType, String template);
    
    /**
     * Get webhook custom headers
     */
    Map<String, String> getWebhookCustomHeaders(Long webhookId);
    
    /**
     * Update webhook custom headers
     */
    IntegrationWebhook updateWebhookCustomHeaders(Long webhookId, Map<String, String> headers);
    
    /**
     * Get webhook retry configuration
     */
    Map<String, Object> getWebhookRetryConfiguration(Long webhookId);
    
    /**
     * Update webhook retry configuration
     */
    IntegrationWebhook updateWebhookRetryConfiguration(Long webhookId, Integer retryAttempts, Integer retryDelaySeconds);
    
    /**
     * Get webhook timeout configuration
     */
    Map<String, Object> getWebhookTimeoutConfiguration(Long webhookId);
    
    /**
     * Update webhook timeout configuration
     */
    IntegrationWebhook updateWebhookTimeoutConfiguration(Long webhookId, Integer timeoutSeconds);
    
    /**
     * Get webhook event history
     */
    List<Map<String, Object>> getWebhookEventHistory(Long webhookId);
    
    /**
     * Get webhook success rate
     */
    Double getWebhookSuccessRate(Long webhookId);
    
    /**
     * Get webhook average response time
     */
    Double getWebhookAverageResponseTime(Long webhookId);
    
    /**
     * Get webhook failure reasons
     */
    List<Map<String, Object>> getWebhookFailureReasons(Long webhookId);
    
    /**
     * Export webhook configuration
     */
    byte[] exportWebhookConfiguration(Long webhookId);
    
    /**
     * Import webhook configuration
     */
    IntegrationWebhook importWebhookConfiguration(byte[] configurationData);
    
    /**
     * Get webhook integration status
     */
    Map<String, Object> getWebhookIntegrationStatus(Long webhookId);
    
    /**
     * Sync webhook with external system
     */
    void syncWebhookWithExternalSystem(Long webhookId);
    
    /**
     * Get webhook monitoring alerts
     */
    List<Map<String, Object>> getWebhookMonitoringAlerts(Long webhookId);
    
    /**
     * Set webhook monitoring alerts
     */
    void setWebhookMonitoringAlerts(Long webhookId, Map<String, Object> alertConfiguration);
}

package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.IntegrationWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IntegrationWebhookRepository extends JpaRepository<IntegrationWebhook, Long> {
    
    /**
     * Find webhooks by type
     */
    List<IntegrationWebhook> findByWebhookTypeOrderByCreatedAtDesc(String webhookType);
    
    /**
     * Find active webhooks
     */
    List<IntegrationWebhook> findByIsActiveTrueOrderByCreatedAtDesc();
    
    /**
     * Find webhooks by name
     */
    List<IntegrationWebhook> findByWebhookNameContainingIgnoreCaseOrderByCreatedAtDesc(String webhookName);
    
    /**
     * Find webhooks by type and active status
     */
    List<IntegrationWebhook> findByWebhookTypeAndIsActiveTrueOrderByCreatedAtDesc(String webhookType);
    
    /**
     * Find webhooks that need retry
     */
    @Query("SELECT w FROM IntegrationWebhook w WHERE w.retryCount < w.maxRetries AND w.isActive = true ORDER BY w.lastTriggered ASC")
    List<IntegrationWebhook> findWebhooksNeedingRetry();
    
    /**
     * Find webhooks by last triggered date
     */
    List<IntegrationWebhook> findByLastTriggeredAfterOrderByLastTriggeredDesc(Date date);
    
    /**
     * Find webhooks by response code
     */
    List<IntegrationWebhook> findByLastResponseCodeOrderByLastTriggeredDesc(Integer responseCode);
    
    /**
     * Get webhook statistics by type
     */
    @Query("SELECT w.webhookType, COUNT(w) FROM IntegrationWebhook w GROUP BY w.webhookType")
    List<Object[]> getWebhookStatisticsByType();
    
    /**
     * Get webhook statistics by status
     */
    @Query("SELECT w.isActive, COUNT(w) FROM IntegrationWebhook w GROUP BY w.isActive")
    List<Object[]> getWebhookStatisticsByStatus();
    
    /**
     * Find webhooks with high retry count
     */
    @Query("SELECT w FROM IntegrationWebhook w WHERE w.retryCount >= :minRetries ORDER BY w.retryCount DESC")
    List<IntegrationWebhook> findWebhooksWithHighRetryCount(@Param("minRetries") Integer minRetries);
    
    /**
     * Find webhooks by URL pattern
     */
    @Query("SELECT w FROM IntegrationWebhook w WHERE w.webhookUrl LIKE %:urlPattern% ORDER BY w.createdAt DESC")
    List<IntegrationWebhook> findWebhooksByUrlPattern(@Param("urlPattern") String urlPattern);
    
    /**
     * Get average response time for webhooks
     */
    @Query("SELECT w.webhookType, AVG(w.lastResponseCode) FROM IntegrationWebhook w WHERE w.lastResponseCode IS NOT NULL GROUP BY w.webhookType")
    List<Object[]> getAverageResponseCodeByType();
    
    /**
     * Find webhooks created in date range
     */
    @Query("SELECT w FROM IntegrationWebhook w WHERE w.createdAt BETWEEN :startDate AND :endDate ORDER BY w.createdAt DESC")
    List<IntegrationWebhook> findWebhooksCreatedInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Find webhooks by secret key pattern
     */
    @Query("SELECT w FROM IntegrationWebhook w WHERE w.secretKey LIKE %:keyPattern% ORDER BY w.createdAt DESC")
    List<IntegrationWebhook> findWebhooksBySecretKeyPattern(@Param("keyPattern") String keyPattern);
    
    /**
     * Get webhook performance statistics
     */
    @Query("SELECT w.webhookType, COUNT(w), AVG(w.retryCount), MAX(w.retryCount) FROM IntegrationWebhook w GROUP BY w.webhookType")
    List<Object[]> getWebhookPerformanceStatistics();
}

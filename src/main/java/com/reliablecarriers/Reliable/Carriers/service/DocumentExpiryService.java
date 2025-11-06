package com.reliablecarriers.Reliable.Carriers.service;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * Service for managing document expiry alerts
 */
public interface DocumentExpiryService {
    
    /**
     * Check for expiring documents and send alerts
     * Runs daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    void checkExpiringDocuments();
    
    /**
     * Check for expired documents and update status
     * Runs daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
    void markExpiredDocuments();
}



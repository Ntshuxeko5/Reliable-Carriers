package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.AuditLog;
import com.reliablecarriers.Reliable.Carriers.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LoginHistoryService {
    
    /**
     * Get login history for a user
     */
    List<AuditLog> getLoginHistory(User user, int limit);
    
    /**
     * Get login history for a user within date range
     */
    List<AuditLog> getLoginHistory(User user, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get login statistics for a user
     */
    Map<String, Object> getLoginStatistics(User user);
    
    /**
     * Get recent login attempts (successful and failed)
     */
    List<AuditLog> getRecentLoginAttempts(User user, int limit);
}


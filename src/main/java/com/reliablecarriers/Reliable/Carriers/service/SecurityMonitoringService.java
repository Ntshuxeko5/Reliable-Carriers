package com.reliablecarriers.Reliable.Carriers.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SecurityMonitoringService {
    
    // Threat detection methods
    boolean detectBruteForceAttack(String ipAddress, String email);
    boolean detectSuspiciousActivity(String ipAddress, String userAgent, String requestPath);
    boolean detectAnomalousBehavior(String userId, String action, String context);
    
    // IP reputation and blacklisting
    boolean isIpBlacklisted(String ipAddress);
    void blacklistIp(String ipAddress, String reason, LocalDateTime expiryDate);
    void whitelistIp(String ipAddress);
    List<String> getBlacklistedIps();
    
    // User behavior analysis
    Map<String, Object> analyzeUserBehavior(String userId);
    boolean isUserBehaviorNormal(String userId, String action);
    void flagSuspiciousUser(String userId, String reason);
    List<String> getFlaggedUsers();
    
    // Security alerts and notifications
    void createSecurityAlert(String type, String description, String severity, String source);
    List<Map<String, Object>> getSecurityAlerts(LocalDateTime since);
    void markAlertAsResolved(String alertId);
    
    // Session management
    void invalidateUserSessions(String userId, String reason);
    boolean isSessionValid(String sessionId, String userId);
    void trackUserSession(String userId, String sessionId, String ipAddress);
    
    // Security metrics and reporting
    Map<String, Object> getSecurityMetrics(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Long> getThreatStatistics(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Object> getSecurityDashboard();
    
    // Automated security responses
    void triggerSecurityResponse(String threatType, String source, String details);
    void enableEnhancedMonitoring(String userId, String reason);
    void disableEnhancedMonitoring(String userId);
    
    // Compliance and audit
    boolean isCompliantWithSecurityPolicy(String userId, String action);
    void logSecurityEvent(String eventType, String userId, String details, String severity);
    List<Map<String, Object>> getSecurityAuditLog(LocalDateTime startDate, LocalDateTime endDate);
    
    // Real-time monitoring
    void startRealTimeMonitoring();
    void stopRealTimeMonitoring();
    boolean isRealTimeMonitoringActive();
    
    // Configuration management
    void updateSecurityConfig(Map<String, Object> config);
    Map<String, Object> getSecurityConfig();
    void resetSecurityConfig();
}

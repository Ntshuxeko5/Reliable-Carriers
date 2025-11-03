package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.service.AuditService;
import com.reliablecarriers.Reliable.Carriers.service.SecurityMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class SecurityMonitoringServiceImpl implements SecurityMonitoringService {

    @Autowired
    private AuditService auditService;

    // In-memory storage (in production, use Redis or database)
    private final Map<String, IpBlacklistEntry> ipBlacklist = new ConcurrentHashMap<>();
    private final Map<String, UserBehaviorProfile> userBehaviorProfiles = new ConcurrentHashMap<>();
    private final Map<String, SecurityAlert> securityAlerts = new ConcurrentHashMap<>();
    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, Boolean> flaggedUsers = new ConcurrentHashMap<>();
    private final Map<String, Object> securityConfig = new ConcurrentHashMap<>();
    
    private final AtomicBoolean realTimeMonitoringActive = new AtomicBoolean(false);

    // Configuration defaults
    private static final int MAX_LOGIN_ATTEMPTS_PER_HOUR = 5;
    private static final int SUSPICIOUS_ACTIVITY_THRESHOLD = 10;
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    @Override
    public boolean detectBruteForceAttack(String ipAddress, String email) {
        String key = ipAddress + ":" + email;
        UserBehaviorProfile profile = userBehaviorProfiles.computeIfAbsent(key, k -> new UserBehaviorProfile());
        
        LocalDateTime now = LocalDateTime.now();
        profile.addLoginAttempt(now);
        
        int attemptsInLastHour = profile.getLoginAttemptsInLastHour();
        
        if (attemptsInLastHour >= MAX_LOGIN_ATTEMPTS_PER_HOUR) {
            createSecurityAlert("BRUTE_FORCE", 
                "Multiple failed login attempts from IP: " + ipAddress + " for email: " + email, 
                "HIGH", ipAddress);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean detectSuspiciousActivity(String ipAddress, String userAgent, String requestPath) {
        // Check for suspicious user agents
        if (userAgent != null && (userAgent.contains("bot") || userAgent.contains("crawler") || userAgent.length() < 20)) {
            createSecurityAlert("SUSPICIOUS_USER_AGENT", 
                "Suspicious user agent detected: " + userAgent, "MEDIUM", ipAddress);
            return true;
        }
        
        // Check for suspicious request patterns
        if (requestPath.contains("/admin") || requestPath.contains("/api/admin")) {
            // Log admin access attempts
            auditService.logAction("ADMIN_ACCESS_ATTEMPT", "SECURITY", null, "INFO", 
                "Admin access attempt from IP: " + ipAddress);
        }
        
        return false;
    }

    @Override
    public boolean detectAnomalousBehavior(String userId, String action, String context) {
        UserBehaviorProfile profile = userBehaviorProfiles.computeIfAbsent(userId, k -> new UserBehaviorProfile());
        
        LocalDateTime now = LocalDateTime.now();
        profile.addAction(action, now);
        
        // Check for unusual activity patterns
        int actionsInLastHour = profile.getActionsInLastHour();
        if (actionsInLastHour > SUSPICIOUS_ACTIVITY_THRESHOLD) {
            createSecurityAlert("ANOMALOUS_BEHAVIOR", 
                "Unusual activity detected for user: " + userId + " - " + actionsInLastHour + " actions in last hour", 
                "MEDIUM", userId);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean isIpBlacklisted(String ipAddress) {
        IpBlacklistEntry entry = ipBlacklist.get(ipAddress);
        if (entry != null) {
            if (entry.getExpiryDate().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                // Remove expired entry
                ipBlacklist.remove(ipAddress);
            }
        }
        return false;
    }

    @Override
    public void blacklistIp(String ipAddress, String reason, LocalDateTime expiryDate) {
        ipBlacklist.put(ipAddress, new IpBlacklistEntry(reason, expiryDate));
        createSecurityAlert("IP_BLACKLISTED", 
            "IP address blacklisted: " + ipAddress + " - Reason: " + reason, "HIGH", ipAddress);
    }

    @Override
    public void whitelistIp(String ipAddress) {
        ipBlacklist.remove(ipAddress);
        createSecurityAlert("IP_WHITELISTED", 
            "IP address whitelisted: " + ipAddress, "LOW", ipAddress);
    }

    @Override
    public List<String> getBlacklistedIps() {
        return new ArrayList<>(ipBlacklist.keySet());
    }

    @Override
    public Map<String, Object> analyzeUserBehavior(String userId) {
        UserBehaviorProfile profile = userBehaviorProfiles.get(userId);
        Map<String, Object> analysis = new HashMap<>();
        
        if (profile != null) {
            analysis.put("totalActions", profile.getTotalActions());
            analysis.put("actionsInLastHour", profile.getActionsInLastHour());
            analysis.put("loginAttemptsInLastHour", profile.getLoginAttemptsInLastHour());
            analysis.put("lastActivity", profile.getLastActivity());
            analysis.put("isFlagged", flaggedUsers.containsKey(userId));
        }
        
        return analysis;
    }

    @Override
    public boolean isUserBehaviorNormal(String userId, String action) {
        UserBehaviorProfile profile = userBehaviorProfiles.get(userId);
        if (profile == null) {
            return true; // New user, assume normal
        }
        
        // Simple heuristic: if user has too many actions in short time, flag as suspicious
        return profile.getActionsInLastHour() <= SUSPICIOUS_ACTIVITY_THRESHOLD;
    }

    @Override
    public void flagSuspiciousUser(String userId, String reason) {
        flaggedUsers.put(userId, true);
        createSecurityAlert("USER_FLAGGED", 
            "User flagged as suspicious: " + userId + " - Reason: " + reason, "MEDIUM", userId);
    }

    @Override
    public List<String> getFlaggedUsers() {
        return new ArrayList<>(flaggedUsers.keySet());
    }

    @Override
    public void createSecurityAlert(String type, String description, String severity, String source) {
        String alertId = UUID.randomUUID().toString();
        SecurityAlert alert = new SecurityAlert(alertId, type, description, severity, source, LocalDateTime.now());
        securityAlerts.put(alertId, alert);
        
        // Log to audit service
        auditService.logAction("SECURITY_ALERT", "SECURITY", null, severity, 
            "Alert: " + type + " - " + description + " - Source: " + source);
    }

    @Override
    public List<Map<String, Object>> getSecurityAlerts(LocalDateTime since) {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        for (SecurityAlert alert : securityAlerts.values()) {
            if (alert.getCreatedAt().isAfter(since)) {
                Map<String, Object> alertMap = new HashMap<>();
                alertMap.put("id", alert.getId());
                alertMap.put("type", alert.getType());
                alertMap.put("description", alert.getDescription());
                alertMap.put("severity", alert.getSeverity());
                alertMap.put("source", alert.getSource());
                alertMap.put("createdAt", alert.getCreatedAt());
                alertMap.put("resolved", alert.isResolved());
                alerts.add(alertMap);
            }
        }
        
        return alerts;
    }

    @Override
    public void markAlertAsResolved(String alertId) {
        SecurityAlert alert = securityAlerts.get(alertId);
        if (alert != null) {
            alert.setResolved(true);
        }
    }

    @Override
    public void invalidateUserSessions(String userId, String reason) {
        // Remove all sessions for the user
        activeSessions.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId));
        
        createSecurityAlert("SESSIONS_INVALIDATED", 
            "All sessions invalidated for user: " + userId + " - Reason: " + reason, "MEDIUM", userId);
    }

    @Override
    public boolean isSessionValid(String sessionId, String userId) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null && session.getUserId().equals(userId)) {
            // Check if session has expired
            if (session.getLastActivity().plusMinutes(SESSION_TIMEOUT_MINUTES).isAfter(LocalDateTime.now())) {
                session.updateLastActivity();
                return true;
            } else {
                activeSessions.remove(sessionId);
            }
        }
        return false;
    }

    @Override
    public void trackUserSession(String userId, String sessionId, String ipAddress) {
        UserSession session = new UserSession(sessionId, userId, ipAddress, LocalDateTime.now());
        activeSessions.put(sessionId, session);
    }

    @Override
    public Map<String, Object> getSecurityMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Count security alerts by severity
        Map<String, Long> alertsBySeverity = new HashMap<>();
        for (SecurityAlert alert : securityAlerts.values()) {
            if (alert.getCreatedAt().isAfter(startDate) && alert.getCreatedAt().isBefore(endDate)) {
                alertsBySeverity.merge(alert.getSeverity(), 1L, Long::sum);
            }
        }
        
        metrics.put("totalAlerts", securityAlerts.size());
        metrics.put("alertsBySeverity", alertsBySeverity);
        metrics.put("blacklistedIps", ipBlacklist.size());
        metrics.put("flaggedUsers", flaggedUsers.size());
        metrics.put("activeSessions", activeSessions.size());
        
        return metrics;
    }

    @Override
    public Map<String, Long> getThreatStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Long> statistics = new HashMap<>();
        
        for (SecurityAlert alert : securityAlerts.values()) {
            if (alert.getCreatedAt().isAfter(startDate) && alert.getCreatedAt().isBefore(endDate)) {
                statistics.merge(alert.getType(), 1L, Long::sum);
            }
        }
        
        return statistics;
    }

    @Override
    public Map<String, Object> getSecurityDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        dashboard.put("last24Hours", getSecurityMetrics(last24Hours, LocalDateTime.now()));
        dashboard.put("activeThreats", getActiveThreats());
        dashboard.put("securityStatus", getSecurityStatus());
        
        return dashboard;
    }

    @Override
    public void triggerSecurityResponse(String threatType, String source, String details) {
        switch (threatType) {
            case "BRUTE_FORCE":
                blacklistIp(source, "Brute force attack detected", LocalDateTime.now().plusHours(24));
                break;
            case "SUSPICIOUS_ACTIVITY":
                createSecurityAlert("AUTOMATED_RESPONSE", 
                    "Automated response triggered for suspicious activity from: " + source, "MEDIUM", source);
                break;
            default:
                createSecurityAlert("AUTOMATED_RESPONSE", 
                    "Automated response triggered for: " + threatType + " from: " + source, "MEDIUM", source);
        }
    }

    @Override
    public void enableEnhancedMonitoring(String userId, String reason) {
        // Implementation for enhanced monitoring
        createSecurityAlert("ENHANCED_MONITORING", 
            "Enhanced monitoring enabled for user: " + userId + " - Reason: " + reason, "LOW", userId);
    }

    @Override
    public void disableEnhancedMonitoring(String userId) {
        createSecurityAlert("ENHANCED_MONITORING", 
            "Enhanced monitoring disabled for user: " + userId, "LOW", userId);
    }

    @Override
    public boolean isCompliantWithSecurityPolicy(String userId, String action) {
        // Basic compliance check
        if (isIpBlacklisted(getUserIpAddress(userId))) {
            return false;
        }
        
        if (flaggedUsers.containsKey(userId)) {
            return false;
        }
        
        return true;
    }

    @Override
    public void logSecurityEvent(String eventType, String userId, String details, String severity) {
        auditService.logAction("SECURITY_EVENT", "SECURITY", null, severity, 
            "Event: " + eventType + " - User: " + userId + " - Details: " + details);
    }

    @Override
    public List<Map<String, Object>> getSecurityAuditLog(LocalDateTime startDate, LocalDateTime endDate) {
        // This would typically query the audit service
        return new ArrayList<>();
    }

    @Override
    public void startRealTimeMonitoring() {
        realTimeMonitoringActive.set(true);
        createSecurityAlert("MONITORING", "Real-time security monitoring started", "LOW", "SYSTEM");
    }

    @Override
    public void stopRealTimeMonitoring() {
        realTimeMonitoringActive.set(false);
        createSecurityAlert("MONITORING", "Real-time security monitoring stopped", "LOW", "SYSTEM");
    }

    @Override
    public boolean isRealTimeMonitoringActive() {
        return realTimeMonitoringActive.get();
    }

    @Override
    public void updateSecurityConfig(Map<String, Object> config) {
        securityConfig.putAll(config);
    }

    @Override
    public Map<String, Object> getSecurityConfig() {
        return new HashMap<>(securityConfig);
    }

    @Override
    public void resetSecurityConfig() {
        securityConfig.clear();
    }

    // Helper methods
    private String getUserIpAddress(String userId) {
        // This would typically be retrieved from session or request context
        return "unknown";
    }

    private Map<String, Object> getActiveThreats() {
        Map<String, Object> threats = new HashMap<>();
        threats.put("blacklistedIps", ipBlacklist.size());
        threats.put("flaggedUsers", flaggedUsers.size());
        threats.put("pendingAlerts", securityAlerts.values().stream().filter(a -> !a.isResolved()).count());
        return threats;
    }

    private String getSecurityStatus() {
        if (securityAlerts.values().stream().anyMatch(a -> "HIGH".equals(a.getSeverity()) && !a.isResolved())) {
            return "CRITICAL";
        } else if (securityAlerts.values().stream().anyMatch(a -> "MEDIUM".equals(a.getSeverity()) && !a.isResolved())) {
            return "WARNING";
        } else {
            return "SECURE";
        }
    }

    // Inner classes for data structures
    private static class IpBlacklistEntry {
        private final String reason;
        private final LocalDateTime expiryDate;

        public IpBlacklistEntry(String reason, LocalDateTime expiryDate) {
            this.reason = reason;
            this.expiryDate = expiryDate;
        }

        @SuppressWarnings("unused")
        public String getReason() { return reason; }
        public LocalDateTime getExpiryDate() { return expiryDate; }
    }

    private static class UserBehaviorProfile {
        private final List<LocalDateTime> actions = new ArrayList<>();
        private final List<LocalDateTime> loginAttempts = new ArrayList<>();

        public void addAction(String action, LocalDateTime time) {
            actions.add(time);
        }

        public void addLoginAttempt(LocalDateTime time) {
            loginAttempts.add(time);
        }

        public int getTotalActions() { return actions.size(); }

        public int getActionsInLastHour() {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            return (int) actions.stream().filter(time -> time.isAfter(oneHourAgo)).count();
        }

        public int getLoginAttemptsInLastHour() {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            return (int) loginAttempts.stream().filter(time -> time.isAfter(oneHourAgo)).count();
        }

        public LocalDateTime getLastActivity() {
            return actions.isEmpty() ? null : actions.get(actions.size() - 1);
        }
    }

    private static class SecurityAlert {
        private final String id;
        private final String type;
        private final String description;
        private final String severity;
        private final String source;
        private final LocalDateTime createdAt;
        private boolean resolved = false;

        public SecurityAlert(String id, String type, String description, String severity, String source, LocalDateTime createdAt) {
            this.id = id;
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.source = source;
            this.createdAt = createdAt;
        }

        // Getters
        public String getId() { return id; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
        public String getSource() { return source; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public boolean isResolved() { return resolved; }

        // Setters
        public void setResolved(boolean resolved) { this.resolved = resolved; }
    }

    private static class UserSession {
        private final String sessionId;
        private final String userId;
        private final String ipAddress;
        private LocalDateTime lastActivity;

        public UserSession(String sessionId, String userId, String ipAddress, LocalDateTime lastActivity) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.ipAddress = ipAddress;
            this.lastActivity = lastActivity;
        }

        @SuppressWarnings("unused")
        public String getSessionId() { return sessionId; }
        public String getUserId() { return userId; }
        @SuppressWarnings("unused")
        public String getIpAddress() { return ipAddress; }
        public LocalDateTime getLastActivity() { return lastActivity; }

        public void updateLastActivity() {
            this.lastActivity = LocalDateTime.now();
        }
    }
}

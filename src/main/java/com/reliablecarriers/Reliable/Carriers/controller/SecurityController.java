package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.service.SecurityMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/security")
@PreAuthorize("hasRole('ADMIN')")
public class SecurityController {

    @Autowired
    private SecurityMonitoringService securityMonitoringService;

    // Security Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getSecurityDashboard() {
        Map<String, Object> dashboard = securityMonitoringService.getSecurityDashboard();
        return ResponseEntity.ok(dashboard);
    }

    // Security Metrics
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSecurityMetrics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> metrics = securityMonitoringService.getSecurityMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }

    // Threat Statistics
    @GetMapping("/threats")
    public ResponseEntity<Map<String, Long>> getThreatStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Long> statistics = securityMonitoringService.getThreatStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    // Security Alerts
    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getSecurityAlerts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        List<Map<String, Object>> alerts = securityMonitoringService.getSecurityAlerts(since);
        return ResponseEntity.ok(alerts);
    }

    @PutMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<String> resolveAlert(@PathVariable String alertId) {
        securityMonitoringService.markAlertAsResolved(alertId);
        return ResponseEntity.ok("Alert resolved successfully");
    }

    // IP Management
    @GetMapping("/ips/blacklisted")
    public ResponseEntity<List<String>> getBlacklistedIps() {
        List<String> blacklistedIps = securityMonitoringService.getBlacklistedIps();
        return ResponseEntity.ok(blacklistedIps);
    }

    @PostMapping("/ips/blacklist")
    public ResponseEntity<String> blacklistIp(
            @RequestParam String ipAddress,
            @RequestParam String reason,
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(hours);
        securityMonitoringService.blacklistIp(ipAddress, reason, expiryDate);
        return ResponseEntity.ok("IP address blacklisted successfully");
    }

    @DeleteMapping("/ips/blacklist/{ipAddress}")
    public ResponseEntity<String> whitelistIp(@PathVariable String ipAddress) {
        securityMonitoringService.whitelistIp(ipAddress);
        return ResponseEntity.ok("IP address whitelisted successfully");
    }

    // User Management
    @GetMapping("/users/flagged")
    public ResponseEntity<List<String>> getFlaggedUsers() {
        List<String> flaggedUsers = securityMonitoringService.getFlaggedUsers();
        return ResponseEntity.ok(flaggedUsers);
    }

    @PostMapping("/users/{userId}/flag")
    public ResponseEntity<String> flagUser(
            @PathVariable String userId,
            @RequestParam String reason) {
        securityMonitoringService.flagSuspiciousUser(userId, reason);
        return ResponseEntity.ok("User flagged successfully");
    }

    @GetMapping("/users/{userId}/behavior")
    public ResponseEntity<Map<String, Object>> analyzeUserBehavior(@PathVariable String userId) {
        Map<String, Object> analysis = securityMonitoringService.analyzeUserBehavior(userId);
        return ResponseEntity.ok(analysis);
    }

    @PostMapping("/users/{userId}/sessions/invalidate")
    public ResponseEntity<String> invalidateUserSessions(
            @PathVariable String userId,
            @RequestParam String reason) {
        securityMonitoringService.invalidateUserSessions(userId, reason);
        return ResponseEntity.ok("User sessions invalidated successfully");
    }

    // Monitoring Control
    @PostMapping("/monitoring/start")
    public ResponseEntity<String> startRealTimeMonitoring() {
        securityMonitoringService.startRealTimeMonitoring();
        return ResponseEntity.ok("Real-time monitoring started");
    }

    @PostMapping("/monitoring/stop")
    public ResponseEntity<String> stopRealTimeMonitoring() {
        securityMonitoringService.stopRealTimeMonitoring();
        return ResponseEntity.ok("Real-time monitoring stopped");
    }

    @GetMapping("/monitoring/status")
    public ResponseEntity<Map<String, Object>> getMonitoringStatus() {
        Map<String, Object> status = Map.of(
            "active", securityMonitoringService.isRealTimeMonitoringActive()
        );
        return ResponseEntity.ok(status);
    }

    // Security Configuration
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSecurityConfig() {
        Map<String, Object> config = securityMonitoringService.getSecurityConfig();
        return ResponseEntity.ok(config);
    }

    @PutMapping("/config")
    public ResponseEntity<String> updateSecurityConfig(@RequestBody Map<String, Object> config) {
        securityMonitoringService.updateSecurityConfig(config);
        return ResponseEntity.ok("Security configuration updated successfully");
    }

    @DeleteMapping("/config")
    public ResponseEntity<String> resetSecurityConfig() {
        securityMonitoringService.resetSecurityConfig();
        return ResponseEntity.ok("Security configuration reset to defaults");
    }

    // Manual Security Responses
    @PostMapping("/response/trigger")
    public ResponseEntity<String> triggerSecurityResponse(
            @RequestParam String threatType,
            @RequestParam String source,
            @RequestParam String details) {
        securityMonitoringService.triggerSecurityResponse(threatType, source, details);
        return ResponseEntity.ok("Security response triggered successfully");
    }

    @PostMapping("/users/{userId}/monitoring/enable")
    public ResponseEntity<String> enableEnhancedMonitoring(
            @PathVariable String userId,
            @RequestParam String reason) {
        securityMonitoringService.enableEnhancedMonitoring(userId, reason);
        return ResponseEntity.ok("Enhanced monitoring enabled for user");
    }

    @PostMapping("/users/{userId}/monitoring/disable")
    public ResponseEntity<String> disableEnhancedMonitoring(@PathVariable String userId) {
        securityMonitoringService.disableEnhancedMonitoring(userId);
        return ResponseEntity.ok("Enhanced monitoring disabled for user");
    }

    // Security Audit
    @GetMapping("/audit")
    public ResponseEntity<List<Map<String, Object>>> getSecurityAuditLog(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Map<String, Object>> auditLog = securityMonitoringService.getSecurityAuditLog(startDate, endDate);
        return ResponseEntity.ok(auditLog);
    }

    // Threat Detection Testing
    @PostMapping("/test/brute-force")
    public ResponseEntity<Map<String, Object>> testBruteForceDetection(
            @RequestParam String ipAddress,
            @RequestParam String email) {
        boolean detected = securityMonitoringService.detectBruteForceAttack(ipAddress, email);
        return ResponseEntity.ok(Map.of(
            "detected", detected,
            "ipAddress", ipAddress,
            "email", email
        ));
    }

    @PostMapping("/test/suspicious-activity")
    public ResponseEntity<Map<String, Object>> testSuspiciousActivityDetection(
            @RequestParam String ipAddress,
            @RequestParam String userAgent,
            @RequestParam String requestPath) {
        boolean detected = securityMonitoringService.detectSuspiciousActivity(ipAddress, userAgent, requestPath);
        return ResponseEntity.ok(Map.of(
            "detected", detected,
            "ipAddress", ipAddress,
            "userAgent", userAgent,
            "requestPath", requestPath
        ));
    }

    @PostMapping("/test/anomalous-behavior")
    public ResponseEntity<Map<String, Object>> testAnomalousBehaviorDetection(
            @RequestParam String userId,
            @RequestParam String action,
            @RequestParam String context) {
        boolean detected = securityMonitoringService.detectAnomalousBehavior(userId, action, context);
        return ResponseEntity.ok(Map.of(
            "detected", detected,
            "userId", userId,
            "action", action,
            "context", context
        ));
    }
}

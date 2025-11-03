package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.AuditLog;
import com.reliablecarriers.Reliable.Carriers.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {
    
    @Autowired
    private AuditService auditService;
    
    // Get audit logs with pagination
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs;
        
        if (userId != null) {
            logs = auditService.getAuditLogsByUser(Long.valueOf(userId), pageable);
        } else if (userRole != null) {
            logs = auditService.getAuditLogsByRole(userRole, pageable);
        } else if (action != null) {
            logs = auditService.getAuditLogsByAction(action, pageable);
        } else if (status != null) {
            logs = auditService.getAuditLogsByStatus(status, pageable);
        } else if (startDate != null && endDate != null) {
            logs = auditService.getAuditLogsByDateRange(startDate, endDate, pageable);
        } else {
            // Default to recent activity
            logs = auditService.getAuditLogsByDateRange(
                LocalDateTime.now().minusDays(7), 
                LocalDateTime.now(), 
                pageable
            );
        }
        
        return ResponseEntity.ok(logs);
    }
    
    // Get audit statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        stats.put("totalLogs", auditService.getAuditCountByDateRange(startDate, endDate));
        stats.put("recentActivity", auditService.getAuditCountByDateRange(endDate.minusHours(24), endDate));
        
        return ResponseEntity.ok(stats);
    }
    
    // Get audit analytics
    @GetMapping("/analytics/counts")
    public ResponseEntity<Map<String, Object>> getAuditAnalytics(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        
        Map<String, Object> analytics = new HashMap<>();
        
        analytics.put("byRole", auditService.getAuditCountByRole(startDate, endDate));
        analytics.put("byAction", auditService.getAuditCountByAction(startDate, endDate));
        analytics.put("byStatus", auditService.getAuditCountByStatus(startDate, endDate));
        analytics.put("dailyCount", auditService.getDailyAuditCount(startDate, endDate));
        
        return ResponseEntity.ok(analytics);
    }
    
    // Get audit logs by user
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable Long userId) {
        List<AuditLog> logs = auditService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(logs);
    }
    
    // Get audit logs by role
    @GetMapping("/logs/role/{userRole}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByRole(@PathVariable String userRole) {
        List<AuditLog> logs = auditService.getAuditLogsByRole(userRole);
        return ResponseEntity.ok(logs);
    }
    
    // Get audit logs by action
    @GetMapping("/logs/action/{action}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(@PathVariable String action) {
        List<AuditLog> logs = auditService.getAuditLogsByAction(action);
        return ResponseEntity.ok(logs);
    }
    
    // Get audit logs by status
    @GetMapping("/logs/status/{status}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByStatus(@PathVariable String status) {
        List<AuditLog> logs = auditService.getAuditLogsByStatus(status);
        return ResponseEntity.ok(logs);
    }
    
    // Get audit logs by date range
    @GetMapping("/logs/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> logs = auditService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    // Get audit logs by IP address
    @GetMapping("/logs/ip/{ipAddress}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByIpAddress(@PathVariable String ipAddress) {
        List<AuditLog> logs = auditService.getAuditLogsByIpAddress(ipAddress);
        return ResponseEntity.ok(logs);
    }
    
    // Get recent activity
    @GetMapping("/logs/recent")
    public ResponseEntity<List<AuditLog>> getRecentActivity(@RequestParam(defaultValue = "24") int hours) {
        List<AuditLog> logs = auditService.getRecentActivity(hours);
        return ResponseEntity.ok(logs);
    }
    
    // Get failed actions
    @GetMapping("/logs/failed")
    public ResponseEntity<List<AuditLog>> getFailedActions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> logs = auditService.getFailedActions(startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    // Get suspicious activity
    @GetMapping("/logs/suspicious")
    public ResponseEntity<List<AuditLog>> getSuspiciousActivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "5") Long threshold) {
        List<AuditLog> logs = auditService.getSuspiciousActivity(startDate, endDate, threshold);
        return ResponseEntity.ok(logs);
    }
    
    
    // Security and compliance endpoints
    @GetMapping("/security/login-attempts")
    public ResponseEntity<List<AuditLog>> getLoginAttempts(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> logs = auditService.getLoginAttempts(email, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/security/failed-logins")
    public ResponseEntity<List<AuditLog>> getFailedLoginAttempts(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> logs = auditService.getFailedLoginAttempts(email, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/security/failed-login-count")
    public ResponseEntity<Long> getFailedLoginCount(
            @RequestParam String email,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Long count = auditService.getFailedLoginCount(email, startDate, endDate);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/security/password-changes/{userId}")
    public ResponseEntity<List<AuditLog>> getPasswordChangeHistory(@PathVariable Long userId) {
        List<AuditLog> logs = auditService.getPasswordChangeHistory(userId);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/security/data-access")
    public ResponseEntity<List<AuditLog>> getDataAccessHistory(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> logs = auditService.getDataAccessHistory(userId, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/security/sensitive-data")
    public ResponseEntity<List<AuditLog>> getSensitiveDataAccess(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> logs = auditService.getSensitiveDataAccess(entityType, entityId, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
    
    // Export endpoints
    @GetMapping("/export/csv")
    public ResponseEntity<String> exportToCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String filters) {
        String csv = auditService.exportAuditLogsToCsv(startDate, endDate, filters);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "audit_logs.csv");
        return new ResponseEntity<>(csv, headers, HttpStatus.OK);
    }
    
    @GetMapping("/export/json")
    public ResponseEntity<String> exportToJson(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String filters) {
        String json = auditService.exportAuditLogsToJson(startDate, endDate, filters);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "audit_logs.json");
        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }
    
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String filters) {
        byte[] pdf = auditService.exportAuditLogsToPdf(startDate, endDate, filters);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "audit_logs.pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
    
    // Maintenance endpoints
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> deleteOldAuditLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {
        auditService.deleteOldAuditLogs(beforeDate);
        return ResponseEntity.ok("Old audit logs deleted successfully");
    }
    
    @PostMapping("/archive")
    public ResponseEntity<String> archiveAuditLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate) {
        auditService.archiveAuditLogs(beforeDate);
        return ResponseEntity.ok("Audit logs archived successfully");
    }
    
}

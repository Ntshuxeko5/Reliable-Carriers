package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditService {
    
    // Core audit logging methods
    void logAction(String action, String entityType, Long entityId, String status, String additionalData);
    void logAction(String action, String entityType, Long entityId, String status, String oldValues, String newValues, String additionalData);
    void logAction(String action, String entityType, Long entityId, String status, String oldValues, String newValues, String additionalData, HttpServletRequest request);
    void logAction(String action, String entityType, Long entityId, String status, String oldValues, String newValues, String additionalData, HttpServletRequest request, Long executionTimeMs);
    
    // Log specific types of actions
    void logLogin(Long userId, String userEmail, String userRole, String status, String ipAddress, String userAgent);
    void logLogout(Long userId, String userEmail, String userRole, String ipAddress);
    void logFailedLogin(String email, String ipAddress, String userAgent, String reason);
    void logPasswordChange(Long userId, String userEmail, String userRole, String status);
    void logPasswordReset(Long userId, String userEmail, String userRole, String status);
    void logUserCreation(Long userId, String userEmail, String userRole, String createdBy);
    void logUserUpdate(Long userId, String userEmail, String userRole, String updatedBy, String changes);
    void logUserDeletion(Long userId, String userEmail, String userRole, String deletedBy);
    void logShipmentCreation(Long shipmentId, Long userId, String userEmail, String userRole);
    void logShipmentUpdate(Long shipmentId, Long userId, String userEmail, String userRole, String changes);
    void logShipmentStatusChange(Long shipmentId, String oldStatus, String newStatus, Long userId, String userEmail, String userRole);
    void logPayment(Long paymentId, Long userId, String userEmail, String userRole, String paymentMethod, Double amount, String status);
    void logDriverAssignment(Long shipmentId, Long driverId, Long assignedBy, String assignedByEmail, String assignedByRole);
    void logDeliveryConfirmation(Long shipmentId, Long driverId, String driverEmail, String driverRole);
    void logFileUpload(String fileName, String fileType, Long fileSize, Long userId, String userEmail, String userRole);
    void logDataExport(String exportType, Long userId, String userEmail, String userRole, String filters);
    void logDataImport(String importType, Long userId, String userEmail, String userRole, int recordCount);
    void logSystemError(String errorType, String errorMessage, String stackTrace, Long userId, String userEmail, String userRole);
    void logSecurityEvent(String eventType, String description, String ipAddress, Long userId, String userEmail, String userRole);
    
    // Retrieval methods
    List<AuditLog> getAuditLogsByUser(Long userId);
    List<AuditLog> getAuditLogsByUser(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable);
    
    List<AuditLog> getAuditLogsByRole(String userRole);
    List<AuditLog> getAuditLogsByRole(String userRole, LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> getAuditLogsByRole(String userRole, Pageable pageable);
    
    List<AuditLog> getAuditLogsByAction(String action);
    List<AuditLog> getAuditLogsByAction(String action, LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable);
    
    List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId);
    List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<AuditLog> getAuditLogsByStatus(String status);
    List<AuditLog> getAuditLogsByStatus(String status, LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> getAuditLogsByStatus(String status, Pageable pageable);
    
    List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    List<AuditLog> getAuditLogsByIpAddress(String ipAddress);
    List<AuditLog> getAuditLogsByIpAddress(String ipAddress, LocalDateTime startDate, LocalDateTime endDate);
    
    List<AuditLog> getRecentActivity(int hours);
    List<AuditLog> getFailedActions(LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> getSuspiciousActivity(LocalDateTime startDate, LocalDateTime endDate, Long threshold);
    
    // Analytics methods
    Long getAuditCountByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Long getAuditCountByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    Long getAuditCountByRoleAndDateRange(String userRole, LocalDateTime startDate, LocalDateTime endDate);
    Long getAuditCountByActionAndDateRange(String action, LocalDateTime startDate, LocalDateTime endDate);
    Long getAuditCountByStatusAndDateRange(String status, LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Long> getAuditCountByRole(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Long> getAuditCountByAction(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Long> getAuditCountByStatus(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Long> getAuditCountByEntityType(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Long> getDailyAuditCount(LocalDateTime startDate, LocalDateTime endDate);
    Map<String, Long> getTopUsersByAuditCount(LocalDateTime startDate, LocalDateTime endDate, int limit);
    Map<String, Long> getTopIpAddresses(LocalDateTime startDate, LocalDateTime endDate, int limit);
    
    Double getAverageExecutionTime(LocalDateTime startDate, LocalDateTime endDate);
    
    // Security and compliance methods
    List<AuditLog> getLoginAttempts(String email, LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> getFailedLoginAttempts(String email, LocalDateTime startDate, LocalDateTime endDate);
    Long getFailedLoginCount(String email, LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> getPasswordChangeHistory(Long userId);
    List<AuditLog> getDataAccessHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> getSensitiveDataAccess(String entityType, Long entityId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Export methods
    String exportAuditLogsToCsv(LocalDateTime startDate, LocalDateTime endDate, String filters);
    String exportAuditLogsToJson(LocalDateTime startDate, LocalDateTime endDate, String filters);
    byte[] exportAuditLogsToPdf(LocalDateTime startDate, LocalDateTime endDate, String filters);
    
    // Cleanup methods
    void deleteOldAuditLogs(LocalDateTime beforeDate);
    void archiveAuditLogs(LocalDateTime beforeDate);
    Long getAuditLogsSize();
    
    // Utility methods
    String getCurrentUserEmail();
    String getCurrentUserRole();
    Long getCurrentUserId();
    String getClientIpAddress(HttpServletRequest request);
    String getUserAgent(HttpServletRequest request);
    String getSessionId(HttpServletRequest request);
}

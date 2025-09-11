package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.AuditLog;
import com.reliablecarriers.Reliable.Carriers.repository.AuditLogRepository;
import com.reliablecarriers.Reliable.Carriers.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuditServiceImpl implements AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Core audit logging methods
    @Override
    public void logAction(String action, String entityType, Long entityId, String status, String additionalData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setStatus(status);
        auditLog.setAdditionalData(additionalData);
        
        // Set current user info
        setCurrentUserInfo(auditLog);
        
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logAction(String action, String entityType, Long entityId, String status, String oldValues, String newValues, String additionalData) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setStatus(status);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setAdditionalData(additionalData);
        
        setCurrentUserInfo(auditLog);
        
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logAction(String action, String entityType, Long entityId, String status, String oldValues, String newValues, String additionalData, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setStatus(status);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setAdditionalData(additionalData);
        
        setCurrentUserInfo(auditLog);
        setRequestInfo(auditLog, request);
        
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logAction(String action, String entityType, Long entityId, String status, String oldValues, String newValues, String additionalData, HttpServletRequest request, Long executionTimeMs) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setStatus(status);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setAdditionalData(additionalData);
        auditLog.setExecutionTimeMs(executionTimeMs);
        
        setCurrentUserInfo(auditLog);
        setRequestInfo(auditLog, request);
        
        auditLogRepository.save(auditLog);
    }
    
    // Specific action logging methods
    @Override
    public void logLogin(Long userId, String userEmail, String userRole, String status, String ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "LOGIN", "USER", userId);
        auditLog.setStatus(status);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logLogout(Long userId, String userEmail, String userRole, String ipAddress) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "LOGOUT", "USER", userId);
        auditLog.setStatus("SUCCESS");
        auditLog.setIpAddress(ipAddress);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logFailedLogin(String email, String ipAddress, String userAgent, String reason) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserEmail(email);
        auditLog.setAction("LOGIN");
        auditLog.setEntityType("USER");
        auditLog.setStatus("FAILED");
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        auditLog.setErrorMessage(reason);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logPasswordChange(Long userId, String userEmail, String userRole, String status) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "PASSWORD_CHANGE", "USER", userId);
        auditLog.setStatus(status);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logPasswordReset(Long userId, String userEmail, String userRole, String status) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "PASSWORD_RESET", "USER", userId);
        auditLog.setStatus(status);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logUserCreation(Long userId, String userEmail, String userRole, String createdBy) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUserEmail(userEmail);
        auditLog.setUserRole(userRole);
        auditLog.setAction("USER_CREATION");
        auditLog.setEntityType("USER");
        auditLog.setEntityId(userId);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("Created by: " + createdBy);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logUserUpdate(Long userId, String userEmail, String userRole, String updatedBy, String changes) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "USER_UPDATE", "USER", userId);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("Updated by: " + updatedBy + ", Changes: " + changes);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logUserDeletion(Long userId, String userEmail, String userRole, String deletedBy) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "USER_DELETION", "USER", userId);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("Deleted by: " + deletedBy);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logShipmentCreation(Long shipmentId, Long userId, String userEmail, String userRole) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "SHIPMENT_CREATION", "SHIPMENT", shipmentId);
        auditLog.setStatus("SUCCESS");
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logShipmentUpdate(Long shipmentId, Long userId, String userEmail, String userRole, String changes) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "SHIPMENT_UPDATE", "SHIPMENT", shipmentId);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("Changes: " + changes);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logShipmentStatusChange(Long shipmentId, String oldStatus, String newStatus, Long userId, String userEmail, String userRole) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "SHIPMENT_STATUS_CHANGE", "SHIPMENT", shipmentId);
        auditLog.setStatus("SUCCESS");
        auditLog.setOldValues(oldStatus);
        auditLog.setNewValues(newStatus);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logPayment(Long paymentId, Long userId, String userEmail, String userRole, String paymentMethod, Double amount, String status) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "PAYMENT", "PAYMENT", paymentId);
        auditLog.setStatus(status);
        auditLog.setAdditionalData("Method: " + paymentMethod + ", Amount: " + amount);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logDriverAssignment(Long shipmentId, Long driverId, Long assignedBy, String assignedByEmail, String assignedByRole) {
        AuditLog auditLog = new AuditLog(assignedBy, assignedByEmail, assignedByRole, "DRIVER_ASSIGNMENT", "SHIPMENT", shipmentId);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("Assigned driver ID: " + driverId);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logDeliveryConfirmation(Long shipmentId, Long driverId, String driverEmail, String driverRole) {
        AuditLog auditLog = new AuditLog(driverId, driverEmail, driverRole, "DELIVERY_CONFIRMATION", "SHIPMENT", shipmentId);
        auditLog.setStatus("SUCCESS");
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logFileUpload(String fileName, String fileType, Long fileSize, Long userId, String userEmail, String userRole) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "FILE_UPLOAD", "FILE", null);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("File: " + fileName + ", Type: " + fileType + ", Size: " + fileSize);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logDataExport(String exportType, Long userId, String userEmail, String userRole, String filters) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "DATA_EXPORT", "SYSTEM", null);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("Type: " + exportType + ", Filters: " + filters);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logDataImport(String importType, Long userId, String userEmail, String userRole, int recordCount) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "DATA_IMPORT", "SYSTEM", null);
        auditLog.setStatus("SUCCESS");
        auditLog.setAdditionalData("Type: " + importType + ", Records: " + recordCount);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logSystemError(String errorType, String errorMessage, String stackTrace, Long userId, String userEmail, String userRole) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "SYSTEM_ERROR", "SYSTEM", null);
        auditLog.setStatus("ERROR");
        auditLog.setErrorMessage(errorMessage);
        auditLog.setAdditionalData("Type: " + errorType + ", Stack: " + stackTrace);
        auditLogRepository.save(auditLog);
    }
    
    @Override
    public void logSecurityEvent(String eventType, String description, String ipAddress, Long userId, String userEmail, String userRole) {
        AuditLog auditLog = new AuditLog(userId, userEmail, userRole, "SECURITY_EVENT", "SECURITY", null);
        auditLog.setStatus("SUCCESS");
        auditLog.setIpAddress(ipAddress);
        auditLog.setAdditionalData("Event: " + eventType + ", Description: " + description);
        auditLogRepository.save(auditLog);
    }
    
    // Retrieval methods
    @Override
    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByUser(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
    }
    
    @Override
    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByRole(String userRole) {
        return auditLogRepository.findByUserRoleOrderByCreatedAtDesc(userRole);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByRole(String userRole, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByUserRoleAndCreatedAtBetweenOrderByCreatedAtDesc(userRole, startDate, endDate);
    }
    
    @Override
    public Page<AuditLog> getAuditLogsByRole(String userRole, Pageable pageable) {
        return auditLogRepository.findByUserRole(userRole, pageable);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByAction(String action, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByActionAndCreatedAtBetweenOrderByCreatedAtDesc(action, startDate, endDate);
    }
    
    @Override
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityIdOrderByCreatedAtDesc(entityId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByEntity(String entityType, Long entityId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByEntityIdOrderByCreatedAtDesc(entityId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByStatus(String status) {
        return auditLogRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByStatus(String status, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByStatusAndDateRange(status, startDate, endDate);
    }
    
    @Override
    public Page<AuditLog> getAuditLogsByStatus(String status, Pageable pageable) {
        return auditLogRepository.findByStatus(status, pageable);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }
    
    @Override
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByIpAddress(String ipAddress) {
        return auditLogRepository.findByIpAddressOrderByCreatedAtDesc(ipAddress);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByIpAddress(String ipAddress, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByIpAddressOrderByCreatedAtDesc(ipAddress);
    }
    
    @Override
    public List<AuditLog> getRecentActivity(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return auditLogRepository.findRecentActivity(since);
    }
    
    @Override
    public List<AuditLog> getFailedActions(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByStatusInOrderByCreatedAtDesc(List.of("FAILED", "ERROR"));
    }
    
    @Override
    public List<AuditLog> getSuspiciousActivity(LocalDateTime startDate, LocalDateTime endDate, Long threshold) {
        List<Object[]> results = auditLogRepository.findSuspiciousActivity(startDate, endDate, threshold);
        // Convert to AuditLog objects - simplified implementation
        return results.stream()
            .map(result -> {
                AuditLog log = new AuditLog();
                log.setUserId((Long) result[0]);
                log.setUserEmail((String) result[1]);
                return log;
            })
            .collect(Collectors.toList());
    }
    
    // Analytics methods
    @Override
    public Long getAuditCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.countByDateRange(startDate, endDate);
    }
    
    @Override
    public Long getAuditCountByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.countByUserAndDateRange(userId, startDate, endDate);
    }
    
    @Override
    public Long getAuditCountByRoleAndDateRange(String userRole, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.countByRoleAndDateRange(userRole, startDate, endDate);
    }
    
    @Override
    public Long getAuditCountByActionAndDateRange(String action, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.countByActionAndDateRange(action, startDate, endDate);
    }
    
    @Override
    public Long getAuditCountByStatusAndDateRange(String status, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.countByStatusAndDateRange(status, startDate, endDate);
    }
    
    @Override
    public Map<String, Long> getAuditCountByRole(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getAuditCountByRole(startDate, endDate);
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((String) result[0], (Long) result[1]);
        }
        return map;
    }
    
    @Override
    public Map<String, Long> getAuditCountByAction(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getAuditCountByAction(startDate, endDate);
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((String) result[0], (Long) result[1]);
        }
        return map;
    }
    
    @Override
    public Map<String, Long> getAuditCountByStatus(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getAuditCountByStatus(startDate, endDate);
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((String) result[0], (Long) result[1]);
        }
        return map;
    }
    
    @Override
    public Map<String, Long> getAuditCountByEntityType(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getAuditCountByEntityType(startDate, endDate);
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((String) result[0], (Long) result[1]);
        }
        return map;
    }
    
    @Override
    public Map<String, Long> getDailyAuditCount(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getDailyAuditCount(startDate, endDate);
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put(result[0].toString(), (Long) result[1]);
        }
        return map;
    }
    
    @Override
    public Map<String, Long> getTopUsersByAuditCount(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Object[]> results = auditLogRepository.getTopUsersByAuditCount(startDate, endDate);
        Map<String, Long> map = new HashMap<>();
        int count = 0;
        for (Object[] result : results) {
            if (count >= limit) break;
            map.put((String) result[1], (Long) result[2]);
            count++;
        }
        return map;
    }
    
    @Override
    public Map<String, Long> getTopIpAddresses(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<Object[]> results = auditLogRepository.getTopIpAddresses(startDate, endDate);
        Map<String, Long> map = new HashMap<>();
        int count = 0;
        for (Object[] result : results) {
            if (count >= limit) break;
            map.put((String) result[0], (Long) result[1]);
            count++;
        }
        return map;
    }
    
    @Override
    public Double getAverageExecutionTime(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.getAverageExecutionTime(startDate, endDate);
    }
    
    // Security and compliance methods
    @Override
    public List<AuditLog> getLoginAttempts(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByUserEmailAndCreatedAtBetweenOrderByCreatedAtDesc(email, startDate, endDate);
    }
    
    @Override
    public List<AuditLog> getFailedLoginAttempts(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByUserEmailAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(email, "FAILED", startDate, endDate);
    }
    
    @Override
    public Long getFailedLoginCount(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.countByUserEmailAndStatusAndCreatedAtBetween(email, "FAILED", startDate, endDate);
    }
    
    @Override
    public List<AuditLog> getPasswordChangeHistory(Long userId) {
        return auditLogRepository.findByUserIdAndActionOrderByCreatedAtDesc(userId, "PASSWORD_CHANGE");
    }
    
    @Override
    public List<AuditLog> getDataAccessHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
    }
    
    @Override
    public List<AuditLog> getSensitiveDataAccess(String entityType, Long entityId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByEntityTypeAndEntityIdAndCreatedAtBetweenOrderByCreatedAtDesc(entityType, entityId, startDate, endDate);
    }
    
    // Export methods (simplified implementations)
    @Override
    public String exportAuditLogsToCsv(LocalDateTime startDate, LocalDateTime endDate, String filters) {
        // Simplified CSV export
        List<AuditLog> logs = getAuditLogsByDateRange(startDate, endDate);
        StringBuilder csv = new StringBuilder("ID,User,Action,Entity,Status,Created\n");
        for (AuditLog log : logs) {
            csv.append(String.format("%d,%s,%s,%s,%s,%s\n", 
                log.getId(), log.getUserEmail(), log.getAction(), 
                log.getEntityType(), log.getStatus(), log.getCreatedAt()));
        }
        return csv.toString();
    }
    
    @Override
    public String exportAuditLogsToJson(LocalDateTime startDate, LocalDateTime endDate, String filters) {
        try {
            List<AuditLog> logs = getAuditLogsByDateRange(startDate, endDate);
            return objectMapper.writeValueAsString(logs);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    @Override
    public byte[] exportAuditLogsToPdf(LocalDateTime startDate, LocalDateTime endDate, String filters) {
        // Simplified PDF export - returns empty byte array
        return new byte[0];
    }
    
    // Cleanup methods
    @Override
    public void deleteOldAuditLogs(LocalDateTime beforeDate) {
        List<AuditLog> oldLogs = auditLogRepository.findByCreatedAtBefore(beforeDate);
        auditLogRepository.deleteAll(oldLogs);
    }
    
    @Override
    public void archiveAuditLogs(LocalDateTime beforeDate) {
        // Simplified archiving - just delete old logs
        deleteOldAuditLogs(beforeDate);
    }
    
    @Override
    public Long getAuditLogsSize() {
        return auditLogRepository.count();
    }
    
    // Utility methods
    @Override
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal()).getUsername();
        }
        return "SYSTEM";
    }
    
    @Override
    public String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            return auth.getAuthorities().iterator().next().getAuthority();
        }
        return "SYSTEM";
    }
    
    @Override
    public Long getCurrentUserId() {
        // Simplified - would need to extract from JWT or session
        return null;
    }
    
    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    @Override
    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
    
    @Override
    public String getSessionId(HttpServletRequest request) {
        return request.getSession().getId();
    }
    
    // Private helper methods
    private void setCurrentUserInfo(AuditLog auditLog) {
        auditLog.setUserEmail(getCurrentUserEmail());
        auditLog.setUserRole(getCurrentUserRole());
        auditLog.setUserId(getCurrentUserId());
    }
    
    private void setRequestInfo(AuditLog auditLog, HttpServletRequest request) {
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(getUserAgent(request));
            auditLog.setSessionId(getSessionId(request));
        }
    }
}

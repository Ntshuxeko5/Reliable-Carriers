package com.reliablecarriers.Reliable.Carriers.controller;

import com.reliablecarriers.Reliable.Carriers.model.AuditLog;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.service.AuthService;
import com.reliablecarriers.Reliable.Carriers.service.LoginHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/login-history")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "*")
public class CustomerLoginHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerLoginHistoryController.class);

    @Autowired
    private LoginHistoryService loginHistoryService;

    @Autowired
    private AuthService authService;

    /**
     * Get login history for current customer
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getLoginHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            List<AuditLog> loginHistory;
            if (startDate != null && endDate != null) {
                loginHistory = loginHistoryService.getLoginHistory(currentUser, startDate, endDate);
            } else {
                loginHistory = loginHistoryService.getLoginHistory(currentUser, limit);
            }

            List<Map<String, Object>> historyDTOs = loginHistory.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "loginHistory", historyDTOs,
                "total", historyDTOs.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching login history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch login history: " + e.getMessage()));
        }
    }

    /**
     * Get login statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLoginStatistics() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User not authenticated"));
            }

            Map<String, Object> stats = loginHistoryService.getLoginStatistics(currentUser);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "statistics", stats
            ));
        } catch (Exception e) {
            logger.error("Error fetching login statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to fetch login statistics: " + e.getMessage()));
        }
    }

    /**
     * Convert AuditLog to DTO
     */
    private Map<String, Object> convertToDTO(AuditLog auditLog) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", auditLog.getId());
        dto.put("status", auditLog.getStatus());
        dto.put("ipAddress", auditLog.getIpAddress());
        dto.put("userAgent", auditLog.getUserAgent());
        dto.put("sessionId", auditLog.getSessionId());
        dto.put("createdAt", auditLog.getCreatedAt());
        dto.put("errorMessage", auditLog.getErrorMessage());
        
        // Parse device info from user agent
        if (auditLog.getUserAgent() != null) {
            String ua = auditLog.getUserAgent().toLowerCase();
            if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
                dto.put("deviceType", "Mobile");
            } else if (ua.contains("tablet") || ua.contains("ipad")) {
                dto.put("deviceType", "Tablet");
            } else {
                dto.put("deviceType", "Desktop");
            }
            
            // Parse browser
            if (ua.contains("chrome") && !ua.contains("edg")) {
                dto.put("browser", "Chrome");
            } else if (ua.contains("firefox")) {
                dto.put("browser", "Firefox");
            } else if (ua.contains("safari") && !ua.contains("chrome")) {
                dto.put("browser", "Safari");
            } else if (ua.contains("edg")) {
                dto.put("browser", "Edge");
            } else {
                dto.put("browser", "Unknown");
            }
        } else {
            dto.put("deviceType", "Unknown");
            dto.put("browser", "Unknown");
        }
        
        return dto;
    }
}


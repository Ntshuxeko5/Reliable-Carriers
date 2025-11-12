package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.reliablecarriers.Reliable.Carriers.model.AuditLog;
import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.AuditLogRepository;
import com.reliablecarriers.Reliable.Carriers.service.LoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoginHistoryServiceImpl implements LoginHistoryService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public List<AuditLog> getLoginHistory(User user, int limit) {
        return auditLogRepository.findByUserIdAndActionOrderByCreatedAtDesc(user.getId(), "LOGIN")
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public List<AuditLog> getLoginHistory(User user, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByUserIdAndActionAndCreatedAtBetweenOrderByCreatedAtDesc(
            user.getId(), "LOGIN", startDate, endDate);
    }

    @Override
    public Map<String, Object> getLoginStatistics(User user) {
        List<AuditLog> allLogins = auditLogRepository.findByUserIdAndActionOrderByCreatedAtDesc(user.getId(), "LOGIN");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogins", allLogins.size());
        stats.put("successfulLogins", allLogins.stream()
            .filter(log -> "SUCCESS".equals(log.getStatus()))
            .count());
        stats.put("failedLogins", allLogins.stream()
            .filter(log -> "FAILED".equals(log.getStatus()))
            .count());
        
        if (!allLogins.isEmpty()) {
            AuditLog lastLogin = allLogins.get(0);
            stats.put("lastLoginDate", lastLogin.getCreatedAt());
            stats.put("lastLoginIp", lastLogin.getIpAddress());
            stats.put("lastLoginUserAgent", lastLogin.getUserAgent());
        }
        
        // Count logins by device/browser (simple user agent parsing)
        Map<String, Long> deviceCounts = allLogins.stream()
            .filter(log -> log.getUserAgent() != null)
            .collect(Collectors.groupingBy(
                log -> {
                    String ua = log.getUserAgent().toLowerCase();
                    if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
                        return "Mobile";
                    } else if (ua.contains("tablet") || ua.contains("ipad")) {
                        return "Tablet";
                    } else {
                        return "Desktop";
                    }
                },
                Collectors.counting()
            ));
        stats.put("deviceBreakdown", deviceCounts);
        
        return stats;
    }

    @Override
    public List<AuditLog> getRecentLoginAttempts(User user, int limit) {
        return auditLogRepository.findByUserIdAndActionOrderByCreatedAtDesc(user.getId(), "LOGIN")
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}


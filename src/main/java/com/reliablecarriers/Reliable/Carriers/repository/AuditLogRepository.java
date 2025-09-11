package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find by user
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    
    // Find by user role
    List<AuditLog> findByUserRoleOrderByCreatedAtDesc(String userRole);
    Page<AuditLog> findByUserRole(String userRole, Pageable pageable);
    
    // Find by action
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    // Find by entity type
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);
    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);
    
    // Find by entity ID
    List<AuditLog> findByEntityIdOrderByCreatedAtDesc(Long entityId);
    
    // Find by status
    List<AuditLog> findByStatusOrderByCreatedAtDesc(String status);
    Page<AuditLog> findByStatus(String status, Pageable pageable);
    
    // Find by date range
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find by user and date range
    List<AuditLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by user role and date range
    List<AuditLog> findByUserRoleAndCreatedAtBetweenOrderByCreatedAtDesc(String userRole, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by action and date range
    List<AuditLog> findByActionAndCreatedAtBetweenOrderByCreatedAtDesc(String action, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find by IP address
    List<AuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);
    
    // Find by session ID
    List<AuditLog> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    // Find failed actions
    List<AuditLog> findByStatusInOrderByCreatedAtDesc(List<String> statuses);
    
    // Additional methods for AuditServiceImpl
    @Query("SELECT a FROM AuditLog a WHERE a.status = :status AND a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByStatusAndDateRange(@Param("status") String status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    List<AuditLog> findByUserEmailAndCreatedAtBetweenOrderByCreatedAtDesc(String userEmail, LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> findByUserEmailAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(String userEmail, String status, LocalDateTime startDate, LocalDateTime endDate);
    Long countByUserEmailAndStatusAndCreatedAtBetween(String userEmail, String status, LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> findByUserIdAndActionOrderByCreatedAtDesc(Long userId, String action);
    List<AuditLog> findByEntityTypeAndEntityIdAndCreatedAtBetweenOrderByCreatedAtDesc(String entityType, Long entityId, LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> findByCreatedAtBefore(LocalDateTime beforeDate);
    
    // Analytics queries
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userRole = :userRole AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countByRoleAndDateRange(@Param("userRole") String userRole, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = :action AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countByActionAndDateRange(@Param("action") String action, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.status = :status AND a.createdAt BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") String status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a.userRole, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.userRole")
    List<Object[]> getAuditCountByRole(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> getAuditCountByAction(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a.status, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.status")
    List<Object[]> getAuditCountByStatus(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a.entityType, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.entityType ORDER BY COUNT(a) DESC")
    List<Object[]> getAuditCountByEntityType(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
    List<Object[]> getDailyAuditCount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a.userId, a.userEmail, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.userId, a.userEmail ORDER BY COUNT(a) DESC")
    List<Object[]> getTopUsersByAuditCount(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(a.executionTimeMs) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate AND a.executionTimeMs IS NOT NULL")
    Double getAverageExecutionTime(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a.ipAddress, COUNT(a) FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.ipAddress ORDER BY COUNT(a) DESC")
    List<Object[]> getTopIpAddresses(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find recent activity
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<AuditLog> findRecentActivity(@Param("since") LocalDateTime since);
    
    // Find suspicious activity (multiple failed attempts)
    @Query("SELECT a.userId, a.userEmail, COUNT(a) FROM AuditLog a WHERE a.status = 'FAILED' AND a.createdAt BETWEEN :startDate AND :endDate GROUP BY a.userId, a.userEmail HAVING COUNT(a) > :threshold")
    List<Object[]> findSuspiciousActivity(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("threshold") Long threshold);
}

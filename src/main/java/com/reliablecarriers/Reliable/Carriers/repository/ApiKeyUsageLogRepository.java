package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.ApiKeyUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ApiKeyUsageLogRepository extends JpaRepository<ApiKeyUsageLog, Long> {
    
    /**
     * Count requests in the last hour
     */
    @Query("SELECT COUNT(l) FROM ApiKeyUsageLog l WHERE l.apiKeyHash = :apiKeyHash AND l.createdAt >= :since")
    long countByApiKeyHashSince(@Param("apiKeyHash") String apiKeyHash, @Param("since") Date since);
    
    /**
     * Get recent usage logs
     */
    List<ApiKeyUsageLog> findByApiKeyHashOrderByCreatedAtDesc(String apiKeyHash);
    
    /**
     * Get usage logs within time range
     */
    @Query("SELECT l FROM ApiKeyUsageLog l WHERE l.apiKeyHash = :apiKeyHash AND l.createdAt BETWEEN :start AND :end ORDER BY l.createdAt DESC")
    List<ApiKeyUsageLog> findByApiKeyHashAndCreatedAtBetween(
        @Param("apiKeyHash") String apiKeyHash,
        @Param("start") Date start,
        @Param("end") Date end
    );
}






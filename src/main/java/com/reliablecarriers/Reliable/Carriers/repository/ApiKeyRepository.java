package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.ApiKey;
import com.reliablecarriers.Reliable.Carriers.model.ApiKeyStatus;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    
    Optional<ApiKey> findByApiKeyHash(String apiKeyHash);
    
    List<ApiKey> findByUser(User user);
    
    List<ApiKey> findByUserAndStatus(User user, ApiKeyStatus status);
    
    boolean existsByApiKeyHash(String apiKeyHash);
    
    long countByUser(User user);
}


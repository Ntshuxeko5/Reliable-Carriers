package com.reliablecarriers.Reliable.Carriers.repository;

import com.reliablecarriers.Reliable.Carriers.model.TwoFactorToken;
import com.reliablecarriers.Reliable.Carriers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface TwoFactorTokenRepository extends JpaRepository<TwoFactorToken, Long> {
    Optional<TwoFactorToken> findByUserAndTokenAndUsedFalse(User user, String token);
    void deleteByUser(User user);
    java.util.List<TwoFactorToken> findByUserAndUsedFalseOrderByExpiresAtDesc(User user);
    
    // Find valid token (unused and not expired) in a single query
    @Query("SELECT t FROM TwoFactorToken t WHERE t.user = :user AND t.token = :token AND t.used = false AND t.expiresAt > :now")
    Optional<TwoFactorToken> findValidTokenForUser(@Param("user") User user, @Param("token") String token, @Param("now") Date now);
    
    // Find used token that matches (for handling duplicate requests)
    @Query("SELECT t FROM TwoFactorToken t WHERE t.user = :user AND t.token = :token AND t.used = true AND t.expiresAt > :now ORDER BY t.expiresAt DESC")
    Optional<TwoFactorToken> findUsedButValidToken(@Param("user") User user, @Param("token") String token, @Param("now") Date now);
}
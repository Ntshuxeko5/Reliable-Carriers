package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling account lockout after failed login attempts
 */
@Service
public class AccountLockoutService {
    
    @Autowired
    private UserRepository userRepository;
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 30;
    
    /**
     * Record a failed login attempt
     */
    @Transactional
    public void recordFailedLoginAttempt(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return; // Don't reveal if user exists
        }
        
        Integer attempts = user.getFailedLoginAttempts();
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;
        
        user.setFailedLoginAttempts(attempts);
        
        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setAccountLockedUntil(new Date(System.currentTimeMillis() + 
                TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES)));
        }
        
        userRepository.save(user);
    }
    
    /**
     * Clear failed login attempts after successful login
     */
    @Transactional
    public void clearFailedLoginAttempts(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        }
    }
    
    /**
     * Check if account is locked
     */
    public boolean isAccountLocked(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !Boolean.TRUE.equals(user.getAccountLocked())) {
            return false;
        }
        
        // Check if lockout period has expired
        if (user.getAccountLockedUntil() != null && 
            user.getAccountLockedUntil().before(new Date())) {
            // Auto-unlock expired lockouts
            user.setAccountLocked(false);
            user.setAccountLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get remaining lockout time in minutes
     */
    public long getRemainingLockoutMinutes(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getAccountLockedUntil() == null) {
            return 0;
        }
        
        long remaining = user.getAccountLockedUntil().getTime() - System.currentTimeMillis();
        return Math.max(0, TimeUnit.MILLISECONDS.toMinutes(remaining));
    }
}

package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;

public interface AuthService {
    
    User registerUser(User user);
    
    User authenticateUser(String email, String password);
    
    User getCurrentUser();
    
    boolean hasRole(UserRole role);
    
    boolean isAuthenticated();
    
    /**
     * Checks if the provided user ID matches the currently authenticated user
     * @param userId The user ID to check
     * @return true if the current user's ID matches the provided ID, false otherwise
     */
    boolean isCurrentUser(Long userId);
}
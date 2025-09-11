package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;

import java.util.List;

public interface UserService {
    
    User createUser(User user);
    
    User updateUser(Long id, User user);
    
    User getUserById(Long id);
    
    User getUserByEmail(String email);
    
    List<User> getAllUsers();
    
    List<User> getUsersByRole(UserRole role);
    
    void deleteUser(Long id);
    
    boolean existsByEmail(String email);
    
    List<User> searchUsers(String searchTerm);
}
package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserValidationService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Validate user registration data
     */
    public Map<String, Object> validateRegistration(String firstName, String lastName, String email, 
                                                   String password, String confirmPassword, String phone) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        Map<String, String> errors = new HashMap<>();
        result.put("errors", errors);
        
        // Validate first name
        if (firstName == null || firstName.trim().isEmpty()) {
            errors.put("firstName", "First name is required");
        } else if (firstName.trim().length() < 2) {
            errors.put("firstName", "First name must be at least 2 characters");
        }
        
        // Validate last name
        if (lastName == null || lastName.trim().isEmpty()) {
            errors.put("lastName", "Last name is required");
        } else if (lastName.trim().length() < 2) {
            errors.put("lastName", "Last name must be at least 2 characters");
        }
        
        // Validate email
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!isValidEmail(email)) {
            errors.put("email", "Please enter a valid email address");
        } else if (userRepository.existsByEmail(email)) {
            errors.put("email", "This email is already registered");
        }
        
        // Validate password
        if (password == null || password.isEmpty()) {
            errors.put("password", "Password is required");
        } else if (password.length() < 6) {
            errors.put("password", "Password must be at least 6 characters");
        }
        
        // Validate password confirmation
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            errors.put("confirmPassword", "Please confirm your password");
        } else if (password != null && !password.equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match");
        }
        
        // Validate phone
        if (phone == null || phone.trim().isEmpty()) {
            errors.put("phone", "Phone number is required");
        } else if (!isValidPhone(phone)) {
            errors.put("phone", "Please enter a valid phone number");
        } else if (userRepository.existsByPhone(phone)) {
            errors.put("phone", "This phone number is already registered");
        }
        
        // Set valid flag
        result.put("valid", errors.isEmpty());
        
        return result;
    }
    
    /**
     * Validate login credentials
     */
    public Map<String, Object> validateLogin(String identifier, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        Map<String, String> errors = new HashMap<>();
        result.put("errors", errors);
        
        // Validate identifier (email or phone)
        if (identifier == null || identifier.trim().isEmpty()) {
            errors.put("identifier", "Email or phone number is required");
        }
        
        // Validate password
        if (password == null || password.isEmpty()) {
            errors.put("password", "Password is required");
        }
        
        // Check if user exists
        if (errors.isEmpty() && identifier != null) {
            Optional<User> user = Optional.empty();
            
            if (identifier.contains("@")) {
                // Email login
                user = userRepository.findByEmail(identifier);
            } else {
                // Phone login
                user = userRepository.findByPhone(identifier);
            }
            
            if (user.isEmpty()) {
                if (identifier.contains("@")) {
                    errors.put("identifier", "No account found with this email address");
                } else {
                    errors.put("identifier", "No account found with this phone number");
                }
            }
        }
        
        // Set valid flag
        result.put("valid", errors.isEmpty());
        
        return result;
    }
    
    /**
     * Check if email is valid
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Check if phone is valid
     */
    private boolean isValidPhone(String phone) {
        // Remove all non-digit characters
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        // Check if it's between 10 and 15 digits
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }
    
    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Check if phone already exists
     */
    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }
    
    /**
     * Get user-friendly error message for authentication
     */
    public String getAuthenticationErrorMessage(String identifier, String password) {
        Optional<User> user = Optional.empty();
        
        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier);
        } else {
            user = userRepository.findByPhone(identifier);
        }
        
        if (user.isEmpty()) {
            if (identifier.contains("@")) {
                return "No account found with this email address";
            } else {
                return "No account found with this phone number";
            }
        } else {
            return "Incorrect password";
        }
    }
}

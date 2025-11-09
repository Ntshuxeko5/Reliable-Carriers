package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.model.User;
import com.reliablecarriers.Reliable.Carriers.model.UserRole;
import com.reliablecarriers.Reliable.Carriers.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, @Lazy AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public User registerUser(User user) {
        logger.debug("Starting user registration for email: " + user.getEmail());
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("Registration attempt with existing email: " + user.getEmail());
            throw new IllegalArgumentException("Email already in use: " + user.getEmail());
        }

        // Encode password if not already a BCrypt hash
        String rawOrHashedPassword = user.getPassword();
        if (rawOrHashedPassword == null || rawOrHashedPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        boolean looksLikeBCrypt = rawOrHashedPassword.startsWith("$2a$")
                || rawOrHashedPassword.startsWith("$2b$")
                || rawOrHashedPassword.startsWith("$2y$");
        if (!looksLikeBCrypt) {
            String encodedPassword = passwordEncoder.encode(rawOrHashedPassword);
            user.setPassword(encodedPassword);
            logger.debug("Password encoded for user: " + user.getEmail());
        } else {
            logger.debug("Password appears already encoded for user: " + user.getEmail());
        }

        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole(UserRole.CUSTOMER);
        }

        // Set timestamps
        Date now = new Date();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        user.setUpdatedAt(now);
        
        // Ensure all required fields are set
        if (user.getCountry() == null || user.getCountry().trim().isEmpty()) {
            user.setCountry("South Africa"); // Default country
        }

        // Save user and flush to ensure it's immediately available
        try {
            User savedUser = userRepository.save(user);
            userRepository.flush(); // Ensure the user is immediately available in the database
            
            logger.info("User successfully saved to database with ID: " + savedUser.getId() + 
                       ", Email: " + savedUser.getEmail() + 
                       ", Role: " + savedUser.getRole());
            
            // Verify the user was actually saved by retrieving it
            User verifiedUser = userRepository.findById(savedUser.getId())
                .orElseThrow(() -> new RuntimeException("User was not saved properly - ID: " + savedUser.getId()));
            
            logger.debug("User verification successful - retrieved from database: " + verifiedUser.getEmail());
            
            return verifiedUser;
        } catch (Exception e) {
            logger.error("Failed to save user to database: " + e.getMessage(), e);
            throw new RuntimeException("Failed to save user registration: " + e.getMessage(), e);
        }
    }

    @Override
    public User authenticateUser(String email, String password) {
        try {
            // Use AuthenticationManager to authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            
            // Set the authentication in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Return the user object
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                    authentication instanceof AnonymousAuthenticationToken) {
                return null;
            }

            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                return null;
            }
            
            return userRepository.findByEmail(email)
                    .orElse(null); // Return null instead of throwing exception to prevent 500 errors
        } catch (Exception e) {
            // Log error but return null to prevent 500 errors on public pages like login/register
            return null;
        }
    }

    @Override
    public boolean hasRole(UserRole role) {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == role;
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
                !(authentication instanceof AnonymousAuthenticationToken);
    }
    
    @Override
    public boolean isCurrentUser(Long userId) {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getId().equals(userId);
    }


}
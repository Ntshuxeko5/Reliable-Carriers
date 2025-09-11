package com.reliablecarriers.Reliable.Carriers.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTestUtil {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "admin123";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("Original password: " + password);
        System.out.println("Hashed password: " + hashedPassword);
        System.out.println("Matches: " + encoder.matches(password, hashedPassword));
        
        // Test with the existing hash from database
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
        System.out.println("Existing hash matches: " + encoder.matches(password, existingHash));
    }
}

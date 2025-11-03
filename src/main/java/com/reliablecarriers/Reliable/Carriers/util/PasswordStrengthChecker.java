package com.reliablecarriers.Reliable.Carriers.util;

import java.util.regex.Pattern;

/**
 * Utility class for checking password strength
 */
public class PasswordStrengthChecker {
    
    private static final Pattern LOWER_CASE = Pattern.compile("[a-z]");
    private static final Pattern UPPER_CASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    public enum Strength {
        WEAK, FAIR, GOOD, STRONG
    }
    
    public static Strength checkStrength(String password) {
        if (password == null || password.length() < 8) {
            return Strength.WEAK;
        }
        
        int score = 0;
        
        // Length checks
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character type checks
        if (LOWER_CASE.matcher(password).find()) score++;
        if (UPPER_CASE.matcher(password).find()) score++;
        if (DIGIT.matcher(password).find()) score++;
        if (SPECIAL_CHAR.matcher(password).find()) score++;
        
        // Determine strength
        if (score <= 2) {
            return Strength.WEAK;
        } else if (score <= 4) {
            return Strength.FAIR;
        } else if (score <= 6) {
            return Strength.GOOD;
        } else {
            return Strength.STRONG;
        }
    }
    
    public static String getStrengthMessage(Strength strength) {
        switch (strength) {
            case WEAK:
                return "Password is too weak. Use at least 8 characters with uppercase, lowercase, numbers, and special characters.";
            case FAIR:
                return "Password strength is fair. Consider adding more characters or special characters.";
            case GOOD:
                return "Password strength is good.";
            case STRONG:
                return "Password strength is strong.";
            default:
                return "Invalid password strength.";
        }
    }
}

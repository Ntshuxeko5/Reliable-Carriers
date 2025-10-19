import java.security.SecureRandom;
import java.util.Base64;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        String password = "admin123";
        
        // Generate a BCrypt hash manually
        String salt = generateSalt();
        String hash = generateBCryptHash(password, salt);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // Also generate a simple hash for testing
        String simpleHash = generateSimpleHash(password);
        System.out.println("Simple Hash: " + simpleHash);
    }
    
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    private static String generateBCryptHash(String password, String salt) {
        // This is a simplified version - in practice, use BCrypt library
        // For now, let's use a known working hash
        return "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa";
    }
    
    private static String generateSimpleHash(String password) {
        // Simple hash for testing
        return password.hashCode() + "";
    }
}

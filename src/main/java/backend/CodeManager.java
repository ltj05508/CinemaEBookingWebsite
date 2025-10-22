package backend;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Manages verification codes and reset tokens in memory.
 * Handles email verification codes (6-digit) and password reset tokens.
 */
public class CodeManager {
    
    // Store verification codes: code -> CodeData
    private static Map<String, CodeData> verificationCodes = new HashMap<>();
    
    // Store reset tokens: token -> CodeData
    private static Map<String, CodeData> resetTokens = new HashMap<>();
    
    private static final Random random = new Random();
    
    /**
     * Inner class to store code/token data with email and expiry time.
     */
    private static class CodeData {
        String email;
        LocalDateTime expiry;
        
        CodeData(String email, LocalDateTime expiry) {
            this.email = email;
            this.expiry = expiry;
        }
        
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }
    
    /**
     * Generate a 6-digit verification code for email confirmation.
     * Code expires in 24 hours.
     * @param email User's email address
     * @return 6-digit verification code (e.g., "123456")
     */
    public static String generateVerificationCode(String email) {
        // Generate random 6-digit code
        String code = String.format("%06d", random.nextInt(1000000));
        
        // Store with 24-hour expiry
        LocalDateTime expiry = LocalDateTime.now().plusHours(24);
        verificationCodes.put(code, new CodeData(email, expiry));
        
        // Clean up expired codes
        cleanExpiredCodes();
        
        return code;
    }
    
    /**
     * Validate a verification code and return the associated email.
     * Removes the code after successful validation (one-time use).
     * @param code The 6-digit verification code
     * @return Email address if code is valid, null if invalid or expired
     */
    public static String validateVerificationCode(String code) {
        CodeData data = verificationCodes.get(code);
        
        if (data == null) {
            return null; // Code doesn't exist
        }
        
        if (data.isExpired()) {
            verificationCodes.remove(code); // Remove expired code
            return null;
        }
        
        // Valid code - remove it (one-time use) and return email
        verificationCodes.remove(code);
        return data.email;
    }
    
    /**
     * Generate a secure random token for password reset.
     * Token expires in 1 hour.
     * @param email User's email address
     * @return Random token string (UUID format)
     */
    public static String generateResetToken(String email) {
        // Generate random UUID token
        String token = UUID.randomUUID().toString();
        
        // Store with 1-hour expiry
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        resetTokens.put(token, new CodeData(email, expiry));
        
        // Clean up expired tokens
        cleanExpiredTokens();
        
        return token;
    }
    
    /**
     * Validate a password reset token and return the associated email.
     * Removes the token after successful validation (one-time use).
     * @param token The reset token
     * @return Email address if token is valid, null if invalid or expired
     */
    public static String validateResetToken(String token) {
        CodeData data = resetTokens.get(token);
        
        if (data == null) {
            return null; // Token doesn't exist
        }
        
        if (data.isExpired()) {
            resetTokens.remove(token); // Remove expired token
            return null;
        }
        
        // Valid token - remove it (one-time use) and return email
        resetTokens.remove(token);
        return data.email;
    }
    
    /**
     * Remove expired verification codes to prevent memory buildup.
     */
    private static void cleanExpiredCodes() {
        verificationCodes.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Remove expired reset tokens to prevent memory buildup.
     */
    private static void cleanExpiredTokens() {
        resetTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Clear all codes and tokens 
     */
    public static void clearAll() {
        verificationCodes.clear();
        resetTokens.clear();
    }
}

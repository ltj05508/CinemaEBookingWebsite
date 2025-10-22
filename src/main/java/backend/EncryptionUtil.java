package backend;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for AES encryption/decryption for payment info
 */
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static String encryptionKey;
    
    /**
     * Sets the encryption key. 
     * Key must be 16, 24, or 32 bytes for AES.
     */
    public static void setEncryptionKey(String key) {
        encryptionKey = key;
    }
    
    /**
     * Encrypt a string value using AES.
     * @param value The plain text to encrypt
     * @return Base64-encoded encrypted string
     * @throws Exception if encryption fails
     */
    public static String encrypt(String value) throws Exception {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] encryptedBytes = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    /**
     * Decrypt a Base64-encoded encrypted string 
     * @param encryptedValue The Base64-encoded encrypted string
     * @return Decrypted plain text
     * @throws Exception if decryption fails
     */
    public static String decrypt(String encryptedValue) throws Exception {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedValue);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
}

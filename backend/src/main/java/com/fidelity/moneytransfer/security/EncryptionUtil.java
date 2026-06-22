package com.fidelity.moneytransfer.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    // 256-bit AES key for encryption
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    // Encryption key (in production, load from secure vault)
    private final SecretKey secretKey;

    public EncryptionUtil() {
        this.secretKey = generateOrLoadKey();
    }

    /**
     * Generate a 256-bit AES key
     */
    private SecretKey generateOrLoadKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    /**
     * Encrypt a plaintext string
     */
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a Base64-encoded ciphertext string
     */
    public String decrypt(String ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(ciphertext);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * Encrypt a numeric value (account ID, amount, etc.)
     */
    public String encryptLong(Long value) {
        return encrypt(value.toString());
    }

    /**
     * Decrypt and return a Long value
     */
    public Long decryptLong(String ciphertext) {
        try {
            String decrypted = decrypt(ciphertext);
            return Long.parseLong(decrypted);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Decrypted value is not a valid number", e);
        }
    }
}

package com.fidelity.moneytransfer.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EncryptionUtil Unit Tests")
class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil();
    }

    @Test
    @DisplayName("Should encrypt and decrypt string values correctly")
    void testStringEncryptionDecryption() {
        String plaintext = "sensitive data";
        
        String encrypted = encryptionUtil.encrypt(plaintext);
        
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertTrue(encrypted.length() > 0);
        
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should encrypt and decrypt long values correctly")
    void testLongEncryptionDecryption() {
        Long originalValue = 12345L;
        
        String encrypted = encryptionUtil.encryptLong(originalValue);
        
        assertNotNull(encrypted);
        assertNotEquals(originalValue.toString(), encrypted);
        
        Long decrypted = encryptionUtil.decryptLong(encrypted);
        
        assertEquals(originalValue, decrypted);
    }

    @Test
    @DisplayName("Should produce different ciphertexts for same plaintext (due to random IV)")
    void testNonDeterministicEncryption() {
        String plaintext = "test value";
        
        String encrypted1 = encryptionUtil.encrypt(plaintext);
        String encrypted2 = encryptionUtil.encrypt(plaintext);
        
        // Due to random IV, ciphertexts should be different
        assertNotEquals(encrypted1, encrypted2);
        
        // But both should decrypt to the same value
        assertEquals(plaintext, encryptionUtil.decrypt(encrypted1));
        assertEquals(plaintext, encryptionUtil.decrypt(encrypted2));
    }

    @Test
    @DisplayName("Should handle edge case: empty string")
    void testEmptyStringEncryption() {
        String plaintext = "";
        
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should handle edge case: zero long value")
    void testZeroLongEncryption() {
        Long originalValue = 0L;
        
        String encrypted = encryptionUtil.encryptLong(originalValue);
        Long decrypted = encryptionUtil.decryptLong(encrypted);
        
        assertEquals(originalValue, decrypted);
    }

    @Test
    @DisplayName("Should handle edge case: negative long value")
    void testNegativeLongEncryption() {
        Long originalValue = -999L;
        
        String encrypted = encryptionUtil.encryptLong(originalValue);
        Long decrypted = encryptionUtil.decryptLong(encrypted);
        
        assertEquals(originalValue, decrypted);
    }

    @Test
    @DisplayName("Should handle large numbers")
    void testLargeLongEncryption() {
        Long originalValue = Long.MAX_VALUE;
        
        String encrypted = encryptionUtil.encryptLong(originalValue);
        Long decrypted = encryptionUtil.decryptLong(encrypted);
        
        assertEquals(originalValue, decrypted);
    }

    @Test
    @DisplayName("Should throw exception on invalid ciphertext")
    void testDecryptionWithInvalidInput() {
        String invalidCiphertext = "not-a-valid-base64!@#$";
        
        assertThrows(Exception.class, () -> {
            encryptionUtil.decrypt(invalidCiphertext);
        });
    }

    @Test
    @DisplayName("Should handle special characters in plaintext")
    void testSpecialCharactersEncryption() {
        String plaintext = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
        
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should handle unicode characters in plaintext")
    void testUnicodeEncryption() {
        String plaintext = "Hello 世界 مرحبا Привет";
        
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testLongStringEncryption() {
        String plaintext = "x".repeat(10000);
        
        String encrypted = encryptionUtil.encrypt(plaintext);
        String decrypted = encryptionUtil.decrypt(encrypted);
        
        assertEquals(plaintext, decrypted);
    }
}

package com.example.superMalle.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES Encryption Utility for sensitive data encryption
 * 
 * Uses AES-GCM (Galois/Counter Mode) for authenticated encryption
 * Provides both encryption and decryption capabilities
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
@Component
@Slf4j
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256; // 256-bit key
    private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag
    private static final int GCM_IV_LENGTH = 12; // 12-byte IV for GCM

    @Value("${encryption.key:}")
    private String encryptionKey;

    private SecretKey secretKey;

    /**
     * Initialize the encryption key
     * Uses the configured key or generates a new one
     */
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            if (encryptionKey != null && !encryptionKey.isEmpty()) {
                // Use configured key
                byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);
                secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
                log.info("Using configured encryption key");
            } else {
                // Generate new key (for development only)
                log.warn("No encryption key configured, generating temporary key. NOT FOR PRODUCTION!");
                try {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
                    keyGenerator.init(KEY_SIZE, new SecureRandom());
                    secretKey = keyGenerator.generateKey();
                    log.info("Generated temporary encryption key: {}", 
                            Base64.getEncoder().encodeToString(secretKey.getEncoded()));
                } catch (Exception e) {
                    log.error("Failed to generate encryption key", e);
                    throw new RuntimeException("Failed to initialize encryption", e);
                }
            }
        }
        return secretKey;
    }

    /**
     * Encrypt plaintext using AES-GCM
     * 
     * @param plaintext The plaintext to encrypt
     * @return Base64-encoded encrypted data (IV + ciphertext + tag)
     * @throws Exception if encryption fails
     */
    public String encrypt(String plaintext) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            SecretKey key = getSecretKey();
            
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            
            // Encrypt
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            // Return Base64-encoded result
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Encryption failed for plaintext: {}", 
                    plaintext.substring(0, Math.min(plaintext.length(), 20)), e);
            throw new Exception("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypt ciphertext using AES-GCM
     * 
     * @param encryptedText Base64-encoded encrypted data (IV + ciphertext + tag)
     * @return Decrypted plaintext
     * @throws Exception if decryption fails
     */
    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            SecretKey key = getSecretKey();
            
            // Decode Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            // Decrypt
            byte[] plaintext = cipher.doFinal(ciphertext);
            
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for encrypted text", e);
            throw new Exception("Failed to decrypt data", e);
        }
    }

    /**
     * Generate a new encryption key
     * 
     * @return Base64-encoded key
     * @throws Exception if key generation fails
     */
    public static String generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, new SecureRandom());
        SecretKey key = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Validate if a key is valid
     * 
     * @param key Base64-encoded key
     * @return true if valid, false otherwise
     */
    public static boolean isValidKey(String key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            return keyBytes.length == KEY_SIZE / 8;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mask sensitive data for logging
     * 
     * @param data The data to mask
     * @return Masked data (e.g., "****1234")
     */
    public static String mask(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        if (data.length() <= 4) {
            return "****";
        }
        return "****" + data.substring(data.length() - 4);
    }
}

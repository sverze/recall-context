package com.recallcontext.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int IV_LENGTH = 16;

    @Value("${encryption.secret}")
    private String masterSecret;

    /**
     * Encrypts the API key using AES-256 encryption
     * Returns a EncryptionResult containing the encrypted key and IV
     */
    public EncryptionResult encrypt(String apiKey, String userId) {
        try {
            // Generate secret key from master secret and user ID
            SecretKey secretKey = generateSecretKey(userId);

            // Generate random IV
            byte[] iv = generateIV();

            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] encryptedBytes = cipher.doFinal(apiKey.getBytes(StandardCharsets.UTF_8));

            // Encode to Base64
            String encryptedApiKey = Base64.getEncoder().encodeToString(encryptedBytes);
            String encodedIV = Base64.getEncoder().encodeToString(iv);

            log.debug("Successfully encrypted API key for user: {}", userId);
            return new EncryptionResult(encryptedApiKey, encodedIV);

        } catch (Exception e) {
            log.error("Error encrypting API key for user: {}", userId, e);
            throw new RuntimeException("Failed to encrypt API key", e);
        }
    }

    /**
     * Decrypts the API key using AES-256 decryption
     */
    public String decrypt(String encryptedApiKey, String encodedIV, String userId) {
        try {
            // Generate secret key from master secret and user ID
            SecretKey secretKey = generateSecretKey(userId);

            // Decode from Base64
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedApiKey);
            byte[] iv = Base64.getDecoder().decode(encodedIV);

            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            log.debug("Successfully decrypted API key for user: {}", userId);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error decrypting API key for user: {}", userId, e);
            throw new RuntimeException("Failed to decrypt API key", e);
        }
    }

    /**
     * Generates a secret key using PBKDF2 from master secret and user ID
     */
    private SecretKey generateSecretKey(String userId) throws Exception {
        // Use user ID as salt for key derivation
        byte[] salt = userId.getBytes(StandardCharsets.UTF_8);

        KeySpec spec = new PBEKeySpec(
                masterSecret.toCharArray(),
                salt,
                ITERATION_COUNT,
                KEY_LENGTH
        );

        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Generates a random Initialization Vector (IV)
     */
    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Result of encryption operation
     */
    public static class EncryptionResult {
        private final String encryptedApiKey;
        private final String iv;

        public EncryptionResult(String encryptedApiKey, String iv) {
            this.encryptedApiKey = encryptedApiKey;
            this.iv = iv;
        }

        public String getEncryptedApiKey() {
            return encryptedApiKey;
        }

        public String getIv() {
            return iv;
        }
    }
}

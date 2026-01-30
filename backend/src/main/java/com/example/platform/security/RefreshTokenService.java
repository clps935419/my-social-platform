package com.example.platform.security;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Refresh token generation and hashing service
 */
@Component
public class RefreshTokenService {

    private static final int TOKEN_LENGTH = 32; // 32 bytes = 256 bits

    /**
     * Generate a secure random refresh token
     */
    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Hash refresh token for storage (using SHA-256)
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }

    /**
     * Verify refresh token against stored hash
     */
    public boolean verifyToken(String token, String storedHash) {
        String computedHash = hashToken(token);
        return computedHash.equals(storedHash);
    }
}

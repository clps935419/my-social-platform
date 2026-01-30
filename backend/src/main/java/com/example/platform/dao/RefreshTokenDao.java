package com.example.platform.dao;

import com.example.platform.common.UnauthorizedException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for refresh token operations
 */
@Repository
public class RefreshTokenDao {

    private final StoredProcedureExecutor spExecutor;
    private final JdbcTemplate jdbcTemplate;

    public RefreshTokenDao(StoredProcedureExecutor spExecutor, JdbcTemplate jdbcTemplate) {
        this.spExecutor = spExecutor;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Issue a new refresh token
     */
    public RefreshTokenRecord issue(UUID userId, String tokenHash, Instant expiresAt) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_user_id", userId);
        params.put("p_token_hash", tokenHash);
        params.put("p_expires_at", expiresAt);

        return spExecutor.executeQuery("sp_refresh_token_issue", params, this::mapRefreshTokenRecord);
    }

    /**
     * Validate refresh token
     * Returns token info if valid, null if invalid/expired/revoked
     */
    public ValidatedToken validate(String tokenHash) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_token_hash", tokenHash);

        try {
            return spExecutor.executeQuery("sp_refresh_token_validate", params, this::mapValidatedToken);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Rotate refresh token (revoke old, issue new)
     */
    public RotatedToken rotate(String oldTokenHash, String newTokenHash, Instant newExpiresAt) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_old_token_hash", oldTokenHash);
        params.put("p_new_token_hash", newTokenHash);
        params.put("p_new_expires_at", newExpiresAt);

        try {
            return spExecutor.executeQuery("sp_refresh_token_rotate", params, this::mapRotatedToken);
        } catch (DataAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("INVALID_REFRESH_TOKEN")) {
                throw new UnauthorizedException("Invalid or expired refresh token");
            }
            throw e;
        }
    }

    /**
     * Revoke refresh token
     */
    public boolean revoke(String tokenHash) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_token_hash", tokenHash);

        Boolean result = spExecutor.executeQuery("sp_refresh_token_revoke", params, rs -> rs.getBoolean("success"));
        return result != null && result;
    }

    private RefreshTokenRecord mapRefreshTokenRecord(ResultSet rs) throws SQLException {
        return new RefreshTokenRecord(
                (UUID) rs.getObject("refresh_token_id"),
                rs.getTimestamp("issued_at").toInstant()
        );
    }

    private ValidatedToken mapValidatedToken(ResultSet rs) throws SQLException {
        return new ValidatedToken(
                (UUID) rs.getObject("user_id"),
                (UUID) rs.getObject("refresh_token_id"),
                rs.getTimestamp("expires_at").toInstant()
        );
    }

    private RotatedToken mapRotatedToken(ResultSet rs) throws SQLException {
        return new RotatedToken(
                (UUID) rs.getObject("user_id"),
                (UUID) rs.getObject("refresh_token_id"),
                rs.getTimestamp("issued_at").toInstant()
        );
    }

    public record RefreshTokenRecord(
            UUID refreshTokenId,
            Instant issuedAt
    ) {}

    public record ValidatedToken(
            UUID userId,
            UUID refreshTokenId,
            Instant expiresAt
    ) {}

    public record RotatedToken(
            UUID userId,
            UUID refreshTokenId,
            Instant issuedAt
    ) {}
}

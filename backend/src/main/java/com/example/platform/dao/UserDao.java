package com.example.platform.dao;

import com.example.platform.common.ConflictException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for user-related database operations
 */
@Repository
public class UserDao {

    private final StoredProcedureExecutor spExecutor;
    private final JdbcTemplate jdbcTemplate;

    public UserDao(@Qualifier("storedProcedureExecutor") StoredProcedureExecutor spExecutor, JdbcTemplate jdbcTemplate) {
        this.spExecutor = spExecutor;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Register a new user
     */
    public UserProfile register(String phoneE164, String userName, String email, 
                                 String passwordHash, String passwordSalt,
                                 String coverImageUrl, String biography) {
        // Use LinkedHashMap to preserve parameter order
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_phone_e164", phoneE164);
        params.put("p_user_name", userName);
        params.put("p_email", email);
        params.put("p_password_hash", passwordHash);
        params.put("p_password_salt", passwordSalt);
        params.put("p_cover_image_url", coverImageUrl);
        params.put("p_biography", biography);

        try {
            return spExecutor.executeQuery("sp_user_register", params, this::mapUserProfile);
        } catch (DataAccessException e) {
            // Check for phone number exists error
            if (e.getMessage() != null && e.getMessage().contains("PHONE_NUMBER_EXISTS")) {
                throw new ConflictException("Phone number already exists");
            }
            throw e;
        }
    }

    /**
     * Get user by phone number (for login)
     */
    public UserWithPassword getUserByPhone(String phoneE164) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_phone_e164", phoneE164);

        return spExecutor.executeQuery("sp_user_get_by_phone", params, this::mapUserWithPassword);
    }

    /**
     * Get user profile by user ID
     */
    public UserProfile getUserProfile(UUID userId) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("p_user_id", userId);

        return spExecutor.executeQuery("sp_user_get_profile", params, this::mapUserProfile);
    }

    private UserProfile mapUserProfile(ResultSet rs) throws SQLException {
        Instant updatedAt = null;
        try {
            if (rs.getTimestamp("updated_at") != null) {
                updatedAt = rs.getTimestamp("updated_at").toInstant();
            }
        } catch (SQLException e) {
            // updated_at column may not exist (e.g., in register response)
        }
        
        return new UserProfile(
                (UUID) rs.getObject("user_id"),
                rs.getString("phone_e164"),
                rs.getString("user_name"),
                rs.getString("email"),
                rs.getString("cover_image_url"),
                rs.getString("biography"),
                rs.getTimestamp("created_at").toInstant(),
                updatedAt
        );
    }

    private UserWithPassword mapUserWithPassword(ResultSet rs) throws SQLException {
        return new UserWithPassword(
                (UUID) rs.getObject("user_id"),
                rs.getString("phone_e164"),
                rs.getString("user_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("password_salt"),
                rs.getString("cover_image_url"),
                rs.getString("biography"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null
        );
    }

    /**
     * User profile (without password)
     */
    public record UserProfile(
            UUID userId,
            String phoneE164,
            String userName,
            String email,
            String coverImageUrl,
            String biography,
            Instant createdAt,
            Instant updatedAt
    ) {}

    /**
     * User with password fields (for login)
     */
    public record UserWithPassword(
            UUID userId,
            String phoneE164,
            String userName,
            String email,
            String passwordHash,
            String passwordSalt,
            String coverImageUrl,
            String biography,
            Instant createdAt,
            Instant updatedAt
    ) {
        public UserProfile toProfile() {
            return new UserProfile(userId, phoneE164, userName, email, coverImageUrl, biography, createdAt, updatedAt);
        }
    }
}

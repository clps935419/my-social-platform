package com.example.platform.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response for authentication (login/register)
 */
@Schema(description = "Authentication response with tokens and user info")
public record AuthResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @JsonProperty("accessToken")
        String accessToken,

        @Schema(description = "Refresh token for obtaining new access tokens")
        @JsonProperty("refreshToken")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer")
        @JsonProperty("tokenType")
        String tokenType,

        @Schema(description = "Access token expiration time in seconds", example = "3600")
        @JsonProperty("expiresInSeconds")
        long expiresInSeconds,

        @Schema(description = "User profile information")
        @JsonProperty("user")
        UserInfo user
) {
    @Schema(description = "User information")
    public record UserInfo(
            @Schema(description = "User ID")
            @JsonProperty("userId")
            UUID userId,

            @Schema(description = "Phone number in E.164 format", example = "+886912345678")
            @JsonProperty("phoneNumber")
            String phoneNumber,

            @Schema(description = "Display name", example = "John Doe")
            @JsonProperty("userName")
            String userName,

            @Schema(description = "Email address", example = "john@example.com")
            @JsonProperty("email")
            String email,

            @Schema(description = "Cover image URL", example = "https://example.com/avatar.jpg")
            @JsonProperty("coverImage")
            String coverImage,

            @Schema(description = "Biography", example = "Software developer")
            @JsonProperty("biography")
            String biography,

            @Schema(description = "Account creation timestamp (UTC)", example = "2026-01-30T12:34:56Z")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
            @JsonProperty("createdAt")
            Instant createdAt
    ) {}
}

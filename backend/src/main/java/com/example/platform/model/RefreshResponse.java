package com.example.platform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response for refresh token endpoint
 */
@Schema(description = "Refresh token response with new tokens")
public record RefreshResponse(
        @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @JsonProperty("accessToken")
        String accessToken,

        @Schema(description = "New refresh token (old token is now invalid)")
        @JsonProperty("refreshToken")
        String refreshToken,

        @Schema(description = "Token type", example = "Bearer")
        @JsonProperty("tokenType")
        String tokenType,

        @Schema(description = "Access token expiration time in seconds", example = "3600")
        @JsonProperty("expiresInSeconds")
        long expiresInSeconds
) {}

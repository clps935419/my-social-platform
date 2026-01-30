package com.example.platform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for refresh token
 */
@Schema(description = "Refresh token request")
public record RefreshRequest(
        @NotBlank(message = "Refresh token is required")
        @Schema(description = "Refresh token obtained from login or previous refresh", 
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @JsonProperty("refreshToken")
        String refreshToken
) {}

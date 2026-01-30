package com.example.platform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for user login
 */
@Schema(description = "User login request")
public record LoginRequest(
        @NotBlank(message = "Phone number is required")
        @Schema(description = "Phone number in E.164 format or with separators", 
                example = "+886912345678")
        @JsonProperty("phoneNumber")
        String phoneNumber,

        @NotBlank(message = "Password is required")
        @Schema(description = "Password", example = "password123")
        @JsonProperty("password")
        String password
) {}

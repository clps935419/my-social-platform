package com.example.platform.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for user registration
 */
@Schema(description = "User registration request")
public record RegisterRequest(
        @NotBlank(message = "Phone number is required")
        @Schema(description = "Phone number in E.164 format or with separators (e.g., +886-912-345-678)", 
                example = "+886912345678")
        @JsonProperty("phoneNumber")
        String phoneNumber,

        @NotBlank(message = "User name is required")
        @Size(max = 100, message = "User name must not exceed 100 characters")
        @Schema(description = "Display name", example = "John Doe")
        @JsonProperty("userName")
        String userName,

        @Size(max = 255, message = "Email must not exceed 255 characters")
        @Pattern(regexp = "^$|^[^@]+@[^@]+\\.[^@]+$", message = "Invalid email format")
        @Schema(description = "Email address (optional)", example = "john@example.com")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Schema(description = "Password (min 8 characters)", example = "password123")
        @JsonProperty("password")
        String password,

        @Size(max = 2048, message = "Cover image URL must not exceed 2048 characters")
        @Pattern(regexp = "^$|^https?://.*", message = "Cover image must be a valid HTTP(S) URL")
        @Schema(description = "Cover image URL (optional)", example = "https://example.com/avatar.jpg")
        @JsonProperty("coverImage")
        String coverImage,

        @Schema(description = "Biography (optional)", example = "Software developer")
        @JsonProperty("biography")
        String biography
) {}

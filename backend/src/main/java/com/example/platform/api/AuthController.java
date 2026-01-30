package com.example.platform.api;

import com.example.platform.common.UnauthorizedException;
import com.example.platform.dao.RefreshTokenDao;
import com.example.platform.dao.UserDao;
import com.example.platform.model.*;
import com.example.platform.security.JwtService;
import com.example.platform.security.PasswordHasher;
import com.example.platform.security.RefreshTokenService;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Authentication endpoints: register, login, refresh
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration, login, and token management")
public class AuthController {

    private final UserDao userDao;
    private final RefreshTokenDao refreshTokenDao;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PhoneNumberUtil phoneNumberUtil;

    public AuthController(UserDao userDao,
                          RefreshTokenDao refreshTokenDao,
                          PasswordHasher passwordHasher,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.userDao = userDao;
        this.refreshTokenDao = refreshTokenDao;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.phoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register new user",
            description = "Register a new user with phone number and password. Phone number must be unique.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or phone number format"),
                    @ApiResponse(responseCode = "409", description = "Phone number already exists"),
                    @ApiResponse(responseCode = "429", description = "Too many requests")
            }
    )
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        // Normalize phone number to E.164
        String phoneE164 = normalizePhoneNumber(request.phoneNumber());

        // Hash password
        String salt = passwordHasher.generateSalt();
        String hash = passwordHasher.hashPassword(request.password(), salt);

        // Register user
        UserDao.UserProfile profile = userDao.register(
                phoneE164,
                request.userName(),
                request.email(),
                hash,
                salt,
                request.coverImage(),
                request.biography()
        );

        // Generate tokens
        String accessToken = jwtService.issueToken(profile.userId());
        String refreshToken = refreshTokenService.generateToken();
        String refreshTokenHash = refreshTokenService.hashToken(refreshToken);

        // Store refresh token
        long refreshExpirationSeconds = 2592000; // 30 days (from config)
        Instant refreshExpiresAt = Instant.now().plusSeconds(refreshExpirationSeconds);
        refreshTokenDao.issue(profile.userId(), refreshTokenHash, refreshExpiresAt);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                new AuthResponse.UserInfo(
                        profile.userId(),
                        profile.phoneE164(),
                        profile.userName(),
                        profile.email(),
                        profile.coverImageUrl(),
                        profile.biography(),
                        profile.createdAt()
                )
        );
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login with phone number and password",
            description = "Authenticate user and receive access token and refresh token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "429", description = "Too many requests")
            }
    )
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        // Normalize phone number to E.164
        String phoneE164 = normalizePhoneNumber(request.phoneNumber());

        // Get user by phone
        UserDao.UserWithPassword user = userDao.getUserByPhone(phoneE164);
        if (user == null) {
            throw new UnauthorizedException("Invalid phone number or password");
        }

        // Verify password
        if (!passwordHasher.verify(request.password(), user.passwordHash(), user.passwordSalt())) {
            throw new UnauthorizedException("Invalid phone number or password");
        }

        // Generate tokens
        String accessToken = jwtService.issueToken(user.userId());
        String refreshToken = refreshTokenService.generateToken();
        String refreshTokenHash = refreshTokenService.hashToken(refreshToken);

        // Store refresh token
        long refreshExpirationSeconds = 2592000; // 30 days (from config)
        Instant refreshExpiresAt = Instant.now().plusSeconds(refreshExpirationSeconds);
        refreshTokenDao.issue(user.userId(), refreshTokenHash, refreshExpiresAt);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getExpirationSeconds(),
                new AuthResponse.UserInfo(
                        user.userId(),
                        user.phoneE164(),
                        user.userName(),
                        user.email(),
                        user.coverImageUrl(),
                        user.biography(),
                        user.createdAt()
                )
        );
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Exchange refresh token for new access token and refresh token (rotation). Old refresh token becomes invalid.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            }
    )
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest request) {
        String oldToken = request.refreshToken();
        String oldTokenHash = refreshTokenService.hashToken(oldToken);

        // Generate new tokens
        String newRefreshToken = refreshTokenService.generateToken();
        String newRefreshTokenHash = refreshTokenService.hashToken(newRefreshToken);

        // Rotate tokens (atomic: revoke old + issue new)
        long refreshExpirationSeconds = 2592000; // 30 days
        Instant newExpiresAt = Instant.now().plusSeconds(refreshExpirationSeconds);
        RefreshTokenDao.RotatedToken rotated = refreshTokenDao.rotate(
                oldTokenHash,
                newRefreshTokenHash,
                newExpiresAt
        );

        // Issue new access token
        String accessToken = jwtService.issueToken(rotated.userId());

        return new RefreshResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }

    /**
     * Normalize phone number to E.164 format
     */
    private String normalizePhoneNumber(String phoneNumber) {
        try {
            // Try parsing with default region (can be configured)
            Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, "TW");
            
            if (!phoneNumberUtil.isValidNumber(parsedNumber)) {
                throw new IllegalArgumentException("Invalid phone number");
            }
            
            return phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Invalid phone number format: " + e.getMessage());
        }
    }
}

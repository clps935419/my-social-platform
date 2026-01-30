package com.example.platform.api;

import com.example.platform.common.UnauthorizedException;
import com.example.platform.dao.UserDao;
import com.example.platform.model.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * User profile endpoint (requires authentication)
 */
@RestController
@RequestMapping("/me")
@Tag(name = "User Profile", description = "Current user profile management")
public class MeController {

    private final UserDao userDao;

    public MeController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping
    @Operation(
            summary = "Get current user profile",
            description = "Get profile information for the authenticated user",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
            }
    )
    public UserProfileResponse getProfile(HttpServletRequest request) {
        // Get user ID from request attribute (set by JwtAuthFilter)
        UUID userId = (UUID) request.getAttribute("userId");
        
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }

        UserDao.UserProfile profile = userDao.getUserProfile(userId);
        
        if (profile == null) {
            throw new UnauthorizedException("User not found");
        }

        return new UserProfileResponse(
                profile.userId(),
                profile.phoneE164(),
                profile.userName(),
                profile.email(),
                profile.coverImageUrl(),
                profile.biography(),
                profile.createdAt(),
                profile.updatedAt()
        );
    }
}

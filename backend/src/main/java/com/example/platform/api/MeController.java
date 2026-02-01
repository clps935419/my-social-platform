package com.example.platform.api;

import com.example.platform.api.validation.RequestValidators;
import com.example.platform.common.UnauthorizedException;
import com.example.platform.dao.PostDao;
import com.example.platform.dao.UserDao;
import com.example.platform.model.Post;
import com.example.platform.model.PostListResponse;
import com.example.platform.model.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User profile endpoint (requires authentication)
 */
@RestController
@RequestMapping("/me")
@Tag(name = "User Profile", description = "Current user profile management")
public class MeController {

    private final UserDao userDao;
    private final PostDao postDao;

    public MeController(UserDao userDao, PostDao postDao) {
        this.userDao = userDao;
        this.postDao = postDao;
    }

    @GetMapping
    @Operation(
            summary = "Get current user profile",
            description = "Get profile information for the authenticated user",
            security = @SecurityRequirement(name = "bearerAuth"),
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

    @GetMapping("/posts")
    @Operation(
            summary = "Get current user's posts",
            description = "Get a paginated list of posts created by the authenticated user. Supports sorting by creation date. Requires authentication.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
            }
    )
    public ResponseEntity<PostListResponse> getMyPosts(
        @Parameter(description = "Maximum number of posts to return (1-100, default: 20)")
        @RequestParam(required = false) Integer limit,
        
        @Parameter(description = "Number of posts to skip (default: 0)")
        @RequestParam(required = false) Integer offset,
        
        @Parameter(description = "Sort order: 'newest' (newest first, default) or 'oldest' (oldest first)")
        @RequestParam(required = false) String sort,
        
        HttpServletRequest httpRequest
    ) {
        // Get authenticated user ID
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        // Validate and normalize parameters
        int validatedLimit = RequestValidators.validateLimit(limit);
        int validatedOffset = RequestValidators.validateOffset(offset);
        String validatedSort = RequestValidators.validateSort(sort, "newest");
        
        // Call DAO with user filter
        Map<String, Object> result = postDao.listPosts(validatedLimit, validatedOffset, userId.toString(), validatedSort);
        
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.get("posts");
        int total = (int) result.get("total");
        
        PostListResponse response = new PostListResponse(posts, total);
        return ResponseEntity.ok(response);
    }
}

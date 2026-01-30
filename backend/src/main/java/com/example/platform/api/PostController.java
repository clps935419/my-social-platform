package com.example.platform.api;

import com.example.platform.api.validation.RequestValidators;
import com.example.platform.common.ForbiddenException;
import com.example.platform.common.NotFoundException;
import com.example.platform.common.UnauthorizedException;
import com.example.platform.dao.PostDao;
import com.example.platform.model.CreatePostRequest;
import com.example.platform.model.Post;
import com.example.platform.model.PostListResponse;
import com.example.platform.model.UpdatePostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for post operations
 * US1: GET /posts - List posts (public, no auth required)
 * US3: POST /posts - Create post (requires auth)
 * US3: PATCH /posts/{postId} - Update post (author only)
 * US3: DELETE /posts/{postId} - Delete post (author only)
 */
@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Post management endpoints")
public class PostController {
    
    private final PostDao postDao;
    
    public PostController(PostDao postDao) {
        this.postDao = postDao;
    }
    
    @GetMapping
    @Operation(
        summary = "List posts",
        description = "Get a paginated list of posts (newest first). Excludes soft-deleted posts. Public endpoint, no authentication required."
    )
    public ResponseEntity<PostListResponse> listPosts(
        @Parameter(description = "Maximum number of posts to return (1-100, default: 20)")
        @RequestParam(required = false) Integer limit,
        
        @Parameter(description = "Number of posts to skip (default: 0)")
        @RequestParam(required = false) Integer offset
    ) {
        // Validate and normalize parameters
        int validatedLimit = RequestValidators.validateLimit(limit);
        int validatedOffset = RequestValidators.validateOffset(offset);
        
        // Call DAO
        Map<String, Object> result = postDao.listPosts(validatedLimit, validatedOffset);
        
        @SuppressWarnings("unchecked")
        List<Post> posts = (List<Post>) result.get("posts");
        int total = (int) result.get("total");
        
        PostListResponse response = new PostListResponse(posts, total);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(
        summary = "Create post",
        description = "Create a new post. Requires authentication.",
        security = @SecurityRequirement(name = "BearerAuth"),
        responses = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
        }
    )
    public ResponseEntity<Post> createPost(
        @Valid @RequestBody CreatePostRequest request,
        HttpServletRequest httpRequest
    ) {
        // Get authenticated user ID
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        // Validate content is not blank
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required and cannot be empty");
        }
        
        // Validate image URL if provided
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (request.getImage().length() > 2048) {
                throw new IllegalArgumentException("Image URL must not exceed 2048 characters");
            }
            if (!request.getImage().matches("^https?://.*")) {
                throw new IllegalArgumentException("Image URL must start with http:// or https://");
            }
        }
        
        // Create post
        Post post = postDao.createPost(
            userId.toString(),
            request.getContent(),
            request.getImage()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }
    
    @PatchMapping("/{postId}")
    @Operation(
        summary = "Update post",
        description = "Update a post. Only the author can update their post.",
        security = @SecurityRequirement(name = "BearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the author"),
            @ApiResponse(responseCode = "404", description = "Post not found or deleted")
        }
    )
    public ResponseEntity<Post> updatePost(
        @Parameter(description = "Post ID") @PathVariable String postId,
        @Valid @RequestBody UpdatePostRequest request,
        HttpServletRequest httpRequest
    ) {
        // Get authenticated user ID
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        // Validate content if provided
        if (request.getContent() != null && request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be empty or all whitespace");
        }
        
        // Validate image URL if provided
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            if (request.getImage().length() > 2048) {
                throw new IllegalArgumentException("Image URL must not exceed 2048 characters");
            }
            if (!request.getImage().matches("^https?://.*")) {
                throw new IllegalArgumentException("Image URL must start with http:// or https://");
            }
        }
        
        // Update post
        Map<String, Object> result = postDao.updatePost(
            userId.toString(),
            postId,
            request.getContent(),
            request.getImage()
        );
        
        // Check metadata and return appropriate response
        boolean exists = (boolean) result.get("exists");
        boolean deleted = (boolean) result.get("deleted");
        boolean isAuthor = (boolean) result.get("isAuthor");
        
        if (!exists || deleted) {
            throw new NotFoundException("Post not found");
        }
        
        if (!isAuthor) {
            throw new ForbiddenException("You are not the author of this post");
        }
        
        Post post = (Post) result.get("post");
        return ResponseEntity.ok(post);
    }
    
    @DeleteMapping("/{postId}")
    @Operation(
        summary = "Delete post",
        description = "Soft delete a post. Only the author can delete their post.",
        security = @SecurityRequirement(name = "BearerAuth"),
        responses = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the author"),
            @ApiResponse(responseCode = "404", description = "Post not found or already deleted")
        }
    )
    public ResponseEntity<Void> deletePost(
        @Parameter(description = "Post ID") @PathVariable String postId,
        HttpServletRequest httpRequest
    ) {
        // Get authenticated user ID
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        // Delete post
        Map<String, Object> result = postDao.deletePost(userId.toString(), postId);
        
        // Check metadata and return appropriate response
        boolean exists = (boolean) result.get("exists");
        boolean deleted = (boolean) result.get("deleted");
        boolean isAuthor = (boolean) result.get("isAuthor");
        
        // If post doesn't exist or is already deleted, return 404
        if (!exists || deleted) {
            throw new NotFoundException("Post not found");
        }
        
        // If user is not the author, return 403
        if (!isAuthor) {
            throw new ForbiddenException("You are not the author of this post");
        }
        
        return ResponseEntity.noContent().build();
    }
}


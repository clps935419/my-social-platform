package com.example.platform.api;

import com.example.platform.api.validation.RequestValidators;
import com.example.platform.common.NotFoundException;
import com.example.platform.common.UnauthorizedException;
import com.example.platform.dao.CommentDao;
import com.example.platform.model.Comment;
import com.example.platform.model.CommentListResponse;
import com.example.platform.model.CreateCommentRequest;
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
 * REST controller for comment operations
 * GET /posts/{postId}/comments - List comments (public, no auth required)
 * POST /posts/{postId}/comments - Create comment (requires auth)
 */
@RestController
@RequestMapping("/posts/{postId}/comments")
@Tag(name = "Comments", description = "Comment management endpoints")
public class CommentController {
    
    private final CommentDao commentDao;
    
    public CommentController(CommentDao commentDao) {
        this.commentDao = commentDao;
    }
    
    @GetMapping
    @Operation(
        summary = "List comments for a post",
        description = "Get a paginated list of comments for a specific post (oldest first). Returns 404 if post doesn't exist or is deleted. Public endpoint, no authentication required."
    )
    public ResponseEntity<CommentListResponse> listComments(
        @Parameter(description = "Post ID (UUID format)")
        @PathVariable String postId,
        
        @Parameter(description = "Maximum number of comments to return (1-100, default: 20)")
        @RequestParam(required = false) Integer limit,
        
        @Parameter(description = "Number of comments to skip (default: 0)")
        @RequestParam(required = false) Integer offset
    ) {
        // Parse and validate UUID
        UUID postUuid;
        try {
            postUuid = UUID.fromString(postId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid post ID format");
        }
        
        // Validate and normalize parameters
        int validatedLimit = RequestValidators.validateLimit(limit);
        int validatedOffset = RequestValidators.validateOffset(offset);
        
        // Call DAO
        Map<String, Object> result = commentDao.listCommentsByPost(postUuid, validatedLimit, validatedOffset);
        
        Boolean postExists = (Boolean) result.get("postExists");
        Boolean postDeleted = (Boolean) result.get("postDeleted");
        
        // Return 404 if post doesn't exist or is deleted
        if (!Boolean.TRUE.equals(postExists) || Boolean.TRUE.equals(postDeleted)) {
            throw new NotFoundException("Post not found");
        }
        
        @SuppressWarnings("unchecked")
        List<Comment> comments = (List<Comment>) result.get("comments");
        int total = (int) result.get("total");
        
        CommentListResponse response = new CommentListResponse(comments, total);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(
        summary = "Create comment",
        description = "Create a new comment on a post. Requires authentication. Returns 404 if post doesn't exist or is deleted.",
        security = @SecurityRequirement(name = "BearerAuth"),
        responses = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "404", description = "Post not found or deleted")
        }
    )
    public ResponseEntity<Comment> createComment(
        @Parameter(description = "Post ID (UUID format)")
        @PathVariable String postId,
        
        @Valid @RequestBody CreateCommentRequest request,
        HttpServletRequest httpRequest
    ) {
        // Parse and validate UUID
        UUID postUuid;
        try {
            postUuid = UUID.fromString(postId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid post ID format");
        }
        
        // Get authenticated user ID
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        
        // Call DAO
        Map<String, Object> result = commentDao.createComment(userId, postUuid, request.getContent());
        
        Boolean postExists = (Boolean) result.get("postExists");
        Boolean postDeleted = (Boolean) result.get("postDeleted");
        
        // Return 404 if post doesn't exist or is deleted
        if (!Boolean.TRUE.equals(postExists) || Boolean.TRUE.equals(postDeleted)) {
            throw new NotFoundException("Post not found");
        }
        
        Comment comment = (Comment) result.get("comment");
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}

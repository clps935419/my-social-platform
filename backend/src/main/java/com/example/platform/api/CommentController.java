package com.example.platform.api;

import com.example.platform.api.validation.RequestValidators;
import com.example.platform.common.NotFoundException;
import com.example.platform.dao.CommentDao;
import com.example.platform.model.Comment;
import com.example.platform.model.CommentListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for comment operations
 * US1: GET /posts/{postId}/comments - List comments (public, no auth required)
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
}

package com.example.platform.api;

import com.example.platform.api.validation.RequestValidators;
import com.example.platform.dao.PostDao;
import com.example.platform.model.Post;
import com.example.platform.model.PostListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for post operations
 * US1: GET /posts - List posts (public, no auth required)
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
}

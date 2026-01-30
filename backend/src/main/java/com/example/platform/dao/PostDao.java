package com.example.platform.dao;

import com.example.platform.model.Author;
import com.example.platform.model.Post;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data access object for posts
 * Uses stored procedure sp_post_list
 */
@Repository
public class PostDao extends StoredProcedureExecutor {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
    public PostDao(DataSource dataSource) {
        super(dataSource);
    }
    
    /**
     * List posts using sp_post_list stored procedure
     * 
     * @param limit Maximum number of posts to return
     * @param offset Number of posts to skip
     * @return Map with "posts" (List<Post>) and "total" (Integer)
     */
    public Map<String, Object> listPosts(int limit, int offset) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_limit", limit);
        params.put("p_offset", offset);
        
        // Query the stored procedure
        List<Post> posts = getJdbcTemplate().query(
            "SELECT * FROM sp_post_list(?, ?)",
            new Object[]{limit, offset},
            new PostRowMapper()
        );
        
        // Extract total count from first row (all rows have same total_count)
        int total = posts.isEmpty() ? 0 : 
            getJdbcTemplate().queryForObject(
                "SELECT COUNT(*) FROM posts WHERE deleted_at IS NULL",
                Integer.class
            );
        
        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("total", total);
        return result;
    }
    
    /**
     * Create a new post
     * 
     * @param authorUserId Author's user ID
     * @param content Post content (required, not blank)
     * @param imageUrl Optional image URL
     * @return Created Post
     */
    public Post createPost(String authorUserId, String content, String imageUrl) {
        List<Post> posts = getJdbcTemplate().query(
            "SELECT * FROM sp_post_create(?, ?, ?)",
            new Object[]{java.util.UUID.fromString(authorUserId), content, imageUrl},
            new PostRowMapper()
        );
        
        if (posts.isEmpty()) {
            throw new RuntimeException("Failed to create post");
        }
        
        return posts.get(0);
    }
    
    /**
     * Update a post (author only)
     * 
     * @param actorUserId User attempting the update
     * @param postId Post ID to update
     * @param content New content (nullable for partial update)
     * @param imageUrl New image URL (nullable for partial update)
     * @return Map with "post" (Post or null), "exists" (boolean), "deleted" (boolean), "isAuthor" (boolean)
     */
    public Map<String, Object> updatePost(String actorUserId, String postId, String content, String imageUrl) {
        List<Map<String, Object>> results = getJdbcTemplate().query(
            "SELECT * FROM sp_post_update(?, ?, ?, ?)",
            new Object[]{
                java.util.UUID.fromString(actorUserId),
                java.util.UUID.fromString(postId),
                content,
                imageUrl
            },
            (rs, rowNum) -> {
                Map<String, Object> result = new HashMap<>();
                
                // Extract metadata
                result.put("exists", rs.getBoolean("post_exists"));
                result.put("deleted", rs.getBoolean("post_deleted"));
                result.put("isAuthor", rs.getBoolean("is_author"));
                
                // Extract post if available
                String returnedPostId = rs.getString("post_id");
                if (returnedPostId != null) {
                    Post post = new Post();
                    post.setPostId(returnedPostId);
                    post.setContent(rs.getString("content"));
                    post.setImage(rs.getString("image_url"));
                    
                    OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
                    if (createdAt != null) {
                        post.setCreatedAt(ISO_FORMATTER.format(createdAt));
                    }
                    
                    OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
                    if (updatedAt != null) {
                        post.setUpdatedAt(ISO_FORMATTER.format(updatedAt));
                    }
                    
                    Author author = new Author();
                    author.setUserId(rs.getString("author_user_id"));
                    author.setUserName(rs.getString("author_user_name"));
                    author.setCoverImage(rs.getString("author_cover_image_url"));
                    post.setAuthor(author);
                    
                    result.put("post", post);
                } else {
                    result.put("post", null);
                }
                
                return result;
            }
        );
        
        if (results.isEmpty()) {
            throw new RuntimeException("Failed to execute update");
        }
        
        return results.get(0);
    }
    
    /**
     * Soft delete a post (author only)
     * 
     * @param actorUserId User attempting the deletion
     * @param postId Post ID to delete
     * @return Map with "exists" (boolean), "deleted" (boolean), "isAuthor" (boolean)
     */
    public Map<String, Object> deletePost(String actorUserId, String postId) {
        List<Map<String, Object>> results = getJdbcTemplate().query(
            "SELECT * FROM sp_post_soft_delete(?, ?)",
            new Object[]{
                java.util.UUID.fromString(actorUserId),
                java.util.UUID.fromString(postId)
            },
            (rs, rowNum) -> {
                Map<String, Object> result = new HashMap<>();
                result.put("exists", rs.getBoolean("post_exists"));
                result.put("deleted", rs.getBoolean("post_deleted"));
                result.put("isAuthor", rs.getBoolean("is_author"));
                return result;
            }
        );
        
        if (results.isEmpty()) {
            throw new RuntimeException("Failed to execute delete");
        }
        
        return results.get(0);
    }
    
    /**
     * RowMapper for Post objects from stored procedure result
     */
    private static class PostRowMapper implements RowMapper<Post> {
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            Post post = new Post();
            post.setPostId(rs.getString("post_id"));
            post.setContent(rs.getString("content"));
            post.setImage(rs.getString("image_url"));
            
            // Format timestamps to ISO-8601
            OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
            if (createdAt != null) {
                post.setCreatedAt(ISO_FORMATTER.format(createdAt));
            }
            
            OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);
            if (updatedAt != null) {
                post.setUpdatedAt(ISO_FORMATTER.format(updatedAt));
            }
            
            // Map author information
            Author author = new Author();
            author.setUserId(rs.getString("author_user_id"));
            author.setUserName(rs.getString("author_user_name"));
            author.setCoverImage(rs.getString("author_cover_image_url"));
            post.setAuthor(author);
            
            return post;
        }
    }
}

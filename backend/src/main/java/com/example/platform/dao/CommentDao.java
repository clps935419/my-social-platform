package com.example.platform.dao;

import com.example.platform.model.Author;
import com.example.platform.model.Comment;
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
import java.util.UUID;

/**
 * Data access object for comments
 * Uses stored procedure sp_comment_list_by_post
 */
@Repository
public class CommentDao extends StoredProcedureExecutor {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    
    public CommentDao(DataSource dataSource) {
        super(dataSource);
    }
    
    /**
     * List comments for a post using sp_comment_list_by_post stored procedure
     * 
     * @param postId The post ID to get comments for
     * @param limit Maximum number of comments to return
     * @param offset Number of comments to skip
     * @return Map with "comments" (List<Comment>), "total" (Integer), "postExists" (Boolean), "postDeleted" (Boolean)
     */
    public Map<String, Object> listCommentsByPost(UUID postId, int limit, int offset) {
        // Query the stored procedure
        List<Map<String, Object>> results = getJdbcTemplate().queryForList(
            "SELECT * FROM sp_comment_list_by_post(?, ?, ?)",
            postId, limit, offset
        );
        
        Map<String, Object> result = new HashMap<>();
        
        // If no results or first row indicates issues
        if (results.isEmpty()) {
            result.put("comments", List.of());
            result.put("total", 0);
            result.put("postExists", false);
            result.put("postDeleted", false);
            return result;
        }
        
        // Check first row for metadata
        Map<String, Object> firstRow = results.get(0);
        Boolean postExists = (Boolean) firstRow.get("post_exists");
        Boolean postDeleted = (Boolean) firstRow.get("post_deleted");
        
        result.put("postExists", postExists != null ? postExists : false);
        result.put("postDeleted", postDeleted != null ? postDeleted : false);
        
        // If post doesn't exist or is deleted, return empty list
        if (!Boolean.TRUE.equals(postExists) || Boolean.TRUE.equals(postDeleted)) {
            result.put("comments", List.of());
            result.put("total", 0);
            return result;
        }
        
        // Parse comments
        List<Comment> comments = results.stream()
            .filter(row -> row.get("comment_id") != null)
            .map(this::mapRowToComment)
            .toList();
        
        // Get total from first row
        Object totalCount = firstRow.get("total_count");
        int total = totalCount != null ? ((Number) totalCount).intValue() : 0;
        
        result.put("comments", comments);
        result.put("total", total);
        
        return result;
    }
    
    /**
     * Map a result row to a Comment object
     */
    private Comment mapRowToComment(Map<String, Object> row) {
        Comment comment = new Comment();
        
        Object commentId = row.get("comment_id");
        if (commentId != null) {
            comment.setCommentId(commentId.toString());
        }
        
        Object postId = row.get("post_id");
        if (postId != null) {
            comment.setPostId(postId.toString());
        }
        
        comment.setContent((String) row.get("content"));
        
        // Format timestamp to ISO-8601
        Object createdAtObj = row.get("created_at");
        if (createdAtObj instanceof OffsetDateTime) {
            comment.setCreatedAt(ISO_FORMATTER.format((OffsetDateTime) createdAtObj));
        }
        
        // Map author information
        Author author = new Author();
        Object authorUserId = row.get("author_user_id");
        if (authorUserId != null) {
            author.setUserId(authorUserId.toString());
        }
        author.setUserName((String) row.get("author_user_name"));
        author.setCoverImage((String) row.get("author_cover_image_url"));
        comment.setAuthor(author);
        
        return comment;
    }
}

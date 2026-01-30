package com.example.platform.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Comment model matching OpenAPI schema
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {
    
    private String commentId;
    private String postId;
    private Author author;
    private String content;
    private String createdAt;
    
    public Comment() {
    }
    
    public String getCommentId() {
        return commentId;
    }
    
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
    
    public String getPostId() {
        return postId;
    }
    
    public void setPostId(String postId) {
        this.postId = postId;
    }
    
    public Author getAuthor() {
        return author;
    }
    
    public void setAuthor(Author author) {
        this.author = author;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

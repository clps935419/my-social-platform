package com.example.platform.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request model for creating a comment
 */
public class CreateCommentRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
    
    public CreateCommentRequest() {
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}

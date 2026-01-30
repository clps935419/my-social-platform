package com.example.platform.model;

import jakarta.validation.constraints.Size;

/**
 * Request model for updating a post
 * Both fields are optional for partial updates
 */
public class UpdatePostRequest {
    
    private String content;
    
    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    private String image;
    
    public UpdatePostRequest() {
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
}

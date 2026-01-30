package com.example.platform.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request model for creating a post
 */
public class CreatePostRequest {
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    private String image;
    
    public CreatePostRequest() {
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

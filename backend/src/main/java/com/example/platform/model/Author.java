package com.example.platform.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Author information embedded in posts and comments
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Author {
    
    private String userId;
    private String userName;
    private String coverImage;
    
    public Author() {
    }
    
    public Author(String userId, String userName, String coverImage) {
        this.userId = userId;
        this.userName = userName;
        this.coverImage = coverImage;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
}

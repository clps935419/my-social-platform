package com.example.platform.model;

import java.util.List;

/**
 * Response model for post list endpoint
 */
public class PostListResponse {
    
    private List<Post> items;
    private int total;
    
    public PostListResponse() {
    }
    
    public PostListResponse(List<Post> items, int total) {
        this.items = items;
        this.total = total;
    }
    
    public List<Post> getItems() {
        return items;
    }
    
    public void setItems(List<Post> items) {
        this.items = items;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

package com.example.platform.model;

import java.util.List;

/**
 * Response model for comment list endpoint
 */
public class CommentListResponse {
    
    private List<Comment> items;
    private int total;
    
    public CommentListResponse() {
    }
    
    public CommentListResponse(List<Comment> items, int total) {
        this.items = items;
        this.total = total;
    }
    
    public List<Comment> getItems() {
        return items;
    }
    
    public void setItems(List<Comment> items) {
        this.items = items;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
}

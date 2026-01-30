package com.example.platform.common;

public class NotFoundException extends AppException {
    
    public NotFoundException(String message) {
        super("NOT_FOUND", message);
    }
    
    public NotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }
}

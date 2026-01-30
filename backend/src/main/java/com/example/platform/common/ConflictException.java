package com.example.platform.common;

public class ConflictException extends AppException {
    
    public ConflictException(String message) {
        super("CONFLICT", message);
    }
    
    public ConflictException(String errorCode, String message) {
        super(errorCode, message);
    }
}

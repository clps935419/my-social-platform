package com.example.platform.common;

public class ForbiddenException extends AppException {
    
    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }
    
    public ForbiddenException(String errorCode, String message) {
        super(errorCode, message);
    }
}

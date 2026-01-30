package com.example.platform.common;

public class UnauthorizedException extends AppException {
    
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
    
    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }
}

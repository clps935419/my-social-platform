package com.example.platform.common;

public class TooManyRequestsException extends AppException {
    
    public TooManyRequestsException(String message) {
        super("TOO_MANY_REQUESTS", message);
    }
    
    public TooManyRequestsException(String errorCode, String message) {
        super(errorCode, message);
    }
}

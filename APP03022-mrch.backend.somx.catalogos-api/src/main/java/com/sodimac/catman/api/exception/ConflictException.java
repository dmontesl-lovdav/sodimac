package com.sodimac.catman.api.exception;

public class ConflictException extends RuntimeException {
    
    private final String errorType;
    
    public ConflictException(String message) {
        super(message);
        this.errorType = "ConflictError";
    }
    
    public ConflictException(String errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public String getErrorType() {
        return errorType;
    }
}

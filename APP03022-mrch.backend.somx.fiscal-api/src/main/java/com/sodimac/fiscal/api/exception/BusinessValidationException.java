package com.sodimac.fiscal.api.exception;

/**
 * Exception thrown when business validation rules are violated
 * 
 * @author Sodimac Team
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


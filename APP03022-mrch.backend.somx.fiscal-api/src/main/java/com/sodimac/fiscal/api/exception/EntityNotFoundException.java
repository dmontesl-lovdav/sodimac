package com.sodimac.fiscal.api.exception;

/**
 * Exception thrown when a requested entity is not found
 * 
 * @author Sodimac Team
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


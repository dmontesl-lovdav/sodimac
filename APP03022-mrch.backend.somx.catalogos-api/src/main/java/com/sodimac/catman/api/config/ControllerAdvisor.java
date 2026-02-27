/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sodimac.catman.api.config;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import com.sodimac.catman.api.exception.ConflictException;
import com.sodimac.catman.api.exception.ExceptionWrapper;
import com.sodimac.catman.api.exception.GenericException;

/**
 * Global exception handler for REST controllers.
 * STM-1295: Standard error response format.
 *
 * @author ggalvan
 */
@ControllerAdvice(annotations = RestController.class)
public class ControllerAdvisor {

    private final Logger logger = LoggerFactory.getLogger(ControllerAdvisor.class);

    /**
     * Handle validation errors from @Valid annotations.
     * STM-1295 CA-05: Reject invalid fields.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionWrapper> handleValidationException(MethodArgumentNotValidException exception) {
        String details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        logger.warn("Validation error: {}", details);
        ExceptionWrapper wrapper = ExceptionWrapper.builder()
                .error("ValidationError")
                .code(HttpStatus.BAD_REQUEST.value())
                .details(details)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(wrapper);
    }

    /**
     * Handle conflict exceptions (409).
     * STM-1295 CA-06: Duplicate detection.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionWrapper> handleConflictException(ConflictException exception) {
        logger.warn("Conflict error: {}", exception.getMessage());
        ExceptionWrapper wrapper = ExceptionWrapper.builder()
                .error(exception.getErrorType())
                .code(HttpStatus.CONFLICT.value())
                .details(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(wrapper);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionWrapper> handleIllegalArgumentException(IllegalArgumentException exception) {
        logger.warn("Validation error: {}", exception.getMessage());
        ExceptionWrapper wrapper = ExceptionWrapper.builder()
                .error("ValidationError")
                .code(HttpStatus.BAD_REQUEST.value())
                .details(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(wrapper);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ExceptionWrapper> handleException(Exception exception) {

        ExceptionWrapper wrapper;
        if (exception instanceof GenericException) {
            GenericException ge = (GenericException) exception;
            wrapper = new ExceptionWrapper(
                    exception.getMessage(),
                    ge.getCode());
        } else if (exception instanceof UndeclaredThrowableException
                && exception.getCause() instanceof GenericException) {
            wrapper = new ExceptionWrapper(
                    exception.getMessage(),
                    ((GenericException) exception.getCause()).getCode());
        } else {
            wrapper = ExceptionWrapper.builder()
                    .error("InternalServerError")
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .details(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                    .build();
            logger.error("UNHANDLED ERROR OCCURRED: {} - CAUSE: {} - AT: {}", exception.getMessage(),
                    exception.getCause(), exception.getStackTrace()[1]);
        }
        return ResponseEntity.status(wrapper.getCode()).body(wrapper);
    }

}

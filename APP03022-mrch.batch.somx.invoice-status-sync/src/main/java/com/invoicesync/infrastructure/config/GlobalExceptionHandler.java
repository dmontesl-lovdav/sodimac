package com.invoicesync.infrastructure.config;

import com.invoicesync.domain.exception.ExternalServiceException;
import com.invoicesync.domain.exception.InvoiceNotFoundException;
import com.invoicesync.domain.exception.SyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ProblemDetail handleInvoiceNotFound(InvoiceNotFoundException ex) {
        log.warn("Invoice not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Invoice Not Found");
        problemDetail.setType(URI.create("https://api.invoicesync.com/errors/invoice-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        if (ex.getInvoiceId() != null) {
            problemDetail.setProperty("invoiceId", ex.getInvoiceId().toString());
        }
        if (ex.getInvoiceNumber() != null) {
            problemDetail.setProperty("invoiceNumber", ex.getInvoiceNumber());
        }

        return problemDetail;
    }

    @ExceptionHandler(SyncException.class)
    public ProblemDetail handleSyncException(SyncException ex) {
        log.error("Sync error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problemDetail.setTitle("Synchronization Error");
        problemDetail.setType(URI.create("https://api.invoicesync.com/errors/sync-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", ex.getErrorCode());

        if (ex.getInvoiceId() != null) {
            problemDetail.setProperty("invoiceId", ex.getInvoiceId().toString());
        }

        return problemDetail;
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalServiceException(ExternalServiceException ex) {
        log.error("External service error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, ex.getMessage());
        problemDetail.setTitle("External Service Error");
        problemDetail.setType(URI.create("https://api.invoicesync.com/errors/external-service-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("serviceName", ex.getServiceName());

        if (ex.getStatusCode() != 0) {
            problemDetail.setProperty("statusCode", ex.getStatusCode());
        }

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed: " + errors);
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://api.invoicesync.com/errors/validation-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
                .toList());

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.invoicesync.com/errors/internal-error"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    private record ValidationError(String field, String message) {}
}

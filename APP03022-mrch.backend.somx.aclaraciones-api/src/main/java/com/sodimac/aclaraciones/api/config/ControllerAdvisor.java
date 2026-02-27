package com.sodimac.aclaraciones.api.config;

import com.sodimac.aclaraciones.api.exception.ExceptionWrapper;
import com.sodimac.aclaraciones.api.exception.GenericException;
import java.lang.reflect.UndeclaredThrowableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice(annotations = RestController.class)
public class ControllerAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvisor.class);

    /* ===================== ResponseStatusException ===================== */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionWrapper> handleResponseStatus(
            ResponseStatusException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ExceptionWrapper(
                        ex.getReason(),
                        ex.getStatusCode().value()));
    }

    /* ===================== GenericException ===================== */
    @ExceptionHandler(GenericException.class)
    public ResponseEntity<ExceptionWrapper> handleGeneric(GenericException ex) {

        return ResponseEntity
                .status(ex.getCode())
                .body(new ExceptionWrapper(
                        ex.getMessage(),
                        ex.getCode()));
    }

    /* ===================== Wrapped GenericException ===================== */
    @ExceptionHandler(UndeclaredThrowableException.class)
    public ResponseEntity<ExceptionWrapper> handleUndeclared(
            UndeclaredThrowableException ex) {

        if (ex.getCause() instanceof GenericException ge) {
            return handleGeneric(ge);
        }

        return handleUnknown(ex);
    }

    /* ===================== Fallback ===================== */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ExceptionWrapper> handleUnknown(Throwable ex) {

        logger.error(
                "UNHANDLED ERROR OCCURRED",
                ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionWrapper(
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}

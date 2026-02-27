package com.sodimac.fiscal.api.config;

import com.sodimac.fiscal.api.exception.ExceptionWrapper;
import com.sodimac.fiscal.api.exception.FiscalErrorResponse;
import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.exception.GenericException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.UndeclaredThrowableException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para controllers REST.
 *
 * Diferencia entre tipos de errores:
 * - FiscalException con código BUS### -> HTTP 400 (Bad Request) - Error de negocio
 * - FiscalException con código ERR### -> HTTP 500 (Internal Server Error) - Error técnico
 * - MethodArgumentNotValidException -> HTTP 400 (Bad Request) - Error de validación
 * - Otras excepciones -> HTTP 500 (Internal Server Error)
 *
 * @author Sodimac Tech Team
 * @version 2.0
 * @since 2025
 */
@ControllerAdvice(annotations = RestController.class)
public class ControllerAdvisor {

    private final Logger logger = LoggerFactory.getLogger(ControllerAdvisor.class);

    /**
     * Maneja excepciones fiscales (FiscalException).
     *
     * - Códigos BUS### (negocio) -> HTTP 400 Bad Request
     * - Códigos ERR### (técnico) -> HTTP 500 Internal Server Error
     */
    @ExceptionHandler(FiscalException.class)
    public ResponseEntity<FiscalErrorResponse> handleFiscalException(
            FiscalException exception,
            HttpServletRequest request) {

        String errorCode = exception.getCode();
        boolean isBusinessError = errorCode != null && errorCode.startsWith("BUS");

        HttpStatus httpStatus = isBusinessError ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        String errorType = isBusinessError
                ? FiscalErrorResponse.ErrorType.BUSINESS.name()
                : FiscalErrorResponse.ErrorType.TECHNICAL.name();

        if (isBusinessError) {
            logger.warn("Error de negocio: [{}] {} - {}",
                    errorCode, exception.getMessage(), exception.getAdditionalInfo());
        } else {
            logger.error("Error técnico: [{}] {} - {}",
                    errorCode, exception.getMessage(), exception.getAdditionalInfo(), exception);
        }

        FiscalErrorResponse response = FiscalErrorResponse.builder()
                .errorCode(errorCode)
                .errorType(errorType)
                .message(exception.getResolvedMessage() != null
                        ? exception.getResolvedMessage()
                        : exception.getMessageCode().getMessage())
                .additionalInfo(exception.getAdditionalInfo())
                .httpStatus(httpStatus.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Maneja errores de validación de @Valid en request bodies.
     * Extrae todos los errores de campo y los retorna en formato legible.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException exception) {
        logger.warn("Errores de validación en request");

        Map<String, String> fieldErrors = new HashMap<>();

        // Extraer todos los errores de campo
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
            logger.debug("Error de validación - Campo: {}, Mensaje: {}", fieldName, errorMessage);
        });

        // Construir respuesta estructurada
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("errorType", FiscalErrorResponse.ErrorType.VALIDATION.name());
        response.put("message", "Error de validación en los campos del request");
        response.put("errors", fieldErrors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Maneja excepciones genéricas y no controladas.
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ExceptionWrapper> handleException(Exception exception) {

        ExceptionWrapper wrapper;
        if (exception instanceof GenericException) {
            wrapper = new ExceptionWrapper(
                    exception.getMessage(),
                    ((GenericException) exception).getCode());
        } else if (exception instanceof UndeclaredThrowableException
                && exception.getCause() instanceof GenericException) {
            wrapper = new ExceptionWrapper(
                    exception.getMessage(),
                    ((GenericException) exception.getCause()).getCode());
        } else {
            wrapper = new ExceptionWrapper(
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
            logger.error("UNHANDLED ERROR OCCURRED: {} - CAUSE: {} - AT: {}",
                    exception.getMessage(), exception.getCause(),
                    exception.getStackTrace().length > 0 ? exception.getStackTrace()[0] : "unknown");
        }
        return ResponseEntity.status(wrapper.getCode()).body(wrapper);
    }
}


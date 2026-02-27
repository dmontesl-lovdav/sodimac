package com.sodimac.fiscal.api.exception;

import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API de facturación.
 *
 * Captura excepciones y las transforma en respuestas HTTP consistentes
 * con mensajes de error legibles para el cliente.
 *
 * Los mensajes se obtienen del servicio de catálogos para soporte multi-idioma.
 *
 * @author Sodimac Tech Team
 * @since 2025-11-13
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageCatalogService messageCatalog;

    /**
     * Maneja errores de validación de @Valid en request bodies.
     *
     * Cuando un DTO con @Valid falla las validaciones de Bean Validation,
     * esta excepción captura todos los errores de campo y los retorna
     * en un formato legible.
     *
     * @param ex Excepción de validación
     * @param request Request HTTP
     * @return ResponseEntity con detalles de los errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.warn("Errores de validación en request: {}", request.getDescription(false));

        Map<String, String> fieldErrors = new HashMap<>();

        // Extraer todos los errores de campo
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
            log.debug("Error de validación - Campo: {}, Mensaje: {}", fieldName, errorMessage);
        });

        // Obtener mensaje del catálogo
        String message = messageCatalog.getMessage(FiscalMessageCode.ERR035);

        // Construir respuesta estructurada
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("code", FiscalMessageCode.ERR035.getCode());
        response.put("message", message);
        response.put("errors", fieldErrors);
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Maneja excepciones de negocio personalizadas (FiscalException).
     *
     * Estas excepciones ya tienen códigos de mensaje de negocio (BUS###, ERR###)
     * y mensajes resueltos desde el catálogo (BD o enum fallback).
     *
     * @param ex Excepción de negocio
     * @param request Request HTTP
     * @return ResponseEntity con detalles del error de negocio
     */
    @ExceptionHandler(FiscalException.class)
    public ResponseEntity<Map<String, Object>> handleFiscalException(
            FiscalException ex,
            WebRequest request) {

        log.error("Error de negocio: Código={}, Mensaje={}",
                ex.getCode(), ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Business Error");
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Maneja excepciones genéricas no capturadas por otros handlers.
     *
     * Este es el handler de último recurso para cualquier excepción
     * no manejada específicamente.
     *
     * @param ex Excepción genérica
     * @param request Request HTTP
     * @return ResponseEntity con detalles del error interno
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex,
            WebRequest request) {

        log.error("Error no manejado: {}", ex.getMessage(), ex);

        // Obtener mensaje del catálogo
        String message = messageCatalog.getMessage(FiscalMessageCode.ERR036);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("code", FiscalMessageCode.ERR036.getCode());
        response.put("message", message);
        response.put("path", request.getDescription(false).replace("uri=", ""));

        // En desarrollo, incluir stack trace
        if (log.isDebugEnabled()) {
            response.put("debugMessage", ex.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Maneja excepciones de argumentos ilegales.
     *
     * Ocurre cuando se pasan valores inválidos a métodos
     * (ej: enums inválidos, valores fuera de rango).
     *
     * @param ex Excepción de argumento ilegal
     * @param request Request HTTP
     * @return ResponseEntity con detalles del error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.warn("Argumento ilegal: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Invalid Argument");
        response.put("code", FiscalMessageCode.ERR003.getCode());
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}

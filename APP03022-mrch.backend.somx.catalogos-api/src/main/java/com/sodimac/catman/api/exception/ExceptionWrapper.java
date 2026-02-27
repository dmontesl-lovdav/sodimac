package com.sodimac.catman.api.exception;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Standard error response wrapper for API errors.
 * STM-1295 ET-07: Error format specification.
 */
@Getter
@AllArgsConstructor
@Builder
@Schema(description = "Wrapper para respuestas de error - STM-1295 ET-07")
public class ExceptionWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Tipo de error", example = "ValidationError")
    private final String error;

    @Schema(description = "Código HTTP de error", example = "400")
    private final int code;

    @Schema(description = "Descripción detallada del error", example = "El campo idElementoOrigen es obligatorio")
    private final String details;

    /**
     * Legacy constructor for backward compatibility.
     */
    public ExceptionWrapper(String message, int code) {
        this.error = mapCodeToErrorType(code);
        this.code = code;
        this.details = message;
    }

    /**
     * Map HTTP code to error type string.
     */
    private static String mapCodeToErrorType(int code) {
        switch (code) {
            case 400:
                return "ValidationError";
            case 401:
                return "UnauthorizedError";
            case 403:
                return "ForbiddenError";
            case 404:
                return "NotFoundError";
            case 409:
                return "ConflictError";
            case 500:
            default:
                return "InternalServerError";
        }
    }

    // Alias for backward compatibility
    public String getMessage() {
        return details;
    }
}

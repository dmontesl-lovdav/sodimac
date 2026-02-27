package com.sodimac.fiscal.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Respuesta estándar para errores fiscales.
 *
 * Diferencia entre errores de negocio (BUS###) y técnicos (ERR###)
 * para facilitar el manejo en el cliente.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FiscalErrorResponse {

    @Schema(description = "Código del error", example = "BUS034")
    private final String errorCode;

    @Schema(description = "Tipo de error: BUSINESS o TECHNICAL", example = "BUSINESS")
    private final String errorType;

    @Schema(description = "Mensaje del error", example = "El documento con UUID ya se encuentra registrado en el sistema")
    private final String message;

    @Schema(description = "Información adicional del contexto", example = "UUID: 123e4567-e89b-12d3-a456-426614174000")
    private final String additionalInfo;

    @Schema(description = "Código HTTP de respuesta", example = "400")
    private final int httpStatus;

    @Schema(description = "Timestamp del error")
    private final LocalDateTime timestamp;

    @Schema(description = "Path del endpoint que generó el error", example = "/api/v1/invoice/register")
    private final String path;

    /**
     * Tipos de error soportados.
     */
    public enum ErrorType {
        /**
         * Errores de negocio (BUS###) - HTTP 400 Bad Request.
         * El cliente puede corregir el request.
         */
        BUSINESS,

        /**
         * Errores técnicos (ERR###) - HTTP 500 Internal Server Error.
         * Problema interno del servidor.
         */
        TECHNICAL,

        /**
         * Errores de validación de entrada (@Valid).
         * HTTP 400 Bad Request.
         */
        VALIDATION
    }
}

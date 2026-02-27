package com.sodimac.fiscal.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO con el resultado de la validación de transición de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTrainValidationResult {

    /**
     * Indica si la validación fue exitosa (transición permitida).
     */
    private boolean valid;

    /**
     * Código de error si la validación falla (WRN7010, WRN7011).
     */
    private String errorCode;

    /**
     * Mensaje de error descriptivo.
     */
    private String errorMessage;

    /**
     * Crea un resultado exitoso (transición permitida).
     */
    public static StatusTrainValidationResult valid() {
        return StatusTrainValidationResult.builder()
                .valid(true)
                .build();
    }

    /**
     * Crea un resultado de error cuando el estatus origen no está catalogado.
     */
    public static StatusTrainValidationResult sourceNotFound() {
        return StatusTrainValidationResult.builder()
                .valid(false)
                .errorCode("WRN7010")
                .errorMessage("El estatus origen no existe catalogado. Por favor, valide la información antes de continuar.")
                .build();
    }

    /**
     * Crea un resultado de error cuando la transición no está permitida.
     */
    public static StatusTrainValidationResult transitionNotAllowed() {
        return StatusTrainValidationResult.builder()
                .valid(false)
                .errorCode("WRN7011")
                .errorMessage("El estatus destino no existe catalogado. Por favor, valide la información antes de continuar.")
                .build();
    }

    /**
     * Crea un resultado de error por falla de comunicación con el servicio.
     */
    public static StatusTrainValidationResult serviceError(String message) {
        return StatusTrainValidationResult.builder()
                .valid(false)
                .errorCode("ERR5001")
                .errorMessage("Error de comunicación con servicio de tren de estatus: " + message)
                .build();
    }
}

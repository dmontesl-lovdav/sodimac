package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de respuesta para validación de transiciones.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Schema(description = "Respuesta de validación de transición")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTrainValidationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;

    @Schema(description = "Indica si la transición es válida", example = "true")
    private boolean valid;

    @Schema(description = "Código de error si la validación falla", example = "WRN7011")
    private String code;

    @Schema(description = "Mensaje descriptivo", example = "Transición válida")
    private String message;

    @Schema(description = "Datos de la transición encontrada")
    private StatusTrainDto data;

    /**
     * Crea una respuesta exitosa con la transición encontrada.
     */
    public static StatusTrainValidationResponse success(StatusTrainDto data) {
        return StatusTrainValidationResponse.builder()
                .success(true)
                .valid(true)
                .data(data)
                .build();
    }

    /**
     * Crea una respuesta de error cuando el estatus origen no está catalogado.
     */
    public static StatusTrainValidationResponse sourceNotFound() {
        return StatusTrainValidationResponse.builder()
                .success(false)
                .valid(false)
                .code("WRN7010")
                .message("El estatus origen no existe catalogado. Por favor, valide la información antes de continuar.")
                .build();
    }

    /**
     * Crea una respuesta de error cuando la transición no está permitida.
     */
    public static StatusTrainValidationResponse transitionNotAllowed() {
        return StatusTrainValidationResponse.builder()
                .success(false)
                .valid(false)
                .code("WRN7011")
                .message("El estatus destino no existe catalogado. Por favor, valide la información antes de continuar.")
                .build();
    }
}

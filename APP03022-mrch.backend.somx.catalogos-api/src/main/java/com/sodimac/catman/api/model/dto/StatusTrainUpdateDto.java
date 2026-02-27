package com.sodimac.catman.api.model.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para actualizar una regla de transición de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Schema(description = "Datos para actualizar una regla de transición")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTrainUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Nuevo estatus origen de la transición", example = "3", required = true)
    @NotNull(message = "El sourceStatus es requerido")
    private Integer sourceStatus;

    @Schema(description = "Nuevo estatus destino permitido", example = "5", required = true)
    @NotNull(message = "El targetStatus es requerido")
    private Integer targetStatus;

    @Schema(description = "ID del usuario que actualiza la regla", example = "1002", required = true)
    @NotNull(message = "El updatedBy es requerido")
    private Long updatedBy;
}

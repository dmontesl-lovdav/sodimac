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
 * DTO para crear una nueva regla de transición de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Schema(description = "Datos para crear una regla de transición")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTrainCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)", example = "1", required = true)
    @NotNull(message = "El optionId es requerido")
    private Integer optionId;

    @Schema(description = "Estatus origen de la transición", example = "3", required = true)
    @NotNull(message = "El sourceStatus es requerido")
    private Integer sourceStatus;

    @Schema(description = "Estatus destino permitido", example = "4", required = true)
    @NotNull(message = "El targetStatus es requerido")
    private Integer targetStatus;

    @Schema(description = "ID del usuario que crea la regla", example = "1001", required = true)
    @NotNull(message = "El createdBy es requerido")
    private Long createdBy;
}

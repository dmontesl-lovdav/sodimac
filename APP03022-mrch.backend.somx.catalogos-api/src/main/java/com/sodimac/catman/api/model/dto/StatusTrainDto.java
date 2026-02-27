package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa una regla de transición de estatus.
 *
 * @author Sodimac Tech Team
 * @since STM-1166
 */
@Schema(description = "Regla de transición de estatus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTrainDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único de la regla", example = "45")
    private Integer id;

    @Schema(description = "ID de la opción (1=Factura, 2=NC, 3=Pagos, 4=CartaPorte)", example = "1")
    private Integer optionId;

    @Schema(description = "Estatus origen de la transición", example = "3")
    private Integer sourceStatus;

    @Schema(description = "Estatus destino permitido", example = "4")
    private Integer targetStatus;

    @Schema(description = "ID del usuario que creó la regla", example = "1001")
    private Long createdBy;

    @Schema(description = "Fecha de creación", example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID del usuario que actualizó la regla", example = "1002")
    private Long updatedBy;

    @Schema(description = "Fecha de última actualización", example = "2025-01-10T15:30:00")
    private LocalDateTime updatedAt;
}

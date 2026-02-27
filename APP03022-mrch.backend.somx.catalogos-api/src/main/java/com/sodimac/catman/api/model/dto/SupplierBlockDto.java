package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para bloqueo de proveedor.
 * STM-1224: Bloqueo de proveedores por rango de fechas.
 */
@Schema(description = "Bloqueo de proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBlockDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID del bloqueo", example = "1")
    private Integer id;

    @Schema(description = "Numero de proveedor", example = "PROV001")
    private String supplierNumber;

    @Schema(description = "Fecha de inicio del bloqueo", example = "2025-01-01")
    private LocalDate validFrom;

    @Schema(description = "Fecha de fin del bloqueo", example = "2025-12-31")
    private LocalDate validTo;

    @Schema(description = "Razon del bloqueo", example = "Incumplimiento de contrato")
    private String blockReason;

    @Schema(description = "Estado: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;

    @Schema(description = "Indica si el bloqueo esta vigente actualmente", example = "true")
    private Boolean currentlyBlocked;

    @Schema(description = "Fecha de creacion")
    private LocalDateTime createdAt;

    @Schema(description = "Usuario que creo el registro")
    private String createdBy;

    @Schema(description = "Fecha de actualizacion")
    private LocalDateTime updatedAt;

    @Schema(description = "Usuario que actualizo el registro")
    private String updatedBy;
}

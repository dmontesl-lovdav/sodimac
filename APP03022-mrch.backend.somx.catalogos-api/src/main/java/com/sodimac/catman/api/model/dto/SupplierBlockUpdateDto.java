package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para actualizar un bloqueo de proveedor.
 * STM-1224: Bloqueo de proveedores por rango de fechas.
 */
@Schema(description = "Datos para actualizar un bloqueo de proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBlockUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Fecha de inicio del bloqueo", example = "2025-01-01")
    private LocalDate validFrom;

    @Schema(description = "Fecha de fin del bloqueo", example = "2025-12-31")
    private LocalDate validTo;

    @Schema(description = "Razon del bloqueo", example = "Incumplimiento de contrato")
    @Size(max = 255, message = "La razon del bloqueo no puede exceder 255 caracteres")
    private String blockReason;

    @Schema(description = "Estado: 1=Activo, 0=Inactivo", example = "1")
    private Integer status;
}

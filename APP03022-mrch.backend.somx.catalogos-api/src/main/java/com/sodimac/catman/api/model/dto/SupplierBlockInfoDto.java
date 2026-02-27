package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para informacion de bloqueo de proveedor.
 * STM-831: Ajustar servicio del catalogo de proveedores.
 */
@Schema(description = "Informacion de bloqueo de proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBlockInfoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Fecha de inicio del bloqueo", example = "2025-12-01")
    private LocalDate validFrom;

    @Schema(description = "Fecha de fin del bloqueo", example = "2025-12-31")
    private LocalDate validTo;

    @Schema(description = "Razon del bloqueo", example = "Bloqueo temporal por auditoria")
    private String blockReason;
}

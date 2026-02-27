package com.sodimac.catman.api.model.dto;

import java.io.Serializable;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para crear un bloqueo de proveedor.
 * STM-1224: Bloqueo de proveedores por rango de fechas.
 */
@Schema(description = "Datos para crear un bloqueo de proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierBlockCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Numero de proveedor", example = "PROV001", required = true)
    @NotBlank(message = "El numero de proveedor es requerido")
    @Size(max = 20, message = "El numero de proveedor no puede exceder 20 caracteres")
    private String supplierNumber;

    @Schema(description = "Fecha de inicio del bloqueo", example = "2025-01-01", required = true)
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate validFrom;

    @Schema(description = "Fecha de fin del bloqueo", example = "2025-12-31", required = true)
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate validTo;

    @Schema(description = "Razon del bloqueo", example = "Incumplimiento de contrato")
    @Size(max = 255, message = "La razon del bloqueo no puede exceder 255 caracteres")
    private String blockReason;
}

package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para request de actualización de factura o nota de crédito (STM-339).
 *
 * Permite actualizar:
 * - Estatus del documento (obligatorio)
 * - Usuario que realiza la actualización (obligatorio)
 * - Información de addenda (opcional)
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para actualizar factura o nota de crédito")
public class InvoiceUpdateRequest {

    @NotNull(message = "UUID es obligatorio")
    @Schema(description = "UUID del documento fiscal a actualizar", required = true)
    private UUID uuid;

    @NotNull(message = "Número de proveedor es obligatorio")
    @Schema(description = "Número de proveedor (Supplier Number)", required = true, example = "1234567890")
    private BigDecimal numeroProveedor;

    @NotNull(message = "Estatus es obligatorio")
    @Schema(description = "Nuevo estatus del documento (código numérico)", required = true, example = "3")
    private Integer estatus;

    @NotNull(message = "ID de usuario actualización es obligatorio")
    @Schema(description = "ID del usuario que realiza la actualización", required = true, example = "12345")
    private Long idUsuarioActualizacion;

    @Schema(description = "Datos de addenda a actualizar (opcional)")
    private AddendaUpdateDto addenda;
}

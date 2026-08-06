package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para request de actualización de estatus con validación de transición (STM-410).
 *
 * Requiere tanto el estatus origen como el destino para validar contra el tren de estatus.
 * La fecha de contabilización solo se guarda cuando el estatus destino es 7 (Pendiente de Pago).
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para actualización de estatus con validación de transición")
public class InvoiceStatusUpdateRequest {

    @NotNull(message = "Número de proveedor es obligatorio")
    @Schema(
            description = "Número de proveedor (Supplier Number)",
            required = true,
            example = "1234567890"
    )
    private BigDecimal numeroProveedor;

    @NotNull(message = "Estatus origen es obligatorio")
    @Schema(
            description = "Estatus actual del documento (origen de la transición)",
            required = true,
            example = "3"
    )
    private Integer estatusOrigen;

    @NotNull(message = "Estatus destino es obligatorio")
    @Schema(
            description = "Nuevo estatus deseado (destino de la transición)",
            required = true,
            example = "7"
    )
    private Integer estatusDestino;

    @NotNull(message = "ID de usuario actualización es obligatorio")
    @Schema(
            description = "UUID del usuario que realiza la actualización (sub del token). Sistema/batch: 00000000-0000-0000-0000-000000000000",
            required = true,
            example = "9f7affd6-fc0f-4f76-bcaf-65b1af36a47d"
    )
    private String idUsuarioActualizacion;

    @Schema(
            description = "Fecha de contabilización. Solo se guarda si estatusDestino = 7 (Pendiente de Pago)",
            required = false,
            example = "2025-06-15"
    )
    private LocalDate fechaContabilizacion;

    @Schema(
            description = "Comentario o razón del cambio de estatus",
            required = false,
            example = "Aprobado para pago tras validación de documentos"
    )
    private String comentario;
}

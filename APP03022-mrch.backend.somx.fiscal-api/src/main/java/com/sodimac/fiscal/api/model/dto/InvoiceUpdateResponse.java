package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para response de actualización de factura o nota de crédito (STM-339).
 *
 * @author Sodimac Tech Team
 * @since 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de actualización de factura o nota de crédito")
public class InvoiceUpdateResponse {

    @Schema(description = "Indica si la operación fue exitosa")
    private boolean success;

    @Schema(description = "Código de respuesta (BUS3xxx para actualización)")
    private String code;

    @Schema(description = "Mensaje descriptivo del resultado")
    private String message;

    @Schema(description = "UUID del documento actualizado")
    private UUID invoiceUuid;

    @Schema(description = "UUID fiscal (del TimbreFiscalDigital)")
    private UUID fiscalUuid;

    @Schema(description = "Tipo de documento: I (Factura) o E (Nota de Crédito)")
    private String documentType;

    @Schema(description = "Estatus anterior del documento")
    private Integer estatusAnterior;

    @Schema(description = "Nuevo estatus del documento")
    private Integer estatusNuevo;

    @Schema(description = "Nombre del estatus nuevo")
    private String estatusNuevoNombre;

    @Schema(description = "Indica si se actualizó la addenda")
    private boolean addendaActualizada;

    @Schema(description = "Fecha y hora de la actualización")
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor para respuesta exitosa.
     */
    public static InvoiceUpdateResponse success(
            String code,
            String message,
            UUID invoiceUuid,
            UUID fiscalUuid,
            String documentType,
            Integer estatusAnterior,
            Integer estatusNuevo,
            String estatusNuevoNombre,
            boolean addendaActualizada) {

        return InvoiceUpdateResponse.builder()
                .success(true)
                .code(code)
                .message(message)
                .invoiceUuid(invoiceUuid)
                .fiscalUuid(fiscalUuid)
                .documentType(documentType)
                .estatusAnterior(estatusAnterior)
                .estatusNuevo(estatusNuevo)
                .estatusNuevoNombre(estatusNuevoNombre)
                .addendaActualizada(addendaActualizada)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Constructor para respuesta de error.
     */
    public static InvoiceUpdateResponse error(String code, String message) {
        return InvoiceUpdateResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }
}

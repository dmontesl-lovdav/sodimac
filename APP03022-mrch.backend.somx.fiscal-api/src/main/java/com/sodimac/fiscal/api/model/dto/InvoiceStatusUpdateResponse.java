package com.sodimac.fiscal.api.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para response de actualización de estatus con validación de transición (STM-410).
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de actualización de estatus con validación de transición")
public class InvoiceStatusUpdateResponse {

    @Schema(description = "Indica si la operación fue exitosa")
    private boolean success;

    @Schema(description = "Código de respuesta")
    private String code;

    @Schema(description = "Mensaje descriptivo del resultado")
    private String message;

    @Schema(description = "UUID del documento actualizado")
    private UUID invoiceUuid;

    @Schema(description = "UUID fiscal (del TimbreFiscalDigital)")
    private UUID fiscalUuid;

    @Schema(description = "Tipo de documento: I (Factura), E (Nota de Crédito), T (Carta Porte), P (Pago)")
    private String documentType;

    @Schema(description = "Estatus anterior del documento")
    private Integer estatusAnterior;

    @Schema(description = "Nuevo estatus del documento")
    private Integer estatusNuevo;

    @Schema(description = "Nombre del estatus nuevo")
    private String estatusNuevoNombre;

    @Schema(description = "Fecha de contabilización (solo si estatusNuevo = 7)")
    private LocalDate fechaContabilizacion;

    @Schema(description = "Fecha y hora de la actualización")
    private LocalDateTime fechaActualizacion;

    /**
     * Constructor para respuesta exitosa.
     */
    public static InvoiceStatusUpdateResponse success(
            String code,
            String message,
            UUID invoiceUuid,
            UUID fiscalUuid,
            String documentType,
            Integer estatusAnterior,
            Integer estatusNuevo,
            String estatusNuevoNombre,
            LocalDate fechaContabilizacion) {

        return InvoiceStatusUpdateResponse.builder()
                .success(true)
                .code(code)
                .message(message)
                .invoiceUuid(invoiceUuid)
                .fiscalUuid(fiscalUuid)
                .documentType(documentType)
                .estatusAnterior(estatusAnterior)
                .estatusNuevo(estatusNuevo)
                .estatusNuevoNombre(estatusNuevoNombre)
                .fechaContabilizacion(fechaContabilizacion)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Constructor para respuesta de error.
     */
    public static InvoiceStatusUpdateResponse error(String code, String message) {
        return InvoiceStatusUpdateResponse.builder()
                .success(false)
                .code(code)
                .message(message)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Constructor para error de validación de transición.
     */
    public static InvoiceStatusUpdateResponse transitionNotAllowed(
            Integer estatusOrigen,
            Integer estatusDestino) {
        return InvoiceStatusUpdateResponse.builder()
                .success(false)
                .code("WRN7011")
                .message(String.format("Transición de estatus no permitida: %d -> %d",
                        estatusOrigen, estatusDestino))
                .estatusAnterior(estatusOrigen)
                .estatusNuevo(estatusDestino)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    /**
     * Constructor para error cuando el estatus origen no está catalogado.
     */
    public static InvoiceStatusUpdateResponse sourceNotCataloged(Integer estatusOrigen) {
        return InvoiceStatusUpdateResponse.builder()
                .success(false)
                .code("WRN7010")
                .message(String.format("Estatus origen no catalogado: %d", estatusOrigen))
                .estatusAnterior(estatusOrigen)
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }
}

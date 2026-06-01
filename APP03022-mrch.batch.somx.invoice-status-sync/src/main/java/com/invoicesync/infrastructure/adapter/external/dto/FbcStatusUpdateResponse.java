package com.invoicesync.infrastructure.adapter.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de actualización de estatus en FBC Portal.
 *
 * Endpoint: PUT https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/{uuid}/status
 *
 * Respuesta según contrato de fiscal-api:
 * - success: Indica si la operación fue exitosa
 * - code: Código de respuesta del servicio
 * - message: Mensaje descriptivo del resultado
 * - invoiceUuid: UUID de la factura actualizada
 * - fiscalUuid: UUID fiscal de la factura
 * - estatusAnterior: Estado anterior de la factura
 * - estatusNuevo: Estado nuevo de la factura
 * - estatusNuevoNombre: Nombre descriptivo del nuevo estado
 * - fechaActualizacion: Fecha y hora de la actualización
 */
public record FbcStatusUpdateResponse(
        boolean success,
        String code,
        String message,

        @JsonProperty("invoiceUuid")
        String invoiceUuid,

        @JsonProperty("fiscalUuid")
        String fiscalUuid,

        @JsonProperty("documentType")
        String documentType,

        @JsonProperty("estatusAnterior")
        Integer estatusAnterior,

        @JsonProperty("estatusNuevo")
        Integer estatusNuevo,

        @JsonProperty("estatusNuevoNombre")
        String estatusNuevoNombre,

        @JsonProperty("fechaContabilizacion")
        LocalDateTime fechaContabilizacion,

        @JsonProperty("fechaActualizacion")
        LocalDateTime fechaActualizacion
) {

    /**
     * Verifica si la actualización fue exitosa.
     *
     * @return true si la actualización fue exitosa
     */
    public boolean isSuccessful() {
        return success;
    }

    /**
     * Obtiene un mensaje descriptivo del resultado.
     *
     * @return Mensaje descriptivo
     */
    public String getResultMessage() {
        if (success) {
            return String.format("Estado actualizado de %d a %d (%s)",
                    estatusAnterior, estatusNuevo, estatusNuevoNombre);
        }
        return String.format("Error: %s - %s", code, message);
    }
}

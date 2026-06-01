package com.invoicesync.infrastructure.adapter.external.dto;

/**
 * DTO para la petición de actualización de estatus en FBC Portal.
 *
 * Endpoint: PUT https://dev.fbusinesscenter.com/ppsomx/fiscal/invoices/{uuid}/status
 *
 * Campos según contrato de fiscal-api:
 * - numeroProveedor: ID del proveedor
 * - estatusOrigen: Estado actual de la factura en FBC
 * - estatusDestino: Estado al que se quiere transicionar
 * - idUsuarioActualizacion: ID del usuario que realiza la actualización (sistema = 1)
 * - comentario: Comentario opcional sobre la transición
 */
public record FbcStatusUpdateRequest(
        int numeroProveedor,
        int estatusOrigen,
        int estatusDestino,
        int idUsuarioActualizacion,
        String comentario
) {

    /**
     * Constructor para actualizaciones automáticas del sistema batch.
     *
     * @param numeroProveedor ID del proveedor
     * @param estatusOrigen Estado actual en FBC
     * @param estatusDestino Estado destino en FBC
     * @return Request configurado para el sistema batch
     */
    public static FbcStatusUpdateRequest forBatchUpdate(
            int numeroProveedor,
            int estatusOrigen,
            int estatusDestino) {
        return new FbcStatusUpdateRequest(
                numeroProveedor,
                estatusOrigen,
                estatusDestino,
                1, // ID del sistema batch
                "Actualización automática por sincronización de estados"
        );
    }
}

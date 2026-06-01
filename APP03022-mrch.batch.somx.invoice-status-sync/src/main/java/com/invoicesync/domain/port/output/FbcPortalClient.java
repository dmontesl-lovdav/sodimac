package com.invoicesync.domain.port.output;

import com.invoicesync.domain.enums.InvoiceFlowStatus;
import com.invoicesync.domain.model.FbcInvoice;

import java.util.List;
import java.util.Optional;

public interface FbcPortalClient {

    List<FbcInvoice> fetchInvoicesByStatus(InvoiceFlowStatus status);

    List<FbcInvoice> fetchAllPendingInvoices();

    boolean updateInvoiceStatus(String idProveedor, String documentNumber, InvoiceFlowStatus newStatus);

    /**
     * Actualiza el estado de una factura en FBC Portal usando el endpoint PUT /invoices/{uuid}/status.
     *
     * Este método utiliza el endpoint oficial de fiscal-api para actualizar el estado de una factura
     * mediante transición de estados (tren de estatus).
     *
     * @param uuid UUID de la factura en FBC
     * @param numeroProveedor Número del proveedor
     * @param currentFbcStatus Estado actual de la factura en FBC (estatusOrigen)
     * @param newFbcStatus Nuevo estado de la factura en FBC (estatusDestino)
     * @return Optional con mensaje de éxito si la actualización fue exitosa, Optional.empty() si falló
     */
    Optional<String> updateInvoiceStatusByUuid(String uuid, int numeroProveedor, int currentFbcStatus, int newFbcStatus);

    boolean isServiceAvailable();
}

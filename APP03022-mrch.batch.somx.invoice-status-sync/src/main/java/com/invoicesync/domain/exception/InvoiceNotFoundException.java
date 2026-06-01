package com.invoicesync.domain.exception;

/**
 * Excepción lanzada cuando una factura no se encuentra en el sistema.
 */
public class InvoiceNotFoundException extends RuntimeException {

    private final Long invoiceId;
    private final String invoiceNumber;

    public InvoiceNotFoundException(Long invoiceId) {
        super("Factura no encontrada con ID: " + invoiceId);
        this.invoiceId = invoiceId;
        this.invoiceNumber = null;
    }

    public InvoiceNotFoundException(String invoiceNumber) {
        super("Factura no encontrada con número: " + invoiceNumber);
        this.invoiceId = null;
        this.invoiceNumber = invoiceNumber;
    }

    public InvoiceNotFoundException(Long invoiceId, String invoiceNumber) {
        super("Factura no encontrada - ID: " + invoiceId + ", Número: " + invoiceNumber);
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }
}

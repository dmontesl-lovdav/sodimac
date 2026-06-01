package com.invoicesync.domain.exception;

/**
 * Excepción lanzada durante errores en el proceso de sincronización.
 */
public class SyncException extends RuntimeException {

    private final String errorCode;
    private final Long invoiceId;

    public SyncException(String message) {
        super(message);
        this.errorCode = "SYNC_ERROR";
        this.invoiceId = null;
    }

    public SyncException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.invoiceId = null;
    }

    public SyncException(String message, String errorCode, Long invoiceId) {
        super(message);
        this.errorCode = errorCode;
        this.invoiceId = invoiceId;
    }

    public SyncException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SYNC_ERROR";
        this.invoiceId = null;
    }

    public SyncException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.invoiceId = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }
}

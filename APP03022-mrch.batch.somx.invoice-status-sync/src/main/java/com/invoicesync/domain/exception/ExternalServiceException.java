package com.invoicesync.domain.exception;

/**
 * Excepción lanzada cuando hay errores al comunicarse con servicios externos.
 */
public class ExternalServiceException extends RuntimeException {

    private final String serviceName;
    private final int statusCode;

    public ExternalServiceException(String message, String serviceName) {
        super(message);
        this.serviceName = serviceName;
        this.statusCode = 0;
    }

    public ExternalServiceException(String message, String serviceName, int statusCode) {
        super(message);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public ExternalServiceException(String message, String serviceName, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.statusCode = 0;
    }

    public ExternalServiceException(String message, String serviceName, int statusCode, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

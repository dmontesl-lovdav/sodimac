package com.sodimac.batch.fiscal.download.service;

/**
 * Error PERMANENTE de estructura del CFDI (XML no parseable, sin UUID de timbre):
 * reprocesar el documento no lo va a arreglar, por lo que el batch lo marca en el
 * tren de estatus como estructura inválida / error.
 * Cualquier otra excepción del desglose (p.ej. DataAccessException por BD caída)
 * se considera TRANSITORIA: el documento permanece en su estatus de entrada y se
 * reintenta en la siguiente corrida.
 */
public class CfdiEstructuraException extends Exception {
    public CfdiEstructuraException(String message) {
        super(message);
    }

    public CfdiEstructuraException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.invoicesync.domain.port.output;

public interface I213Repository {

    int validateDocumentoAP(String idProveedor, String numeroDocumento);

    int validateDocumentoPagado(String idProveedor, String numeroDocumento);
}

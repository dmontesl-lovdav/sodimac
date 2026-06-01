package com.invoicesync.domain.port.output;

public interface SodimacSapRepository {

    boolean existsInEnviosAp(String codigoProveedor, String numeroDocumento, String uuid);

    int countByProveedorAndDocumento(String codigoProveedor, String numeroDocumento, String uuid);
}

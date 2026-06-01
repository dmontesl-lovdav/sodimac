package com.invoicesync.domain.port.output;

public interface SapitoRepository {

    boolean isPendingToSendToI213(String codigoProveedor, String numeroDocumento, String uuid);

    boolean wasSentToI213(String codigoProveedor, String uuid);

    int getFlagEnviado(String codigoProveedor, String uuid);
}

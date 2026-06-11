package com.invoicesync.infrastructure.adapter.external;

/**
 * Nombres de estatus del Tren de Estatus v1.0 (Factura, option_id=1).
 *
 * Fuente de verdad: BD shared_catalogs.status_train + enum fiscal-api InvoiceStatus.
 * El batch ya NO remapea interno→FBC: empuja directo el código del tren a fiscal-api.
 * Esta clase solo resuelve el nombre legible para logs.
 */
public class FbcStatusMapper {

    private FbcStatusMapper() {
    }

    /**
     * Nombre descriptivo del estatus v1.0 para logging.
     */
    public static String getFbcStatusName(int statusCode) {
        return switch (statusCode) {
            case 1 -> "Rechazo Comercial";
            case 2 -> "Recibido Parcial";
            case 3 -> "Recibida";
            case 4 -> "En proceso de descarga";
            case 5 -> "Desglose de factura";
            case 6 -> "Error en el desglose";
            case 7 -> "Pendiente registro en SAPITO";
            case 8 -> "Pendiente de envío a i213";
            case 9 -> "Factura enviada a i213";
            case 10 -> "Pendiente de contabilizar";
            case 11 -> "Pendiente de Pago";
            case 12 -> "Pendiente de complemento";
            case 13 -> "Completado";
            case 14 -> "Rechazo Contable";
            case 15 -> "No válido fiscal";
            case 16 -> "Estructura inválida";
            case 17 -> "Error envío i213";
            case 18 -> "Pago Manual";
            default -> "Estado Desconocido (" + statusCode + ")";
        };
    }
}

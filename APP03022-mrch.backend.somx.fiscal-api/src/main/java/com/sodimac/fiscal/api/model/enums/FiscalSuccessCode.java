package com.sodimac.fiscal.api.model.enums;

/**
 * Enumeración de códigos de éxito para operaciones de negocio fiscal.
 *
 * Cada código contiene:
 * - code: Clave del catálogo (ej: RES004)
 * - message: Mensaje fallback para uso sin conexión a catalogos-api
 *
 * @author Sodimac Tech Team
 * @version 2.0
 * @since 2025
 */
public enum FiscalSuccessCode {

    // ========== REGISTROS GENERALES (RES001-RES003) ==========
    RES001("RES001", "El registro del proveedor {0} se realizó exitosamente."),
    RES002("RES002", "El reporte se ha descargado exitosamente."),
    RES003("RES003", "El registro de la guía de embarque se realizó exitosamente."),

    // ========== REGISTROS EXITOSOS (RES004-RES008) ==========
    RES004("RES004", "Factura registrada exitosamente"),
    RES005("RES005", "Factura registrada exitosamente - Pendiente de Addenda"),
    RES006("RES006", "Nota de Crédito registrada exitosamente"),
    RES007("RES007", "Nota de Crédito registrada exitosamente - Pendiente de Addenda"),
    RES008("RES008", "Complemento de Pago registrado exitosamente"),

    // ========== ACTUALIZACIONES EXITOSAS (RES009-RES012) ==========
    RES009("RES009", "Factura actualizada exitosamente"),
    RES010("RES010", "Factura actualizada exitosamente - Addenda actualizada"),
    RES011("RES011", "Nota de Crédito actualizada exitosamente"),
    RES012("RES012", "Nota de Crédito actualizada exitosamente - Addenda actualizada");

    private final String code;
    private final String message;

    FiscalSuccessCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", code, message);
    }
}

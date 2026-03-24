package com.sodimac.fiscal.api.model.enums;

/**
 * Enumeración de acciones de auditoría para el servicio de bitácora (auditoria-api).
 *
 * Cada constante representa un punto de trazabilidad en los flujos de negocio.
 * Se usa como segundo parámetro en {@code auditoriaApiService.logActivity()}.
 *
 * @author Sodimac Tech Team
 * @since STM-704
 */
public enum AuditAction {

    // ========== REGISTRO DE FACTURA/NC (registerInvoice) ==========
    REGISTRO_REQUEST("REGISTRO_REQUEST", "Inicio de registro de factura/NC"),
    LEER_ARCHIVO_XML("LEER_ARCHIVO_XML", "Lectura del archivo XML recibido"),
    DETECTAR_TIPO_DOCUMENTO("DETECTAR_TIPO_DOCUMENTO", "Detección del tipo de documento fiscal"),
    PROCESAR_XML_CFDI("PROCESAR_XML_CFDI", "Procesamiento y parseo del XML CFDI"),
    VALIDAR_SERIE_FOLIO("VALIDAR_SERIE_FOLIO", "Validación de serie y folio del documento"),
    VALIDAR_VERSION_CFDI("VALIDAR_VERSION_CFDI", "Validación de versión CFDI vigente"),
    VALIDAR_RFC_RECEPTOR("VALIDAR_RFC_RECEPTOR", "Validación de RFC receptor autorizado"),
    OBTENER_EMISOR("OBTENER_EMISOR", "Obtención o creación del emisor"),
    VALIDAR_DUPLICADO_SERIE_FOLIO("VALIDAR_DUPLICADO_SERIE_FOLIO", "Validación de duplicado por serie+folio"),
    VALIDAR_DUPLICADO_UUID("VALIDAR_DUPLICADO_UUID", "Validación de duplicado por UUID fiscal"),
    VALIDAR_ADDENDA("VALIDAR_ADDENDA", "Validación de estructura y contenido de addenda"),
    VALIDAR_SAT("VALIDAR_SAT", "Validación del documento con SAT vía PAC"),
    PERSISTIR_DOCUMENTO("PERSISTIR_DOCUMENTO", "Persistencia del documento en base de datos"),
    REGISTRO_RESPONSE("REGISTRO_RESPONSE", "Respuesta del registro completado"),
    REGISTRO_ERROR_NEGOCIO("REGISTRO_ERROR_NEGOCIO", "Error de validación de negocio en registro"),
    REGISTRO_ERROR_TECNICO("REGISTRO_ERROR_TECNICO", "Error técnico inesperado en registro"),

    // ========== REGISTRO DE COMPLEMENTO DE PAGO (registerPayment) ==========
    PAGO_REGISTRO_REQUEST("PAGO_REGISTRO_REQUEST", "Inicio de registro de complemento de pago"),
    PAGO_LEER_ARCHIVO_XML("PAGO_LEER_ARCHIVO_XML", "Lectura del archivo XML de pago"),
    PAGO_VALIDAR_TIPO_ADDENDA("PAGO_VALIDAR_TIPO_ADDENDA", "Validación del tipo de addenda"),
    PAGO_VALIDAR_ESTRUCTURA_XSD("PAGO_VALIDAR_ESTRUCTURA_XSD", "Validación de estructura XML contra XSD"),
    PAGO_PARSEAR_XML("PAGO_PARSEAR_XML", "Parseo del XML de complemento de pago"),
    PAGO_VALIDAR_TIPO_COMPROBANTE("PAGO_VALIDAR_TIPO_COMPROBANTE", "Validación de tipo de comprobante (P)"),
    PAGO_VALIDAR_DUPLICADO("PAGO_VALIDAR_DUPLICADO", "Validación de duplicado por UUID fiscal"),
    PAGO_VALIDAR_RECEPTOR("PAGO_VALIDAR_RECEPTOR", "Validación de receptor autorizado"),
    PAGO_VALIDAR_VERSION("PAGO_VALIDAR_VERSION", "Validación de versión Pagos 2.0"),
    PAGO_VALIDAR_SAT("PAGO_VALIDAR_SAT", "Validación con SAT vía multipac"),
    PAGO_PERSISTIR_BD("PAGO_PERSISTIR_BD", "Persistencia del complemento de pago en BD"),
    PAGO_REGISTRO_ARCHIVO("PAGO_REGISTRO_ARCHIVO", "Registro de archivo procesado"),
    PAGO_REGISTRO_RESPONSE("PAGO_REGISTRO_RESPONSE", "Respuesta del registro de pago completado"),
    PAGO_REGISTRO_ERROR("PAGO_REGISTRO_ERROR", "Error en registro de complemento de pago"),

    // ========== ACTUALIZACIÓN DE FACTURA/NC (updateInvoice) ==========
    UPDATE_REQUEST("UPDATE_REQUEST", "Inicio de actualización de factura/NC"),
    UPDATE_RESPONSE("UPDATE_RESPONSE", "Respuesta de actualización completada"),
    UPDATE_ERROR_NEGOCIO("UPDATE_ERROR_NEGOCIO", "Error de validación de negocio en actualización"),
    UPDATE_ERROR_TECNICO("UPDATE_ERROR_TECNICO", "Error técnico inesperado en actualización");

    private final String code;
    private final String description;

    AuditAction(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}

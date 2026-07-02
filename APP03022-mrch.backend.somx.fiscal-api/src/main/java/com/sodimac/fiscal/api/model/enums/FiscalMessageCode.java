package com.sodimac.fiscal.api.model.enums;

/**
 * Enumeración centralizada de códigos de mensaje del sistema fiscal.
 *
 * Incluye diferentes tipos de mensajes:
 * - ERR###: Errores técnicos
 * - BUS###: Errores/validaciones de negocio
 * - WRN###: Advertencias
 * - RES###: Respuestas exitosas
 *
 * Cada código contiene:
 * - code: Clave del catálogo (ej: ERR001, BUS034)
 * - message: Mensaje fallback para uso sin conexión a catalogos-api
 *
 * @author Sodimac Tech Team
 * @version 2.0
 * @since 2025
 */
public enum FiscalMessageCode {

    // ========== ERRORES DE ARCHIVO XML (ERR001-ERR005) ==========
    ERR001("ERR001", "El archivo está vacío"),
    ERR002("ERR002", "El archivo debe tener extensión .xml"),
    ERR003("ERR003", "Error procesando archivo XML fiscal"),
    ERR004("ERR004", "El XML no tiene una estructura válida"),
    ERR005("ERR005", "Error leyendo archivo XML"),

    // ========== ERRORES DE VALIDACIÓN XSD (ERR006-ERR007) ==========
    ERR006("ERR006", "El XML no cumple con la estructura XSD"),
    ERR007("ERR007", "El XML no cumple con la estructura XSD Pagos 2.0"),

    // ========== ERRORES DE PARSEO/DESERIALIZACIÓN (ERR008-ERR013) ==========
    ERR008("ERR008", "Error configurando procesador CartaPorte"),
    ERR009("ERR009", "Error configurando procesador CFDI"),
    ERR010("ERR010", "Error configurando procesador Pagos"),
    ERR011("ERR011", "Error procesando CartaPorte"),
    ERR012("ERR012", "Error procesando CFDI"),
    ERR013("ERR013", "Error procesando Pagos"),

    // ========== ERRORES DE TIPO DE DOCUMENTO (ERR014-ERR020) ==========
    ERR014("ERR014", "Error detectando tipo de documento XML"),
    ERR015("ERR015", "Tipo de documento fiscal no válido"),
    ERR016("ERR016", "Tipo de CFDI no soportado en este procesador"),
    ERR017("ERR017", "Se esperaba Factura (I) pero se encontró tipo diferente"),
    ERR018("ERR018", "Se esperaba Nota de Crédito (E) pero se encontró tipo diferente"),
    ERR019("ERR019", "Tipo de comprobante no válido"),
    ERR020("ERR020", "El tipo de comprobante debe ser P (Pago)"),

    // ========== ERRORES DE COMPLEMENTO DE PAGO (ERR021-ERR027) ==========
    ERR021("ERR021", "Datos de Pagos no pueden ser nulos"),
    ERR022("ERR022", "Complemento de Pago debe incluir nodo Totales"),
    ERR023("ERR023", "Nodo Totales debe especificar el monto total de pagos"),
    ERR024("ERR024", "Complemento de Pago debe contener al menos un elemento Pago"),
    ERR025("ERR025", "Complemento de Pago debe especificar la versión"),
    ERR026("ERR026", "El complemento de pago ya se encuentra registrado en el sistema"),
    ERR027("ERR027", "Error convirtiendo XML a Document"),

    // ========== ERRORES DE VALIDACIÓN DE NEGOCIO GENERAL (ERR028-ERR031) ==========
    ERR028("ERR028", "El tipo de addenda debe ser 5 para complementos de pago"),
    ERR029("ERR029", "El RFC receptor no está autorizado en el sistema"),
    ERR030("ERR030", "La versión del complemento de pago no es vigente en el sistema"),
    ERR031("ERR031", "El documento relacionado no se encuentra registrado en el sistema"),

    // ========== ERRORES DE SISTEMA ADICIONALES (ERR032-ERR036) ==========
    ERR032("ERR032", "PAC no disponible para el nivel de prioridad solicitado"),
    ERR033("ERR033", "Error generando PDF desde XML"),
    ERR034("ERR034", "Error generando PDF del Complemento de Pago"),
    ERR035("ERR035", "Error de validación en los campos del request"),
    ERR036("ERR036", "Error inesperado del sistema. Contacte al administrador"),

    // ========== ERRORES DE NEGOCIO - CARTA PORTE (BUS002-BUS007) ==========
    BUS002("BUS002", "El RFC del receptor {0} no acepta documentos fiscales. Verifica que el RFC esté autorizado para recibir comprobantes fiscales."),
    BUS003("BUS003", "La guía carta porte no contiene un archivo xml y csv asociado, favor de validar."),
    BUS004("BUS004", "No fue posible registrar la guía carta porte, falta información por completar."),
    BUS005("BUS005", "La guía de embarque se encuentra previamente registrada, favor de validar."),
    BUS006("BUS006", "Las facturas no corresponden al complemento que desea publicar."),
    BUS007("BUS007", "Las notas de crédito no corresponden al complemento que desea publicar."),

    // ========== ERRORES DE NEGOCIO - RFC RECEPTOR (BUS008-BUS010) ==========
    BUS008("BUS008", "El RFC del receptor no acepta documentos fiscales. Verifica que el RFC esté autorizado para recibir comprobantes fiscales."),
    BUS009("BUS009", "El RFC del receptor no está activo en el período de vigencia especificado"),
    BUS010("BUS010", "El RFC del receptor no se encuentra registrado en el sistema"),

    // ========== ERRORES DE NEGOCIO - ADDENDA (BUS011-BUS019) ==========
    BUS001("BUS001", "La addenda de la factura no cumple con la estructura requerida. Por favor, verifica el formato y el nombre de los elementos."),
    BUS011("BUS011", "La addenda debe contener el nodo Addenda_Sodimac para documentos de mercancía, servicios y transporte local"),
    BUS012("BUS012", "La addenda debe contener el nodo Addenda_Sodimac_CartaPorte para transporte foráneo"),
    BUS013("BUS013", "El campo RFC en la addenda es obligatorio"),
    BUS014("BUS014", "El campo UUID en la addenda es obligatorio"),
    BUS015("BUS015", "El campo Folio en la addenda es obligatorio"),
    BUS016("BUS016", "El campo NoOC (Número de Orden de Compra) en la addenda es obligatorio"),
    BUS017("BUS017", "El campo Proveedor en la addenda es obligatorio"),
    BUS018("BUS018", "El formato del RFC en la addenda no es válido"),
    BUS019("BUS019", "El UUID en la addenda no coincide con el UUID del TimbreFiscalDigital"),

    // ========== ERRORES DE NEGOCIO - VERSIÓN CFDI (BUS020-BUS022) ==========
    BUS020("BUS020", "La versión del documento no se encuentra configurada en el sistema"),
    BUS021("BUS021", "La versión del documento no está vigente"),
    BUS022("BUS022", "El documento debe ser versión CFDI 4.0"),

    // ========== ERRORES DE NEGOCIO - TIPO DOCUMENTO (BUS023-BUS025) ==========
    BUS023("BUS023", "El tipo de documento debe ser I (Factura) o E (Nota de Crédito)"),
    BUS024("BUS024", "El tipo de comprobante no está permitido para este proceso"),
    BUS025("BUS025", "Las Notas de Crédito deben incluir CFDIs relacionados con tipo de relación 01"),

    // ========== ERRORES DE NEGOCIO - VALIDACIÓN SAT (BUS026-BUS030) ==========
    BUS026("BUS026", "El documento fiscal no se encuentra vigente en el SAT"),
    BUS027("BUS027", "El documento fiscal se encuentra cancelado en el SAT"),
    BUS028("BUS028", "El UUID no se encuentra registrado en el SAT"),
    BUS029("BUS029", "El sello digital del documento no es válido según el SAT"),
    BUS030("BUS030", "No fue posible validar el estatus del documento en el SAT"),

    // ========== ERRORES DE NEGOCIO - EMISOR (BUS031-BUS033) ==========
    BUS031("BUS031", "El RFC del emisor no se encuentra registrado como proveedor autorizado"),
    BUS032("BUS032", "El emisor se encuentra inactivo en el sistema"),
    BUS033("BUS033", "El RFC del emisor no es válido"),

    // ========== ERRORES DE NEGOCIO - DUPLICIDAD (BUS034-BUS036) ==========
    BUS034("BUS034", "El documento con UUID ya se encuentra registrado en el sistema"),
    BUS035("BUS035", "El documento con Serie y Folio ya se encuentra registrado"),
    BUS036("BUS036", "El archivo ya fue procesado anteriormente"),

    // ========== ERRORES DE NEGOCIO - VALIDACIÓN CONTENIDO (BUS037-BUS041) ==========
    BUS037("BUS037", "El documento debe contener al menos un concepto"),
    BUS038("BUS038", "El total del documento no puede ser cero"),
    BUS039("BUS039", "Los montos de los conceptos no coinciden con el total del documento"),
    BUS040("BUS040", "Los impuestos calculados no coinciden con los declarados"),
    BUS041("BUS041", "El documento contiene impuestos no permitidos por Sodimac"),

    // ========== ERRORES DE NEGOCIO - CFDIs RELACIONADOS NC (BUS042-BUS045) ==========
    BUS042("BUS042", "La Nota de Crédito debe incluir al menos un CFDI relacionado en el nodo CfdiRelacionados"),
    BUS043("BUS043", "La Factura relacionada no se encuentra registrada en el sistema. Debe registrar primero la Factura antes de cargar la Nota de Crédito"),
    BUS044("BUS044", "El CFDI relacionado no es una Factura (tipo I). Solo se pueden relacionar NC con Facturas"),
    BUS045("BUS045", "El tipo de relación no es válido. Para Notas de Crédito debe ser 01"),

    // ========== ERRORES DE NEGOCIO - ACTUALIZACIÓN FACTURAS/NC (BUS046-BUS048) ==========
    BUS046("BUS046", "El documento con UUID no se encuentra registrado en el sistema"),
    BUS047("BUS047", "El documento con UUID no pertenece al proveedor especificado"),
    BUS048("BUS048", "La addenda del documento no se encuentra registrada en el sistema"),

    // ========== ERRORES DE NEGOCIO - TRANSICIÓN ESTATUS (BUS049-BUS053) ==========
    BUS049("BUS049", "El estatus no es válido para el tipo de documento"),
    BUS050("BUS050", "El estatus no existe en el catálogo de estatus"),
    BUS051("BUS051", "La transición de estatus no está permitida"),
    BUS052("BUS052", "No se puede actualizar un documento en estatus final alcanzado"),
    BUS053("BUS053", "El estatus del documento ya es el indicado. No se requiere actualización"),

    // ========== ERRORES DE NEGOCIO - PERMISOS (BUS054-BUS056) ==========
    BUS054("BUS054", "El usuario no tiene permisos para actualizar documentos fiscales"),
    BUS055("BUS055", "Solo el proveedor propietario puede actualizar este documento"),
    BUS056("BUS056", "El documento está en proceso automático y no puede ser modificado manualmente"),

    // ========== ERRORES DE NEGOCIO - VALIDACIÓN REGISTRO (BUS057) ==========
    BUS057("BUS057", "La diferencia entre el subtotal de la factura ({0}) y el importe de la recepción ({1}) supera la tolerancia permitida de {2} pesos"),

    // ========== ERRORES DE NEGOCIO - VALIDACIÓN NOTA DE CRÉDITO (BUS058-BUS059) ==========
    BUS058("BUS058", "La forma de pago ({0}) de la nota de crédito no se encuentra configurada como válida para su registro. Por favor, valida con el área de Finanzas de Sodimac."),
    BUS059("BUS059", "El uso de CFDI ({0}) de la nota de crédito no se encuentra configurado como válido para su registro. Por favor, valida con el área de Finanzas de Sodimac."),

    // ========== ERRORES DE NEGOCIO - TIPO DE DOCUMENTO XML (BUS060) ==========
    // QA junio-2026 pidió este mensaje con id BUS057, pero BUS057 ya estaba asignado a
    // la tolerancia de importe. Reasignado a BUS060 (avisar a Ivan).
    BUS060("BUS060", "El archivo XML no corresponde a una factura válida. Por favor, valida el documento antes de continuar."),

    // ========== ERRORES DE NEGOCIO - MONTO NOTA DE CRÉDITO (BUS061) - QA junio-2026 ==========
    BUS061("BUS061", "El monto de la nota de crédito ({0}) no puede ser mayor al monto de la factura relacionada ({1}). Por favor, valida el documento antes de continuar."),

    // ========== ERRORES DE NEGOCIO - BLOQUEO DE PUBLICACIÓN (BUS2028-BUS2029) ==========
    BUS2028("BUS2028", "Actualmente existe un bloqueo para la publicación de facturas según el tipo de proveedor. Por favor, valida con el área de Finanzas de Sodimac para continuar."),
    BUS2029("BUS2029", "Actualmente existe un bloqueo para la publicación de facturas. Por favor, valida con el área de Finanzas de Sodimac para continuar."),

    // ========== ADVERTENCIAS - COMPLEMENTOS (WRN001-WRN005) ==========
    WRN001("WRN001", "Version de CartaPorte no soportada: {0}. Solo se soportan versiones 3.0 y 3.1"),

    // ========== ADVERTENCIAS - VALIDACIÓN DE BÚSQUEDA (WRN7000-WRN7006) ==========
    WRN7000("WRN7000", "El rango de fechas excede el máximo permitido de {0} meses"),
    WRN7005("WRN7005", "La fecha de inicio no puede ser mayor a la fecha fin"),

    // ========== ADVERTENCIAS - VALIDACIÓN FACTURA (WRN7012-WRN7014) - STM-395 ==========
    WRN7012("WRN7012", "La factura requiere un folio para publicar el documento. Por favor, valide la información antes de continuar."),
    WRN7013("WRN7013", "La factura se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar."),
    WRN7014("WRN7014", "La factura se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar."),

    // ========== ADVERTENCIAS - VALIDACIÓN NOTA DE CRÉDITO (WRN7015-WRN7017) - STM-397 ==========
    WRN7015("WRN7015", "La nota de crédito requiere un folio para publicar el documento. Por favor, valide la información antes de continuar."),
    WRN7016("WRN7016", "La nota de crédito se encuentra previamente registrada con la misma serie y folio. Por favor, valide la información antes de continuar."),
    WRN7017("WRN7017", "La nota de crédito se encuentra previamente registrada con el mismo UUID. Por favor, valide la información antes de continuar."),

    // ========== ADVERTENCIAS - CANCELACIÓN NC (WRN7023) - STM-335 ==========
    WRN7023("WRN7023", "La nota de crédito no puede cancelarse porque ya cuenta con una afectación contable."),

    // ========== ADVERTENCIAS - TOLERANCIA IMPORTE (WRN7030-WRN7031) - QA junio-2026 ==========
    // Fuera de tolerancia la factura se registra (no se rechaza); estas advertencias informan al usuario.
    // Factura MAYOR a recepción -> 2 Recibido Parcial (requiere NC).
    WRN7030("WRN7030", "La factura se registró como Recibido Parcial: la diferencia entre el subtotal de la factura ({0}) y el importe de la recepción ({1}) supera la tolerancia permitida de {2}. Se requiere una nota de crédito para conciliar el monto y dar inicio al proceso de pago de la factura."),
    // Factura MENOR a recepción -> 1 Rechazo Comercial (decisión Ivan, diagrama 2026-06-18).
    WRN7031("WRN7031", "La factura se registró como Rechazo Comercial: el subtotal de la factura ({0}) es menor al importe de la recepción ({1}) y la diferencia supera la tolerancia permitida de {2}."),

    // Factura ya cargada manualmente (existe el folio fiscal en tenant_finance.addendum_manual) - QA 2026-06-23
    WRN7032("WRN7032", "La factura se encuentra previamente registrada manualmente, Por favor, validar con el área de finanzas."),

    // PDF no se pudo almacenar en el bucket (la factura sí se registró) - QA 2026-06-23
    WRN7033("WRN7033", "La factura se registró correctamente, pero el PDF no se pudo almacenar. El documento podría no estar disponible para descarga; intente cargarlo nuevamente o contacte a soporte."),

    // Confirmación: la NC dejaría el neto (factura - NCs) por debajo de la recepción -> rechazo. Fila 104 QA.
    WRN7034("WRN7034", "La factura será rechazada y las notas de crédito serán canceladas, ya que el monto total de la factura menos las notas de crédito son menor al monto disponible de la recepción, ¿Desea continuar?"),

    // Búsqueda sin UUID: las fechas de recepción son obligatorias. QA Fer/Ivan jul-2026.
    // Código BUS (no WRN) para que el ControllerAdvisor lo mapee a HTTP 400 (error de negocio).
    BUS3103("BUS3103", "Las fechas de recepción son obligatorias cuando no se realiza la búsqueda por UUID.");

    private final String code;
    private final String message;

    FiscalMessageCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Construye un mensaje formateado con parámetros adicionales.
     *
     * Soporta dos formatos de placeholders:
     * - MessageFormat: {0}, {1}, {2}... (recomendado para multi-idioma)
     * - String.format: %s (legacy)
     *
     * @param params Parámetros adicionales para incluir en el mensaje
     * @return Mensaje formateado
     */
    public String formatMessage(Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }
        // Formato MessageFormat: {0}, {1}, {2}...
        if (message.contains("{0}") || message.contains("{1}")) {
            return java.text.MessageFormat.format(message, params);
        }
        // Formato String.format: %s
        if (message.contains("%s")) {
            return String.format(message, params);
        }
        // Fallback: concatenar parámetros
        return message + ": " + String.join(", ", convertToStringArray(params));
    }

    private String[] convertToStringArray(Object... params) {
        String[] result = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = params[i] != null ? params[i].toString() : "null";
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", code, message);
    }
}

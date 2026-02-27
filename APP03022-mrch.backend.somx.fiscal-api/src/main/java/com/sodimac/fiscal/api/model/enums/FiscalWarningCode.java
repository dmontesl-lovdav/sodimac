package com.sodimac.fiscal.api.model.enums;

/**
 * Enumeración centralizada de códigos de advertencia del sistema fiscal.
 *
 * Cada código contiene:
 * - code: Clave del catálogo (ej: WRN009)
 * - message: Mensaje fallback para uso sin conexión a catalogos-api
 *
 * Las advertencias son mensajes informativos que no detienen el proceso
 * pero alertan sobre situaciones que requieren atención.
 *
 * @author Sodimac Tech Team
 * @version 2.0
 * @since 2025
 */
public enum FiscalWarningCode {

    // ========== ADVERTENCIAS DE VALIDACIÓN (WRN009-WRN011) ==========
    WRN009("WRN009", "El total calculado no coincide con el total declarado"),
    WRN010("WRN010", "Tipo de cambio no especificado, usando valor por defecto"),
    WRN011("WRN011", "Moneda no especificada, usando MXN por defecto"),

    // ========== ADVERTENCIAS DE COMPLEMENTO DE PAGO (WRN012-WRN014) ==========
    WRN012("WRN012", "El pago es parcial, saldo pendiente"),
    WRN013("WRN013", "Número de operación no especificado"),
    WRN014("WRN014", "Información bancaria incompleta"),

    // ========== ADVERTENCIAS DE DOCUMENTOS RELACIONADOS (WRN015-WRN016) ==========
    WRN015("WRN015", "Documento relacionado con saldo cero"),
    WRN016("WRN016", "El monto pagado excede el saldo del documento"),

    // ========== ADVERTENCIAS DE CARTA PORTE (WRN017-WRN018) ==========
    WRN017("WRN017", "Datos opcionales de CartaPorte no especificados"),
    WRN018("WRN018", "Figuras de transporte no especificadas"),

    // ========== ADVERTENCIAS DE ADDENDA (WRN019-WRN020) ==========
    WRN019("WRN019", "Addenda sin contenido"),
    WRN020("WRN020", "Campos opcionales de addenda vacíos"),

    // ========== ADVERTENCIAS DE REGISTRO (WRN021-WRN023) ==========
    WRN021("WRN021", "Emisor no existía, se creó nuevo registro"),
    WRN022("WRN022", "Receptor no existía, se creó nuevo registro"),
    WRN023("WRN023", "Archivo con nombre similar ya fue procesado"),

    // ========== ADVERTENCIAS DE SAT (WRN024-WRN025) ==========
    WRN024("WRN024", "Validación SAT tardó más de lo esperado"),
    WRN025("WRN025", "Respuesta SAT incompleta pero válida"),

    // ========== ADVERTENCIAS GENERALES (WRN026-WRN027) ==========
    WRN026("WRN026", "Versión del documento será obsoleta próximamente"),
    WRN027("WRN027", "Datos opcionales no especificados"),

    // ========== INFORMACIÓN SOBRE RECEPCIÓN (WRN028-WRN030) ==========
    WRN028("WRN028", "Documento registrado sin número de recepción. Requiere vinculación manual"),
    WRN029("WRN029", "La recepción no se encuentra disponible en el sistema"),
    WRN030("WRN030", "La recepción ya se encuentra vinculada a otro documento fiscal");

    private final String code;
    private final String message;

    FiscalWarningCode(String code, String message) {
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
     * Construye un mensaje de advertencia formateado con parámetros adicionales.
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

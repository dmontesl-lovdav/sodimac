package com.sodimac.fiscal.api.service;

import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.model.enums.FiscalWarningCode;

/**
 * Servicio para gestión centralizada de mensajes del sistema fiscal.
 *
 * Este servicio actúa como una capa de abstracción para obtener mensajes
 * desde un catálogo centralizado. Funciona con un switch de configuración:
 * - catalogs.api.enabled=true: Consulta catalogos-api para mensajes multi-idioma
 * - catalogs.api.enabled=false: Usa el mensaje fallback del enum
 *
 * Los mensajes de negocio (BUS###, ERR###) se consultan en BD cuando está habilitado.
 * Las validaciones de entrada y excepciones genéricas no pasan por este servicio.
 *
 * @author Sodimac Tech Team
 * @version 2.0
 * @since 2025
 */
public interface MessageCatalogService {

    /**
     * Obtiene el mensaje por código desde el catálogo.
     * Consulta BD si catalogs.api.enabled=true, sino usa fallback del enum.
     *
     * @param messageCode Código de mensaje (ERR###, BUS###, etc.)
     * @return Mensaje del catálogo o fallback
     */
    String getMessage(FiscalMessageCode messageCode);

    /**
     * Obtiene el mensaje formateado con parámetros adicionales.
     *
     * @param messageCode Código de mensaje
     * @param params Parámetros adicionales
     * @return Mensaje formateado
     */
    String getMessage(FiscalMessageCode messageCode, Object... params);

    /**
     * Obtiene el mensaje de advertencia por código.
     *
     * @param warningCode Código de advertencia
     * @return Mensaje de advertencia
     */
    String getWarningMessage(FiscalWarningCode warningCode);

    /**
     * Obtiene el mensaje de advertencia formateado con parámetros adicionales.
     *
     * @param warningCode Código de advertencia
     * @param params Parámetros adicionales
     * @return Mensaje formateado
     */
    String getWarningMessage(FiscalWarningCode warningCode, Object... params);

    /**
     * Obtiene el código del mensaje.
     *
     * @param messageCode Enumeración de mensaje
     * @return Código del mensaje (ej: BUS034, ERR001)
     */
    String getCode(FiscalMessageCode messageCode);

    /**
     * Obtiene el código de advertencia.
     *
     * @param warningCode Enumeración de advertencia
     * @return Código de advertencia (ej: WRN009)
     */
    String getWarningCode(FiscalWarningCode warningCode);

    /**
     * Construye un mensaje completo con código y descripción.
     *
     * @param messageCode Código de mensaje
     * @return Mensaje completo [CÓDIGO] Descripción
     */
    String getFullMessage(FiscalMessageCode messageCode);

    /**
     * Construye un mensaje completo con código y descripción.
     *
     * @param warningCode Código de advertencia
     * @return Mensaje completo [CÓDIGO] Descripción
     */
    String getFullWarningMessage(FiscalWarningCode warningCode);

    /**
     * Lanza una FiscalException con el código de mensaje especificado.
     * Consulta el catálogo antes de lanzar si está habilitado.
     *
     * @param messageCode Código de mensaje
     * @throws com.sodimac.fiscal.api.exception.FiscalException
     */
    void throwException(FiscalMessageCode messageCode);

    /**
     * Lanza una FiscalException con el código de mensaje y parámetros para formatear.
     * Los parámetros reemplazan placeholders {0}, {1}, etc. en el mensaje.
     *
     * Ejemplo de uso:
     * <pre>
     * // Mensaje: "El RFC del receptor {0} no acepta documentos fiscales"
     * messageCatalog.throwExceptionWithParams(FiscalMessageCode.BUS002, rfcReceptor);
     * </pre>
     *
     * @param messageCode Código de mensaje
     * @param params Parámetros para formatear el mensaje ({0}, {1}, etc.)
     * @throws com.sodimac.fiscal.api.exception.FiscalException
     */
    void throwExceptionWithParams(FiscalMessageCode messageCode, Object... params);

    /**
     * Lanza una FiscalException con el código de mensaje e información adicional.
     * Consulta el catálogo antes de lanzar si está habilitado.
     *
     * @param messageCode Código de mensaje
     * @param additionalInfo Información adicional del contexto
     * @throws com.sodimac.fiscal.api.exception.FiscalException
     */
    void throwException(FiscalMessageCode messageCode, String additionalInfo);

    /**
     * Lanza una FiscalException con el código de mensaje y causa original.
     * Consulta el catálogo antes de lanzar si está habilitado.
     *
     * @param messageCode Código de mensaje
     * @param cause Excepción original
     * @throws com.sodimac.fiscal.api.exception.FiscalException
     */
    void throwException(FiscalMessageCode messageCode, Throwable cause);

    /**
     * Lanza una FiscalException con el código de mensaje, información adicional y causa.
     * Consulta el catálogo antes de lanzar si está habilitado.
     *
     * @param messageCode Código de mensaje
     * @param additionalInfo Información adicional del contexto
     * @param cause Excepción original
     * @throws com.sodimac.fiscal.api.exception.FiscalException
     */
    void throwException(FiscalMessageCode messageCode, String additionalInfo, Throwable cause);

    // ========== MÉTODOS CREATE EXCEPTION (para uso con throw) ==========

    /**
     * Crea una FiscalException sin lanzarla.
     * Útil para usar con 'throw' y evitar 'return null' después de throwException().
     *
     * Uso recomendado:
     * <pre>
     * throw messageCatalog.createException(FiscalMessageCode.BUS034);
     * </pre>
     *
     * @param messageCode Código de mensaje
     * @return FiscalException lista para lanzar
     */
    com.sodimac.fiscal.api.exception.FiscalException createException(FiscalMessageCode messageCode);

    /**
     * Crea una FiscalException con parámetros para formatear sin lanzarla.
     * Los parámetros reemplazan placeholders {0}, {1}, etc. en el mensaje.
     *
     * Uso recomendado:
     * <pre>
     * // Mensaje: "El RFC del receptor {0} no acepta documentos fiscales"
     * throw messageCatalog.createExceptionWithParams(FiscalMessageCode.BUS002, rfcReceptor);
     * </pre>
     *
     * @param messageCode Código de mensaje
     * @param params Parámetros para formatear el mensaje ({0}, {1}, etc.)
     * @return FiscalException lista para lanzar con mensaje formateado
     */
    com.sodimac.fiscal.api.exception.FiscalException createExceptionWithParams(FiscalMessageCode messageCode, Object... params);

    /**
     * Crea una FiscalException con información adicional sin lanzarla.
     *
     * @param messageCode Código de mensaje
     * @param additionalInfo Información adicional del contexto
     * @return FiscalException lista para lanzar
     */
    com.sodimac.fiscal.api.exception.FiscalException createException(FiscalMessageCode messageCode, String additionalInfo);

    /**
     * Crea una FiscalException con causa original sin lanzarla.
     *
     * @param messageCode Código de mensaje
     * @param cause Excepción original
     * @return FiscalException lista para lanzar
     */
    com.sodimac.fiscal.api.exception.FiscalException createException(FiscalMessageCode messageCode, Throwable cause);

    /**
     * Crea una FiscalException con información adicional y causa sin lanzarla.
     *
     * @param messageCode Código de mensaje
     * @param additionalInfo Información adicional del contexto
     * @param cause Excepción original
     * @return FiscalException lista para lanzar
     */
    com.sodimac.fiscal.api.exception.FiscalException createException(FiscalMessageCode messageCode, String additionalInfo, Throwable cause);

    // ========== MÉTODOS DEPRECADOS PARA COMPATIBILIDAD ==========

    /**
     * @deprecated Usar getMessage(FiscalMessageCode) en su lugar
     */
    @Deprecated
    default String getErrorMessage(FiscalMessageCode messageCode) {
        return getMessage(messageCode);
    }

    /**
     * @deprecated Usar getMessage(FiscalMessageCode, Object...) en su lugar
     */
    @Deprecated
    default String getErrorMessage(FiscalMessageCode messageCode, Object... params) {
        return getMessage(messageCode, params);
    }

    /**
     * @deprecated Usar getCode(FiscalMessageCode) en su lugar
     */
    @Deprecated
    default String getErrorCode(FiscalMessageCode messageCode) {
        return getCode(messageCode);
    }

    /**
     * @deprecated Usar getFullMessage(FiscalMessageCode) en su lugar
     */
    @Deprecated
    default String getFullErrorMessage(FiscalMessageCode messageCode) {
        return getFullMessage(messageCode);
    }

    /**
     * @deprecated Usar throwException(FiscalMessageCode) en su lugar
     */
    @Deprecated
    default void throwError(FiscalMessageCode messageCode) {
        throwException(messageCode);
    }

    /**
     * @deprecated Usar throwException(FiscalMessageCode, String) en su lugar
     */
    @Deprecated
    default void throwError(FiscalMessageCode messageCode, String additionalInfo) {
        throwException(messageCode, additionalInfo);
    }

    /**
     * @deprecated Usar throwException(FiscalMessageCode, Throwable) en su lugar
     */
    @Deprecated
    default void throwError(FiscalMessageCode messageCode, Throwable cause) {
        throwException(messageCode, cause);
    }

    /**
     * @deprecated Usar throwException(FiscalMessageCode, String, Throwable) en su lugar
     */
    @Deprecated
    default void throwError(FiscalMessageCode messageCode, String additionalInfo, Throwable cause) {
        throwException(messageCode, additionalInfo, cause);
    }
}

package com.sodimac.fiscal.api.exception;

import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;

/**
 * Excepción personalizada para errores del sistema fiscal.
 *
 * Encapsula un código de mensaje y mensaje centralizado.
 * Soporta mensajes resueltos desde catálogo (BD) o fallback (enum).
 *
 * @author Sodimac Tech Team
 * @version 2.0
 * @since 2025
 */
public class FiscalException extends RuntimeException {

    private static final long serialVersionUID = -2019982120691054415L;
    private final FiscalMessageCode messageCode;
    private final String additionalInfo;
    private final String resolvedMessage;

    /**
     * Constructor con código de mensaje.
     * Usa el mensaje del enum como fallback.
     *
     * @param messageCode Código de mensaje del catálogo
     */
    public FiscalException(FiscalMessageCode messageCode) {
        super(messageCode.getMessage());
        this.messageCode = messageCode;
        this.additionalInfo = null;
        this.resolvedMessage = null;
    }

    /**
     * Constructor con código de mensaje e información adicional.
     * Usa el mensaje del enum como fallback.
     *
     * @param messageCode Código de mensaje del catálogo
     * @param additionalInfo Información adicional del contexto
     */
    public FiscalException(FiscalMessageCode messageCode, String additionalInfo) {
        super(buildMessage(messageCode.getMessage(), additionalInfo));
        this.messageCode = messageCode;
        this.additionalInfo = additionalInfo;
        this.resolvedMessage = null;
    }

    /**
     * Constructor con código de mensaje y causa original.
     * Usa el mensaje del enum como fallback.
     *
     * @param messageCode Código de mensaje del catálogo
     * @param cause Excepción original
     */
    public FiscalException(FiscalMessageCode messageCode, Throwable cause) {
        super(messageCode.getMessage(), cause);
        this.messageCode = messageCode;
        this.additionalInfo = null;
        this.resolvedMessage = null;
    }

    /**
     * Constructor con código de mensaje, información adicional y causa original.
     * Usa el mensaje del enum como fallback.
     *
     * @param messageCode Código de mensaje del catálogo
     * @param additionalInfo Información adicional del contexto
     * @param cause Excepción original
     */
    public FiscalException(FiscalMessageCode messageCode, String additionalInfo, Throwable cause) {
        super(buildMessage(messageCode.getMessage(), additionalInfo), cause);
        this.messageCode = messageCode;
        this.additionalInfo = additionalInfo;
        this.resolvedMessage = null;
    }

    /**
     * Constructor con mensaje ya resuelto desde catálogo (BD).
     * Usado por MessageCatalogService cuando consulta catalogos-api.
     *
     * @param messageCode Código de mensaje del catálogo
     * @param resolvedMessage Mensaje ya obtenido del catálogo (BD o enum)
     * @param additionalInfo Información adicional del contexto
     * @param cause Causa original (puede ser null)
     */
    public FiscalException(FiscalMessageCode messageCode, String resolvedMessage,
                           String additionalInfo, Throwable cause) {
        super(buildMessage(resolvedMessage, additionalInfo), cause);
        this.messageCode = messageCode;
        this.additionalInfo = additionalInfo;
        this.resolvedMessage = resolvedMessage;
    }

    /**
     * Construye el mensaje final concatenando mensaje base con información adicional.
     */
    private static String buildMessage(String message, String additionalInfo) {
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            return message + ": " + additionalInfo;
        }
        return message;
    }

    public FiscalMessageCode getMessageCode() {
        return messageCode;
    }

    /**
     * @deprecated Usar getMessageCode() en su lugar
     */
    @Deprecated
    public FiscalMessageCode getErrorCode() {
        return messageCode;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Obtiene el mensaje resuelto desde catálogo, o null si se usó fallback.
     */
    public String getResolvedMessage() {
        return resolvedMessage;
    }

    /**
     * Obtiene el código del mensaje.
     *
     * @return Código del mensaje (ej: BUS034, ERR001)
     */
    public String getCode() {
        return messageCode.getCode();
    }

    /**
     * Obtiene el mensaje completo con código y descripción.
     *
     * @return Mensaje formateado [CÓDIGO] Mensaje: info adicional
     */
    public String getFullMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(messageCode.getCode()).append("] ");

        // Usar mensaje resuelto si existe, sino el del enum
        String baseMessage = resolvedMessage != null ? resolvedMessage : messageCode.getMessage();
        sb.append(baseMessage);

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            sb.append(": ").append(additionalInfo);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFullMessage();
    }
}

// src/services/messageCatalog.service.ts
/**
 * Servicio de catalogo de mensajes para utils-api
 * Similar a MessageCatalogService en fiscal-api
 *
 * ARQUITECTURA:
 * - Si CATALOGOS_API_ENABLED=true: Consulta catalogos-api para mensajes multi-idioma
 * - Si CATALOGOS_API_ENABLED=false: Usa mensajes fallback del enum
 *
 * MULTI-IDIOMA:
 * El idioma se pasa como parametro o se usa el default de configuracion.
 */

import { catalogosClient } from '../clients/catalogos.client.js';
import catalogosConfig from '../config/catalogos.config.js';
import { UtilsMessageCode, UtilsMessageFallback } from '../enums/UtilsMessageCode.js';
import { UtilsException } from '../exceptions/UtilsException.js';

/**
 * Idiomas soportados
 */
export enum LanguageId {
    ES = 1,
    EN = 2,
    PT = 3,
}

/**
 * Servicio de mensajes centralizado
 */
class MessageCatalogService {
    /**
     * Obtiene el mensaje por codigo desde el catalogo
     * @param code Codigo de mensaje
     * @param langId ID del idioma (opcional)
     * @returns Mensaje del catalogo o fallback
     */
    async getMessage(code: UtilsMessageCode, langId?: LanguageId): Promise<string> {
        const lang = langId ?? catalogosConfig.defaultLangId;

        // Intentar obtener de catalogos-api
        if (catalogosConfig.enabled) {
            const message = await catalogosClient.getMessage(code, lang);
            if (message) {
                return message;
            }
        }

        // Fallback: usar mensaje del enum
        return UtilsMessageFallback[code] || code;
    }

    /**
     * Obtiene el mensaje formateado con parametros
     * @param code Codigo de mensaje
     * @param params Parametros para formatear ({0}, {1}, etc.)
     * @param langId ID del idioma (opcional)
     * @returns Mensaje formateado
     */
    async getMessageWithParams(
        code: UtilsMessageCode,
        params: (string | number)[],
        langId?: LanguageId
    ): Promise<string> {
        const message = await this.getMessage(code, langId);
        return this.formatMessage(message, params);
    }

    /**
     * Obtiene el mensaje de forma sincrona (usa fallback)
     * Util para casos donde no se puede usar async/await
     * @param code Codigo de mensaje
     * @returns Mensaje fallback
     */
    getMessageSync(code: UtilsMessageCode): string {
        return UtilsMessageFallback[code] || code;
    }

    /**
     * Obtiene el mensaje formateado de forma sincrona
     * @param code Codigo de mensaje
     * @param params Parametros para formatear
     * @returns Mensaje formateado
     */
    getMessageSyncWithParams(code: UtilsMessageCode, params: (string | number)[]): string {
        const message = this.getMessageSync(code);
        return this.formatMessage(message, params);
    }

    /**
     * Crea una UtilsException con el codigo de mensaje
     * @param code Codigo de mensaje
     * @param additionalInfo Informacion adicional (opcional)
     * @returns UtilsException lista para lanzar
     */
    createException(code: UtilsMessageCode, additionalInfo?: string): UtilsException {
        const message = this.getMessageSync(code);
        this.logException(code, message, additionalInfo);
        return new UtilsException(code, message, additionalInfo);
    }

    /**
     * Crea una UtilsException con parametros formateados
     * @param code Codigo de mensaje
     * @param params Parametros para formatear
     * @param additionalInfo Informacion adicional (opcional)
     * @returns UtilsException lista para lanzar
     */
    createExceptionWithParams(
        code: UtilsMessageCode,
        params: (string | number)[],
        additionalInfo?: string
    ): UtilsException {
        const message = this.getMessageSyncWithParams(code, params);
        this.logException(code, message, additionalInfo);
        return new UtilsException(code, message, additionalInfo);
    }

    /**
     * Crea una UtilsException con causa original
     * @param code Codigo de mensaje
     * @param cause Error original
     * @param additionalInfo Informacion adicional (opcional)
     * @returns UtilsException lista para lanzar
     */
    createExceptionWithCause(
        code: UtilsMessageCode,
        cause: Error,
        additionalInfo?: string
    ): UtilsException {
        const message = this.getMessageSync(code);
        this.logException(code, message, additionalInfo, cause);
        return new UtilsException(code, message, additionalInfo, cause);
    }

    /**
     * Lanza una UtilsException con el codigo de mensaje
     * @param code Codigo de mensaje
     * @param additionalInfo Informacion adicional (opcional)
     * @throws UtilsException
     */
    throwException(code: UtilsMessageCode, additionalInfo?: string): never {
        throw this.createException(code, additionalInfo);
    }

    /**
     * Lanza una UtilsException con parametros formateados
     * @param code Codigo de mensaje
     * @param params Parametros para formatear
     * @param additionalInfo Informacion adicional (opcional)
     * @throws UtilsException
     */
    throwExceptionWithParams(
        code: UtilsMessageCode,
        params: (string | number)[],
        additionalInfo?: string
    ): never {
        throw this.createExceptionWithParams(code, params, additionalInfo);
    }

    /**
     * Formatea un mensaje reemplazando placeholders {0}, {1}, etc.
     * @param message Mensaje base
     * @param params Parametros para reemplazar
     * @returns Mensaje formateado
     */
    private formatMessage(message: string, params: (string | number)[]): string {
        if (!params || params.length === 0) {
            return message;
        }

        let result = message;
        params.forEach((param, index) => {
            const placeholder = `{${index}}`;
            result = result.replace(new RegExp(placeholder.replace(/[{}]/g, '\\$&'), 'g'), String(param));
        });

        return result;
    }

    /**
     * Registra el log de la excepcion segun el tipo de error
     * BUS### (negocio) -> console.warn
     * ERR### (tecnico) -> console.error
     */
    private logException(
        code: UtilsMessageCode,
        message: string,
        additionalInfo?: string,
        cause?: Error
    ): void {
        const isBusinessError = code.startsWith('BUS');
        const logMessage = additionalInfo
            ? `[${code}] ${message} - ${additionalInfo}`
            : `[${code}] ${message}`;

        if (isBusinessError) {
            console.warn(`[MessageCatalog] ${logMessage}`);
        } else if (cause) {
            console.error(`[MessageCatalog] ${logMessage}`, cause);
        } else {
            console.error(`[MessageCatalog] ${logMessage}`);
        }
    }

    /**
     * Indica si catalogos-api esta habilitado
     */
    isEnabled(): boolean {
        return catalogosConfig.enabled;
    }

    /**
     * Limpia el cache del cliente
     */
    clearCache(): void {
        catalogosClient.clearCache();
    }
}

// Singleton
export const messageCatalog = new MessageCatalogService();
export default messageCatalog;

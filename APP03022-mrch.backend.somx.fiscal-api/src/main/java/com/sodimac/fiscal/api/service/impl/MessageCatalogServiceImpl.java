package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.exception.FiscalException;
import com.sodimac.fiscal.api.model.enums.FiscalMessageCode;
import com.sodimac.fiscal.api.model.enums.FiscalWarningCode;
import com.sodimac.fiscal.api.service.MessageCatalogService;
import com.sodimac.fiscal.api.util.LanguageIdMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación del servicio de catálogo de mensajes.
 *
 * ARQUITECTURA:
 * Funciona con un switch de configuración (catalogs.api.enabled):
 * - true: Consulta catalogos-api para obtener mensajes multi-idioma
 * - false: Usa el mensaje fallback del enum (modo offline/desarrollo)
 *
 * MULTI-IDIOMA:
 * El idioma se obtiene automáticamente del LocaleContextHolder, que es
 * establecido por LocaleFilter desde el header Accept-Language o X-Language.
 * No requiere cambios en los servicios que usan MessageCatalogService.
 *
 * Los métodos throwException() ahora resuelven el mensaje desde el catálogo
 * antes de lanzar la excepción, asegurando que FiscalException contenga
 * el mensaje correcto según la configuración e idioma del request.
 *
 * Incluye cache local para optimizar rendimiento cuando se usa catalogos-api.
 *
 * @author Sodimac Tech Team
 * @version 2.0
 * @since 2025
 */
@Service
@Slf4j
public class MessageCatalogServiceImpl implements MessageCatalogService {

    @Value("${catalogs.api.enabled:false}")
    private boolean catalogsApiEnabled;

    @Value("${catalogs.api.url:http://catalogos-api:8080}")
    private String catalogsApiUrl;

    @Value("${catalogs.api.lang:1}")
    private int defaultLangId;

    private final RestTemplate restTemplate;

    /**
     * Cache local de mensajes para evitar llamadas repetidas.
     * Key: catalogKey_langId, Value: mensaje
     */
    private final Map<String, String> messageCache = new ConcurrentHashMap<>();

    public MessageCatalogServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getMessage(FiscalMessageCode messageCode) {
        if (!catalogsApiEnabled) {
            return messageCode.getMessage();
        }
        return getMessageFromCatalog(messageCode.getCode(), messageCode.getMessage());
    }

    @Override
    public String getMessage(FiscalMessageCode messageCode, Object... params) {
        String message = getMessage(messageCode);
        return formatMessage(message, params);
    }

    @Override
    public String getWarningMessage(FiscalWarningCode warningCode) {
        if (!catalogsApiEnabled) {
            return warningCode.getMessage();
        }
        return getMessageFromCatalog(warningCode.getCode(), warningCode.getMessage());
    }

    @Override
    public String getWarningMessage(FiscalWarningCode warningCode, Object... params) {
        String message = getWarningMessage(warningCode);
        return formatMessage(message, params);
    }

    @Override
    public String getCode(FiscalMessageCode messageCode) {
        return messageCode.getCode();
    }

    @Override
    public String getWarningCode(FiscalWarningCode warningCode) {
        return warningCode.getCode();
    }

    @Override
    public String getFullMessage(FiscalMessageCode messageCode) {
        return String.format("[%s] %s", messageCode.getCode(), getMessage(messageCode));
    }

    @Override
    public String getFullWarningMessage(FiscalWarningCode warningCode) {
        return String.format("[%s] %s", warningCode.getCode(), getWarningMessage(warningCode));
    }

    @Override
    public void throwException(FiscalMessageCode messageCode) {
        throw createException(messageCode);
    }

    @Override
    public void throwExceptionWithParams(FiscalMessageCode messageCode, Object... params) {
        throw createExceptionWithParams(messageCode, params);
    }

    @Override
    public void throwException(FiscalMessageCode messageCode, String additionalInfo) {
        throw createException(messageCode, additionalInfo);
    }

    @Override
    public void throwException(FiscalMessageCode messageCode, Throwable cause) {
        throw createException(messageCode, cause);
    }

    @Override
    public void throwException(FiscalMessageCode messageCode, String additionalInfo, Throwable cause) {
        throw createException(messageCode, additionalInfo, cause);
    }

    // ========== MÉTODOS CREATE EXCEPTION ==========

    @Override
    public FiscalException createException(FiscalMessageCode messageCode) {
        String resolvedMessage = getMessage(messageCode);
        logException(messageCode.getCode(), resolvedMessage, null, null);
        return new FiscalException(messageCode, resolvedMessage, null, null);
    }

    @Override
    public FiscalException createExceptionWithParams(FiscalMessageCode messageCode, Object... params) {
        String resolvedMessage = getMessage(messageCode, params);
        logException(messageCode.getCode(), resolvedMessage, null, null);
        return new FiscalException(messageCode, resolvedMessage, null, null);
    }

    @Override
    public FiscalException createException(FiscalMessageCode messageCode, String additionalInfo) {
        String resolvedMessage = getMessage(messageCode);
        logException(messageCode.getCode(), resolvedMessage, additionalInfo, null);
        return new FiscalException(messageCode, resolvedMessage, additionalInfo, null);
    }

    @Override
    public FiscalException createException(FiscalMessageCode messageCode, Throwable cause) {
        String resolvedMessage = getMessage(messageCode);
        logException(messageCode.getCode(), resolvedMessage, null, cause);
        return new FiscalException(messageCode, resolvedMessage, null, cause);
    }

    @Override
    public FiscalException createException(FiscalMessageCode messageCode, String additionalInfo, Throwable cause) {
        String resolvedMessage = getMessage(messageCode);
        logException(messageCode.getCode(), resolvedMessage, additionalInfo, cause);
        return new FiscalException(messageCode, resolvedMessage, additionalInfo, cause);
    }

    /**
     * Registra el log de la excepción según el tipo de error.
     * - BUS### (negocio) -> log.warn (error esperado/controlado)
     * - ERR### (técnico) -> log.error (problema del sistema)
     */
    private void logException(String code, String message, String additionalInfo, Throwable cause) {
        boolean isBusinessError = code != null && code.startsWith("BUS");
        String logMessage = additionalInfo != null
                ? String.format("Creando excepción: [%s] %s - %s", code, message, additionalInfo)
                : String.format("Creando excepción: [%s] %s", code, message);

        if (isBusinessError) {
            log.warn(logMessage);
        } else {
            if (cause != null) {
                log.error(logMessage, cause);
            } else {
                log.error(logMessage);
            }
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    /**
     * Obtiene el ID de idioma actual desde el contexto del request.
     * Usa LocaleContextHolder que es establecido por LocaleFilter.
     *
     * @return ID de idioma para catalogos-api
     */
    private int getCurrentLanguageId() {
        Locale locale = LocaleContextHolder.getLocale();
        int langId = LanguageIdMapper.getLanguageId(locale);
        log.trace("Idioma del contexto: {} -> langId: {}", locale.getLanguage(), langId);
        return langId;
    }

    /**
     * Obtiene el mensaje desde catalogos-api o usa fallback.
     *
     * El idioma se obtiene automáticamente del contexto del request
     * (establecido por LocaleFilter desde Accept-Language o X-Language header).
     *
     * @param catalogKey Clave del catálogo (ej: BUS034, ERR001)
     * @param fallbackMessage Mensaje por defecto si falla
     * @return Mensaje del catálogo o fallback
     */
    @SuppressWarnings("unchecked")
    private String getMessageFromCatalog(String catalogKey, String fallbackMessage) {
        int langId = getCurrentLanguageId();
        String cacheKey = catalogKey + "_" + langId;

        // Verificar cache
        String cachedMessage = messageCache.get(cacheKey);
        if (cachedMessage != null) {
            return cachedMessage;
        }

        try {
            // Llamar a catalogos-api
            String url = String.format("%s/message/%s?lang=%d",
                    catalogsApiUrl, catalogKey, langId);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() &&
                response.getBody() != null &&
                response.getBody().containsKey("description")) {

                String message = (String) response.getBody().get("description");
                messageCache.put(cacheKey, message);
                log.debug("Mensaje obtenido de catalogos-api para {} (lang={}): {}", catalogKey, langId, message);
                return message;
            }
        } catch (Exception e) {
            log.warn("Error consultando catalogos-api para clave {} (lang={}): {}. Usando fallback.",
                    catalogKey, langId, e.getMessage());
        }

        // Fallback: usar mensaje del enum
        return fallbackMessage;
    }

    /**
     * Formatea un mensaje con parámetros opcionales.
     *
     * Soporta dos formatos de placeholders:
     * - MessageFormat: {0}, {1}, {2}... (recomendado para multi-idioma)
     * - String.format: %s (legacy)
     *
     * @param message Mensaje base
     * @param params Parámetros para formatear
     * @return Mensaje formateado
     */
    private String formatMessage(String message, Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }

        // Formato MessageFormat: {0}, {1}, {2}...
        if (message.contains("{0}") || message.contains("{1}")) {
            try {
                return java.text.MessageFormat.format(message, params);
            } catch (Exception e) {
                log.warn("Error formateando mensaje con MessageFormat: {}", e.getMessage());
            }
        }

        // Formato String.format: %s
        if (message.contains("%s")) {
            try {
                return String.format(message, params);
            } catch (Exception e) {
                log.warn("Error formateando mensaje con String.format: {}", e.getMessage());
            }
        }

        // Fallback: concatenar parámetros al final
        StringBuilder sb = new StringBuilder(message);
        sb.append(": ");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params[i] != null ? params[i].toString() : "null");
        }
        return sb.toString();
    }

    /**
     * Limpia el cache de mensajes.
     * Útil para pruebas o cuando se actualiza el catálogo.
     */
    public void clearCache() {
        messageCache.clear();
        log.info("Cache de mensajes limpiado");
    }

    /**
     * Obtiene el tamaño actual del cache.
     *
     * @return Número de mensajes en cache
     */
    public int getCacheSize() {
        return messageCache.size();
    }

    /**
     * Indica si catalogos-api está habilitado.
     *
     * @return true si consulta catalogos-api, false si usa fallback
     */
    public boolean isCatalogsApiEnabled() {
        return catalogsApiEnabled;
    }
}

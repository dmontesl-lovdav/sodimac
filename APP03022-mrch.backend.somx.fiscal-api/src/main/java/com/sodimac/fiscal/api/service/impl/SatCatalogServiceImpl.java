package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.service.SatCatalogService;
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
 * Implementación del servicio de catálogos SAT y datos.
 *
 * Consulta catalogos-api para obtener descripciones de catálogos como:
 * - c_TipoRelacion: Tipos de relación entre CFDIs
 * - TipoAddenda: Tipos de addenda Sodimac
 *
 * Incluye fallback a descripciones hardcodeadas cuando catalogos-api no está disponible.
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class SatCatalogServiceImpl implements SatCatalogService {

    private static final String CATALOG_TIPO_RELACION = "c_TipoRelacion";
    private static final String CATALOG_TIPO_ADDENDA = "TipoAddenda";
    private static final String UNKNOWN = "Desconocido";

    @Value("${catalogs.api.enabled:false}")
    private boolean catalogsApiEnabled;

    @Value("${catalogs.api.url:http://catalogos-api:8080}")
    private String catalogsApiUrl;

    private final RestTemplate restTemplate;

    /**
     * Cache local de descripciones para evitar llamadas repetidas.
     * Key: catalogCode_externalKey_langId, Value: descripción
     */
    private final Map<String, String> descriptionCache = new ConcurrentHashMap<>();

    public SatCatalogServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getTipoRelacionDescription(String tipoRelacion) {
        if (tipoRelacion == null || tipoRelacion.isBlank()) {
            return UNKNOWN;
        }

        if (!catalogsApiEnabled) {
            return getFallbackTipoRelacion(tipoRelacion);
        }

        String description = getCatalogDescription(CATALOG_TIPO_RELACION, tipoRelacion);
        return description != null ? description : getFallbackTipoRelacion(tipoRelacion);
    }

    @Override
    public String getTipoAddendaDescription(Integer tipoAddenda) {
        if (tipoAddenda == null) {
            return UNKNOWN;
        }

        if (!catalogsApiEnabled) {
            return getFallbackTipoAddenda(tipoAddenda);
        }

        String description = getCatalogDescription(CATALOG_TIPO_ADDENDA, tipoAddenda.toString());
        return description != null ? description : getFallbackTipoAddenda(tipoAddenda);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getCatalogDescription(String catalogCode, String externalKey) {
        if (catalogCode == null || externalKey == null) {
            return null;
        }

        int langId = getCurrentLanguageId();
        String cacheKey = catalogCode + "_" + externalKey + "_" + langId;

        // Verificar cache
        String cachedDescription = descriptionCache.get(cacheKey);
        if (cachedDescription != null) {
            return cachedDescription;
        }

        try {
            // Endpoint: GET /{catalogCode}/details/{externalKey}?lang={langId}
            // Nota: El endpoint busca por key, pero necesitamos buscar por external_key
            // Usamos el endpoint alternativo que busca por external_key
            String url = String.format("%s/%s/details?lang=%d",
                    catalogsApiUrl, catalogCode, langId);

            ResponseEntity<Map[]> response = restTemplate.getForEntity(url, Map[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                for (Map detail : response.getBody()) {
                    String detailExternalKey = (String) detail.get("externalKey");
                    if (externalKey.equals(detailExternalKey)) {
                        String description = (String) detail.get("description");
                        if (description != null) {
                            descriptionCache.put(cacheKey, description);
                            log.debug("Descripcion obtenida de catalogos-api para {}/{} (lang={}): {}",
                                    catalogCode, externalKey, langId, description);
                            return description;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error consultando catalogos-api para {}/{} (lang={}): {}. Usando fallback.",
                    catalogCode, externalKey, langId, e.getMessage());
        }

        return null;
    }

    @Override
    public void clearCache() {
        descriptionCache.clear();
        log.info("Cache de catalogos SAT limpiado");
    }

    @Override
    public boolean isCatalogsApiEnabled() {
        return catalogsApiEnabled;
    }

    /**
     * Obtiene el ID de idioma actual desde el contexto del request.
     */
    private int getCurrentLanguageId() {
        Locale locale = LocaleContextHolder.getLocale();
        int langId = LanguageIdMapper.getLanguageId(locale);
        log.trace("Idioma del contexto: {} -> langId: {}", locale.getLanguage(), langId);
        return langId;
    }

    /**
     * Fallback para TipoRelacion cuando catalogos-api no está disponible.
     * Basado en el catálogo c_TipoRelacion del SAT (Anexo 20 CFDI 4.0).
     */
    private String getFallbackTipoRelacion(String tipoRelacion) {
        return switch (tipoRelacion) {
            case "01" -> "Nota de crédito de los documentos relacionados";
            case "02" -> "Nota de débito de los documentos relacionados";
            case "03" -> "Devolución de mercancía sobre facturas o traslados previos";
            case "04" -> "Sustitución de los CFDI previos";
            case "05" -> "Traslados de mercancías facturados previamente";
            case "06" -> "Factura generada por los traslados previos";
            case "07" -> "CFDI por aplicación de anticipo";
            case "08" -> "Factura generada por pagos en parcialidades";
            case "09" -> "Factura generada por pagos diferidos";
            default -> UNKNOWN;
        };
    }

    /**
     * Fallback para TipoAddenda cuando catalogos-api no está disponible.
     * Basado en la clasificación interna de Sodimac.
     */
    private String getFallbackTipoAddenda(Integer tipoAddenda) {
        return switch (tipoAddenda) {
            case 1 -> "Addenda Estándar";
            case 2 -> "Addenda con Carta Porte";
            case 3 -> "Addenda Complemento de Pago";
            case 4 -> "Addenda Internacional";
            case 5 -> "Addenda Sodimac";
            default -> UNKNOWN;
        };
    }
}

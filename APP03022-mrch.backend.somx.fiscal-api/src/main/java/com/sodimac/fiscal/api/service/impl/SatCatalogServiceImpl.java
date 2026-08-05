package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.repository.AddendumRepository;
import com.sodimac.fiscal.api.service.SatCatalogService;
import com.sodimac.fiscal.api.util.LanguageIdMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lectura de catálogos de shared_catalogs DIRECTO desde la BD (misma base b2b_portal),
 * sin pasar por util-api. Antes usaba HTTP a util-api ({utils.api.url}/catalog/...); si fiscal
 * no alcanzaba util-api en un ambiente, los valores volvían vacíos y disparaban rechazos falsos
 * (ej. BUS058/BUS059). Ahora se resuelve por JPA (AddendumRepository, native shared_catalogs),
 * mismo patrón que CatTipoRelacionFacturaNC. Las descripciones conservan fallback hardcodeado.
 */
@Service
@Slf4j
public class SatCatalogServiceImpl implements SatCatalogService {

    private static final String CATALOG_TIPO_RELACION = "c_TipoRelacion";
    private static final String CATALOG_TIPO_ADDENDA = "TipoAddenda";
    private static final String UNKNOWN = "Desconocido";

    private final AddendumRepository addendumRepository;

    private final Map<String, String> descriptionCache = new ConcurrentHashMap<>();

    public SatCatalogServiceImpl(AddendumRepository addendumRepository) {
        this.addendumRepository = addendumRepository;
    }

    @Override
    public String getTipoRelacionDescription(String tipoRelacion) {
        if (tipoRelacion == null || tipoRelacion.isBlank()) {
            return UNKNOWN;
        }
        String description = getCatalogDescription(CATALOG_TIPO_RELACION, tipoRelacion);
        return description != null ? description : getFallbackTipoRelacion(tipoRelacion);
    }

    @Override
    public String getTipoAddendaDescription(Integer tipoAddenda) {
        if (tipoAddenda == null) {
            return UNKNOWN;
        }
        String description = getCatalogDescription(CATALOG_TIPO_ADDENDA, tipoAddenda.toString());
        return description != null ? description : getFallbackTipoAddenda(tipoAddenda);
    }

    @Override
    public String getCatalogDescription(String catalogCode, String externalKey) {
        if (catalogCode == null || externalKey == null) {
            return null;
        }

        int langId = getCurrentLanguageId();
        String cacheKey = catalogCode + "_" + externalKey + "_" + langId;

        String cachedDescription = descriptionCache.get(cacheKey);
        if (cachedDescription != null) {
            return cachedDescription;
        }

        try {
            String description = addendumRepository.findCatalogDescription(catalogCode, externalKey, langId);
            if (description != null) {
                descriptionCache.put(cacheKey, description);
                log.debug("Descripción de catálogo {}/{} (lang={}): {}", catalogCode, externalKey, langId, description);
                return description;
            }
        } catch (Exception e) {
            log.warn("Error leyendo descripción del catálogo {}/{} (lang={}): {}. Usando fallback.",
                    catalogCode, externalKey, langId, e.getMessage());
        }

        return null;
    }

    @Override
    public java.util.Set<String> getActiveCatalogValues(String catalogCode) {
        java.util.Set<String> values = new java.util.HashSet<>();
        if (catalogCode == null || catalogCode.isBlank()) {
            return values;
        }
        try {
            List<String> activos = addendumRepository.findActiveCatalogValues(catalogCode);
            for (String value : activos) {
                if (value != null && !value.isBlank()) {
                    values.add(value.trim());
                }
            }
        } catch (Exception e) {
            log.warn("Error leyendo valores del catálogo {} en shared_catalogs: {}", catalogCode, e.getMessage());
        }
        return values;
    }

    @Override
    public void clearCache() {
        descriptionCache.clear();
        log.info("Cache de catalogos SAT limpiado");
    }

    private int getCurrentLanguageId() {
        Locale locale = LocaleContextHolder.getLocale();
        int langId = LanguageIdMapper.getLanguageId(locale);
        log.trace("Idioma del contexto: {} -> langId: {}", locale.getLanguage(), langId);
        return langId;
    }

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

package com.sodimac.fiscal.api.service;

/**
 * Servicio para consulta de catálogos SAT y catálogos de datos desde catalogos-api.
 *
 * Este servicio proporciona acceso a catálogos como:
 * - c_TipoRelacion: Tipos de relación entre CFDIs (01-09)
 * - TipoAddenda: Tipos de addenda Sodimac (1-5)
 * - Otros catálogos SAT según se requieran
 *
 * Funciona con el switch de configuración catalogs.api.enabled:
 * - true: Consulta catalogos-api para descripciones multi-idioma
 * - false: Usa descripciones fallback hardcodeadas
 *
 * @author Sodimac Tech Team
 * @version 1.0
 * @since 2025
 * @see MessageCatalogService
 */
public interface SatCatalogService {

    /**
     * Obtiene la descripción del tipo de relación entre CFDIs.
     * Consulta el catálogo c_TipoRelacion del SAT.
     *
     * @param tipoRelacion Código del tipo de relación (01-09)
     * @return Descripción del tipo de relación o "Desconocido" si no existe
     */
    String getTipoRelacionDescription(String tipoRelacion);

    /**
     * Obtiene la descripción del tipo de addenda Sodimac.
     * Consulta el catálogo TipoAddenda.
     *
     * @param tipoAddenda Código del tipo de addenda (1-5)
     * @return Descripción del tipo de addenda o "Desconocido" si no existe
     */
    String getTipoAddendaDescription(Integer tipoAddenda);

    /**
     * Obtiene la descripción de un catálogo genérico por código y clave externa.
     *
     * @param catalogCode Código del catálogo (ej: "c_TipoRelacion", "TipoAddenda")
     * @param externalKey Clave externa del elemento (ej: "01", "1")
     * @return Descripción del elemento o null si no existe
     */
    String getCatalogDescription(String catalogCode, String externalKey);

    /**
     * Limpia el cache de catálogos.
     * Útil para pruebas o cuando se actualiza el catálogo.
     */
    void clearCache();

    /**
     * Indica si el servicio de catálogos está habilitado.
     *
     * @return true si consulta catalogos-api, false si usa fallback
     */
    boolean isCatalogsApiEnabled();
}

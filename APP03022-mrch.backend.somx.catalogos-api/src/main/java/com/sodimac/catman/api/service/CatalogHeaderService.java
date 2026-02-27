package com.sodimac.catman.api.service;

import java.util.List;
import java.util.Optional;

import com.sodimac.catman.api.model.dto.CatalogDetailDto;
import com.sodimac.catman.api.model.dto.CatalogHeaderDto;

public interface CatalogHeaderService {

    List<CatalogHeaderDto> findAllCatalogs(Integer langId);

    List<CatalogHeaderDto> findCatalogsByModule(String module, Integer langId);

    Optional<CatalogHeaderDto> findCatalogByCode(String code, Integer langId);

    Optional<CatalogHeaderDto> findCatalogById(Integer id, Integer langId);

    Optional<CatalogHeaderDto> findCatalogByPrefix(String prefix, Integer langId);

    List<CatalogDetailDto> findDetailsByCode(String code, Integer langId);

    Optional<CatalogDetailDto> findDetailByCodeAndKey(String code, String key, Integer langId);

    Optional<CatalogDetailDto> findDetailByKey(String key, Integer langId);

    /**
     * Obtiene un mensaje por clave y formatea los placeholders {0}, {1}, etc.
     *
     * @param key Clave del mensaje (ej: BUS002, RES001)
     * @param langId ID del idioma
     * @param params Parámetros para sustituir placeholders
     * @return Detalle con mensaje formateado
     */
    Optional<CatalogDetailDto> findDetailByKeyFormatted(String key, Integer langId, List<String> params);

    /**
     * Obtiene detalles de un catálogo filtrados por dependencia de otro catálogo.
     * Ejemplo: GET /USO_CFDI/details?dependsOn=REGIMEN_FISCAL:605
     * Retorna los UsoCFDI que dependen del RegimenFiscal con external_key=605
     *
     * Si la tabla de relaciones está vacía o no hay match, retorna todos los detalles
     * del catálogo (comportamiento por defecto sin filtro).
     *
     * @param code Código del catálogo a consultar (ej: USO_CFDI)
     * @param targetCatalogCode Código del catálogo del que depende (ej: REGIMEN_FISCAL)
     * @param targetExternalKey Clave externa del detalle del que depende (ej: 605)
     * @param langId ID del idioma
     * @return Lista de detalles filtrados o todos si no hay dependencias
     */
    List<CatalogDetailDto> findDetailsByCodeWithDependency(String code, String targetCatalogCode, String targetExternalKey, Integer langId);
}

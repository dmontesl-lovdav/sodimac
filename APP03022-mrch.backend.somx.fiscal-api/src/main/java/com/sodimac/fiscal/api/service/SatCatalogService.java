package com.sodimac.fiscal.api.service;

public interface SatCatalogService {

    String getTipoRelacionDescription(String tipoRelacion);

    String getTipoAddendaDescription(Integer tipoAddenda);

    String getCatalogDescription(String catalogCode, String externalKey);

    /**
     * Devuelve el conjunto de valores activos y vigentes de un catálogo
     * (campos {@code value} y {@code externalKey} de cada detalle).
     *
     * Útil para validar pertenencia (ej: forma de pago / uso CFDI válidos para NC).
     * Si el catálogo no existe, está inactivo o util-api no responde, devuelve un set vacío.
     *
     * @param catalogCode código del catálogo (ej: CatFormaPagoValidoNc)
     * @return valores activos del catálogo; nunca null
     */
    java.util.Set<String> getActiveCatalogValues(String catalogCode);

    void clearCache();
}

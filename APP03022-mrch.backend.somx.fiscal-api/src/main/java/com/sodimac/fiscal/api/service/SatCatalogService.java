package com.sodimac.fiscal.api.service;

public interface SatCatalogService {

    String getTipoRelacionDescription(String tipoRelacion);

    String getTipoAddendaDescription(Integer tipoAddenda);

    String getCatalogDescription(String catalogCode, String externalKey);

    void clearCache();
}

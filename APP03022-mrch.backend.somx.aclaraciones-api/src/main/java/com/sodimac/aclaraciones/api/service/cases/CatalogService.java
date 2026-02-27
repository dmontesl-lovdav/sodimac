package com.sodimac.aclaraciones.api.service.cases;

import com.sodimac.aclaraciones.api.model.dto.CatalogDto;
import java.util.List;

/**
 * API de consulta/validación de catálogos de la aplicación.
 */
public interface CatalogService {

    /** Lista de elementos de catálogo. */
    List<CatalogDto> retrieveList(int type, int parentId);

    /**
     * Valida que el estatus exista en el catálogo.
     *
     * @throws IllegalStateException si el estatus no está registrado.
     */
    void requireStatus(Integer statusId);
}

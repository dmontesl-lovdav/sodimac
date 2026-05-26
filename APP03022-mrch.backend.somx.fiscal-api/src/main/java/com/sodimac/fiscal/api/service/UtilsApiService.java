package com.sodimac.fiscal.api.service;

/**
 * Servicio para consultar parámetros centralizados desde utils-api.
 *
 * Obtiene configuraciones dinámicas desde el microservicio utils-api,
 * que almacena parámetros en la tabla core_utils.cat_parameter.
 *
 * @author Sodimac Tech Team
 * @since 2025
 */
public interface UtilsApiService {

    /**
     * Obtiene el valor de un parámetro por su nombre.
     *
     * @param parameterName Nombre del parámetro (ej: MAX_SEARCH_MONTHS)
     * @param defaultValue Valor por defecto si no se encuentra o hay error
     * @return Valor del parámetro o valor por defecto
     */
    String getParameterValue(String parameterName, String defaultValue);

    /**
     * Obtiene el valor de un parámetro como entero.
     *
     * @param parameterName Nombre del parámetro
     * @param defaultValue Valor por defecto si no se encuentra o hay error
     * @return Valor del parámetro como entero
     */
    int getParameterValueAsInt(String parameterName, int defaultValue);

    /**
     * Obtiene el valor de un parámetro filtrado por módulo.
     *
     * @param parameterName Nombre del parámetro
     * @param moduleId ID del módulo (1=FISCAL, 2=CATALOGOS, etc.)
     * @param defaultValue Valor por defecto
     * @return Valor del parámetro o valor por defecto
     */
    String getParameterValue(String parameterName, int moduleId, String defaultValue);

}

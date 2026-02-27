package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.service.UtilsApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación del servicio de consulta de parámetros desde utils-api.
 *
 * ARQUITECTURA:
 * Funciona con un switch de configuración (utils.api.enabled):
 * - true: Consulta utils-api para obtener parámetros dinámicos
 * - false: Usa valores por defecto (modo offline/desarrollo)
 *
 * Incluye cache local para optimizar rendimiento.
 *
 * @author Sodimac Tech Team
 * @since 2025
 */
@Service
@Slf4j
public class UtilsApiServiceImpl implements UtilsApiService {

    @Value("${utils.api.enabled:false}")
    private boolean utilsApiEnabled;

    @Value("${utils.api.url:http://localhost:3712}")
    private String utilsApiUrl;

    private final RestTemplate restTemplate;

    /**
     * Cache local de parámetros para evitar llamadas repetidas.
     * Key: parameterName_moduleId, Value: valor del parámetro
     */
    private final Map<String, String> parameterCache = new ConcurrentHashMap<>();

    public UtilsApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getParameterValue(String parameterName, String defaultValue) {
        return getParameterValue(parameterName, 0, defaultValue);
    }

    @Override
    public int getParameterValueAsInt(String parameterName, int defaultValue) {
        String value = getParameterValue(parameterName, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Error parseando parámetro {} como entero: {}. Usando valor por defecto: {}",
                    parameterName, value, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public String getParameterValue(String parameterName, int moduleId, String defaultValue) {
        if (!utilsApiEnabled) {
            log.debug("utils-api deshabilitado. Usando valor por defecto para {}: {}", parameterName, defaultValue);
            return defaultValue;
        }

        String cacheKey = parameterName + "_" + moduleId;

        // Verificar cache
        String cachedValue = parameterCache.get(cacheKey);
        if (cachedValue != null) {
            log.debug("Parámetro {} obtenido de cache: {}", parameterName, cachedValue);
            return cachedValue;
        }

        return getParameterFromApi(parameterName, moduleId, defaultValue, cacheKey);
    }

    @Override
    public boolean isUtilsApiEnabled() {
        return utilsApiEnabled;
    }

    /**
     * Consulta el parámetro desde utils-api.
     *
     * Endpoint: GET /api/parameters?name={parameterName}&idModule={moduleId}
     *
     * Respuesta esperada:
     * {
     *   "success": true,
     *   "data": [{ "idParameter": 45, "name": "MAX_SEARCH_MONTHS", "value": "6", ... }],
     *   "count": 1
     * }
     */
    @SuppressWarnings("unchecked")
    private String getParameterFromApi(String parameterName, int moduleId, String defaultValue, String cacheKey) {
        try {
            // Construir URL con filtros
            StringBuilder urlBuilder = new StringBuilder(utilsApiUrl)
                    .append("/api/parameters?name=")
                    .append(parameterName);

            if (moduleId > 0) {
                urlBuilder.append("&idModule=").append(moduleId);
            }

            String url = urlBuilder.toString();
            log.debug("Consultando utils-api: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Boolean success = (Boolean) body.get("success");

                if (Boolean.TRUE.equals(success) && body.containsKey("data")) {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");

                    if (data != null && !data.isEmpty()) {
                        Map<String, Object> parameter = data.get(0);
                        String value = (String) parameter.get("value");

                        if (value != null) {
                            parameterCache.put(cacheKey, value);
                            log.info("Parámetro {} obtenido de utils-api: {}", parameterName, value);
                            return value;
                        }
                    }
                }
            }

            log.warn("Parámetro {} no encontrado en utils-api. Usando valor por defecto: {}",
                    parameterName, defaultValue);

        } catch (Exception e) {
            log.warn("Error consultando utils-api para parámetro {}: {}. Usando valor por defecto: {}",
                    parameterName, e.getMessage(), defaultValue);
        }

        return defaultValue;
    }

    /**
     * Limpia el cache de parámetros.
     * Útil para pruebas o cuando se actualizan parámetros.
     */
    public void clearCache() {
        parameterCache.clear();
        log.info("Cache de parámetros limpiado");
    }

    /**
     * Obtiene el tamaño actual del cache.
     *
     * @return Número de parámetros en cache
     */
    public int getCacheSize() {
        return parameterCache.size();
    }
}

package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.service.SupplierBlockApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Implementación del cliente de bloqueos de proveedor contra util-api.
 *
 * - Tipo de proveedor: GET /suppliers/number/{n}/type-blocked → {typeBlocked}
 * - Proveedor:         GET /supplier-blocks/supplier/{n}/is-blocked → {blocked}
 *
 * Tolerante a fallos: cualquier error o respuesta no concluyente => false (no bloquea).
 *
 * @author Sodimac Tech Team
 * @since QA junio-2026 (bloqueo de proveedores)
 */
@Service
@Slf4j
public class SupplierBlockApiServiceImpl implements SupplierBlockApiService {

    @Value("${utils.api.url:http://localhost:3712}")
    private String utilsApiUrl;

    private final RestTemplate restTemplate;

    public SupplierBlockApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isSupplierTypeBlocked(String supplierNumber) {
        if (supplierNumber == null || supplierNumber.isBlank()) {
            return false;
        }
        String url = String.format("%s/suppliers/number/%s/type-blocked", utilsApiUrl, supplierNumber);
        return queryBlocked(url, "typeBlocked", supplierNumber);
    }

    @Override
    public boolean isSupplierBlocked(String supplierNumber) {
        if (supplierNumber == null || supplierNumber.isBlank()) {
            return false;
        }
        String url = String.format("%s/supplier-blocks/supplier/%s/is-blocked", utilsApiUrl, supplierNumber);
        return queryBlocked(url, "blocked", supplierNumber);
    }

    private boolean queryBlocked(String url, String field, String supplierNumber) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object value = response.getBody().get(field);
                boolean blocked = Boolean.TRUE.equals(value);
                log.debug("Consulta bloqueo. url={} {}={}", url, field, blocked);
                return blocked;
            }
            log.warn("Respuesta no concluyente de util-api ({}). Se asume NO bloqueado. supplierNumber={}",
                    url, supplierNumber);
            return false;
        } catch (Exception e) {
            log.warn("No se pudo consultar bloqueo en util-api (se asume NO bloqueado). supplierNumber={} error={}",
                    supplierNumber, e.getMessage());
            return false;
        }
    }
}

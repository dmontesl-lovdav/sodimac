package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.service.AuditoriaApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del cliente para el servicio de Auditoría (auditoria-api).
 *
 * Consume el endpoint POST /api/activity-logs/ para registrar actividades
 * en la bitácora centralizada (core_audit.activity_logs).
 *
 * Configuración:
 * - auditoria.api.enabled: habilita/deshabilita el servicio
 * - auditoria.api.url: URL base del servicio
 *
 * @author Sodimac Tech Team
 * @since STM-704
 */
@Service
@Slf4j
public class AuditoriaApiServiceImpl implements AuditoriaApiService {

    private static final String MODULO = "fiscal-api";
    private static final String ENDPOINT = "/api/activity-logs/";
    private static final int MAX_MESSAGE_LENGTH = 100;

    @Value("${auditoria.api.enabled:false}")
    private boolean auditoriaEnabled;

    @Value("${auditoria.api.url:http://localhost:8091}")
    private String auditoriaUrl;

    private final RestTemplate restTemplate;

    public AuditoriaApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void logActivity(String traceId, String action, String serviceName,
                            String userId, boolean isError, String message,
                            String messageDetail, Map<String, Object> details,
                            Long durationMs) {
        if (!auditoriaEnabled) {
            log.info("auditoria-api deshabilitado. Omitiendo log: action={}, traceId={}", action, traceId);
            return;
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("traceId", traceId);
            body.put("modulo", MODULO);
            body.put("action", action);
            body.put("serviceName", serviceName);
            body.put("userId", userId != null ? userId : "system");
            body.put("isError", isError);
            body.put("message", truncate(message, MAX_MESSAGE_LENGTH));
            body.put("messageDetail", messageDetail);
            body.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            if (details != null) {
                body.put("details", details);
            }
            if (durationMs != null) {
                body.put("durationms", durationMs);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            String url = auditoriaUrl + ENDPOINT;
            restTemplate.postForEntity(url, request, Map.class);

            log.info("Actividad registrada en auditoria-api: action={}, traceId={}", action, traceId);

        } catch (Exception e) {
            log.warn("Error registrando actividad en auditoria-api (no afecta operacion principal): action={}, error={}",
                    action, e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return auditoriaEnabled;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

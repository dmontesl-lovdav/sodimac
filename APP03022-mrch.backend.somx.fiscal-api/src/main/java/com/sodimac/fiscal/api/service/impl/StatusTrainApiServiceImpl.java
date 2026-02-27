package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.dto.StatusTrainValidationResult;
import com.sodimac.fiscal.api.service.StatusTrainApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Implementación del cliente para el servicio de Tren de Estatus (STM-1166).
 *
 * Consume el endpoint /status-train/validate de catalogos-api para validar
 * si una transición de estatus está permitida.
 *
 * @author Sodimac Tech Team
 * @since STM-410
 */
@Service
@Slf4j
public class StatusTrainApiServiceImpl implements StatusTrainApiService {

    @Value("${status-train.api.enabled:true}")
    private boolean statusTrainEnabled;

    @Value("${status-train.api.url:http://localhost:8083}")
    private String statusTrainUrl;

    @Value("${status-train.api.timeout-ms:5000}")
    private int timeoutMs;

    private final RestTemplate restTemplate;

    public StatusTrainApiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StatusTrainValidationResult validateTransition(Integer optionId, Integer sourceStatus, Integer targetStatus) {
        if (!statusTrainEnabled) {
            log.warn("Status Train API deshabilitado - todas las transiciones serán permitidas");
            return StatusTrainValidationResult.valid();
        }

        String url = String.format("%s/status-train/validate?optionId=%d&sourceStatus=%d&targetStatus=%d",
                statusTrainUrl, optionId, sourceStatus, targetStatus);

        log.debug("Validando transición: optionId={}, sourceStatus={} -> targetStatus={}",
                optionId, sourceStatus, targetStatus);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Boolean valid = (Boolean) body.get("valid");

                if (Boolean.TRUE.equals(valid)) {
                    log.debug("Transición permitida: {} -> {}", sourceStatus, targetStatus);
                    return StatusTrainValidationResult.valid();
                }
            }

            // Si llegamos aquí con 200 pero valid=false, no debería pasar normalmente
            log.warn("Respuesta inesperada del servicio de status-train");
            return StatusTrainValidationResult.transitionNotAllowed();

        } catch (HttpClientErrorException.BadRequest e) {
            // El servicio retorna 400 cuando la transición no es válida
            return handleBadRequestError(e);

        } catch (Exception e) {
            log.error("Error comunicándose con status-train API: {}", e.getMessage(), e);
            return StatusTrainValidationResult.serviceError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private StatusTrainValidationResult handleBadRequestError(HttpClientErrorException.BadRequest e) {
        try {
            // Intentar parsear el body del error
            String responseBody = e.getResponseBodyAsString();
            log.debug("Respuesta de error del servicio: {}", responseBody);

            // El servicio retorna JSON con code y message
            if (responseBody.contains("WRN7010")) {
                log.warn("Estatus origen no catalogado");
                return StatusTrainValidationResult.sourceNotFound();
            } else if (responseBody.contains("WRN7011")) {
                log.warn("Transición no permitida");
                return StatusTrainValidationResult.transitionNotAllowed();
            }

        } catch (Exception parseError) {
            log.warn("No se pudo parsear respuesta de error: {}", parseError.getMessage());
        }

        // Fallback: asumir que es transición no permitida
        return StatusTrainValidationResult.transitionNotAllowed();
    }

    @Override
    public boolean isEnabled() {
        return statusTrainEnabled;
    }
}

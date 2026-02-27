package com.sodimac.fiscal.api.service.impl;

import com.sodimac.fiscal.api.model.entity.LogEntity;
import com.sodimac.fiscal.api.repository.LogRepository;
import com.sodimac.fiscal.api.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación del servicio de log de actividad.
 *
 * Usa REQUIRES_NEW para crear una transacción independiente, de modo que
 * los errores de persistencia del log no afecten la transacción principal.
 *
 * @author Sodimac Tech Team
 * @since 2026-02-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final LogRepository logRepository;

    /**
     * {@inheritDoc}
     *
     * IMPORTANTE: Usa REQUIRES_NEW para que este método tenga su propia transacción.
     * Si falla el INSERT a la tabla log (ej: FK constraint), la transacción principal
     * NO se verá afectada y el error de negocio original se propagará correctamente.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveActivityLog(
            String operationType,
            UUID invoiceUuid,
            LocalDateTime startTime,
            String statusCode,
            String statusMessage,
            String requestData,
            String responseData,
            Long userId) {

        try {
            LogEntity logEntry = new LogEntity();
            logEntry.setOperationType(operationType);
            logEntry.setCfdiUuid(invoiceUuid);  // Usar invoiceUuid (PK), no fiscalUuid
            logEntry.setTransactionDate(LocalDateTime.now());
            logEntry.setRecordStartDate(startTime);
            logEntry.setRecordEndDate(LocalDateTime.now());
            logEntry.setStatusCode(statusCode);
            logEntry.setStatusMessage(truncateMessage(statusMessage, 500));
            logEntry.setRequestData(requestData);
            logEntry.setResponseData(responseData);
            logEntry.setCreatedBy(userId);

            logRepository.save(logEntry);
            log.debug("Actividad registrada en bitacora. Operation: {}, Invoice UUID: {}, Status: {}",
                    operationType, invoiceUuid, statusCode);

        } catch (Exception e) {
            // Capturar cualquier error y solo loguearlo
            // La transacción de este método hará rollback, pero NO afectará
            // la transacción principal del servicio que lo llamó
            log.error("Error registrando actividad en bitacora (no afecta operacion principal): {}",
                    e.getMessage());
        }
    }

    /**
     * Trunca un mensaje si excede la longitud máxima.
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return null;
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength - 3) + "...";
    }
}

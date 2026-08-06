package com.sodimac.fiscal.api.service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio para registro de actividad en bitácora.
 *
 * Este servicio usa una transacción independiente (REQUIRES_NEW) para que
 * los errores de log no afecten la transacción principal de negocio.
 *
 * @author Sodimac Tech Team
 * @since 2026-02-03
 */
public interface ActivityLogService {

    /**
     * Registra una actividad en la bitácora.
     *
     * IMPORTANTE: Este método usa REQUIRES_NEW para crear una transacción
     * independiente. Si falla el registro de log, no afectará la transacción
     * principal.
     *
     * @param operationType Tipo de operación (CREATE, UPDATE, DELETE, etc.)
     * @param invoiceUuid UUID interno del invoice (PK de tabla invoice)
     * @param startTime Hora de inicio de la operación
     * @param statusCode Código de resultado (BUS048, BUS3001, etc.)
     * @param statusMessage Mensaje de resultado
     * @param requestData JSON del request
     * @param responseData JSON del response (puede ser null si hubo error)
     * @param userId ID del usuario que realizó la operación
     */
    void saveActivityLog(
            String operationType,
            UUID invoiceUuid,
            LocalDateTime startTime,
            String statusCode,
            String statusMessage,
            String requestData,
            String responseData,
            UUID userId);
}

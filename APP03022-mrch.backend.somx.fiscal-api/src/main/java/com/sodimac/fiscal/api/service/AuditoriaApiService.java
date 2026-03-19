package com.sodimac.fiscal.api.service;

import java.util.Map;

/**
 * Servicio cliente para consumir el API de Auditoría (auditoria-api).
 *
 * Registra actividades en la bitácora centralizada (core_audit.activity_logs)
 * mediante llamadas HTTP fire-and-forget.
 *
 * @author Sodimac Tech Team
 * @since STM-704
 */
public interface AuditoriaApiService {

    /**
     * Registra una actividad en la bitácora de auditoría.
     *
     * La llamada es asíncrona (fire-and-forget): no bloquea el flujo principal
     * y si falla, solo se loguea warning sin afectar la operación de negocio.
     *
     * @param traceId       ID de transacción para correlacionar pasos
     * @param action        Nombre de la acción (ej: VALIDAR_VERSION_CFDI)
     * @param serviceName   Nombre del servicio (ej: InvoiceService.registerInvoice)
     * @param userId        ID del usuario
     * @param isError       true si el paso falló
     * @param message       Mensaje funcional (negocio)
     * @param messageDetail Mensaje técnico detallado
     * @param details       Datos adicionales (JSONB), puede ser null
     * @param durationMs    Duración en milisegundos, puede ser null
     */
    void logActivity(String traceId, String action, String serviceName,
                     String userId, boolean isError, String message,
                     String messageDetail, Map<String, Object> details,
                     Long durationMs);

    /**
     * Verifica si el servicio de auditoría está habilitado.
     *
     * @return true si el servicio está habilitado
     */
    boolean isEnabled();
}

package com.invoicesync.infrastructure.adapter.external;

import com.invoicesync.domain.model.BatchExecutionLog;
import com.invoicesync.domain.port.output.AlertNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailAlertNotificationAdapter implements AlertNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailAlertNotificationAdapter.class);

    @Value("${alert.notification.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${alert.notification.email.recipients:soporte@empresa.com}")
    private String recipients;

    @Override
    public void sendExecutionCompletedAlert(BatchExecutionLog executionLog) {
        String subject = String.format("[Invoice Sync] Ejecución completada - %s errores",
                executionLog.getErrorCount());

        String body = buildCompletionBody(executionLog);

        sendAlert(subject, body);
    }

    @Override
    public void sendExecutionFailedAlert(BatchExecutionLog executionLog, String errorDetail) {
        String subject = "[Invoice Sync] ERROR - Ejecución fallida";

        String body = String.format("""
            La sincronización de facturas ha fallado.

            Detalles:
            - ID Ejecución: %s
            - Intento: %d
            - Hora inicio: %s
            - Hora fin: %s
            - Error: %s

            Facturas procesadas antes del error: %d
            """,
                executionLog.getExecutionId(),
                executionLog.getAttemptNumber(),
                executionLog.getStartTime(),
                executionLog.getEndTime(),
                errorDetail,
                executionLog.getTotalInvoicesProcessed()
        );

        sendAlert(subject, body);
    }

    @Override
    public void sendCriticalErrorAlert(String errorMessage) {
        String subject = "[Invoice Sync] ALERTA CRÍTICA";

        String body = String.format("""
            Se ha producido un error crítico en el proceso de sincronización.

            Error: %s

            Por favor, revise los logs para más detalles.
            """, errorMessage);

        sendAlert(subject, body);
    }

    @Override
    public void sendRetryNotification(int attemptNumber, int maxAttempts, String reason) {
        String subject = String.format("[Invoice Sync] Reintento %d/%d", attemptNumber, maxAttempts);

        String body = String.format("""
            El proceso de sincronización está reintentando.

            - Intento actual: %d de %d
            - Razón del reintento: %s
            - Próximo intento en: 30 minutos
            """, attemptNumber, maxAttempts, reason);

        sendAlert(subject, body);
    }

    private void sendAlert(String subject, String body) {
        if (emailEnabled) {
            // TODO: Implementar envío real de email
            log.info("Sending email alert to {}: {}", recipients, subject);
            log.debug("Email body: {}", body);
        } else {
            log.info("=== ALERT (email disabled) ===");
            log.info("Subject: {}", subject);
            log.info("Body: {}", body);
            log.info("==============================");
        }
    }

    private String buildCompletionBody(BatchExecutionLog log) {
        return String.format("""
            Resumen de ejecución del proceso de sincronización de facturas:

            ID Ejecución: %s
            Hora inicio: %s
            Hora fin: %s
            Duración: %d segundos

            Resultados:
            - Total procesadas: %d
            - Exitosas: %d
            - Con errores: %d
            - Sin cambios: %d
            - Tasa de éxito: %.2f%%

            Cifras control ANTES:
            %s

            Cifras control DESPUÉS:
            %s
            """,
                log.getExecutionId(),
                log.getStartTime(),
                log.getEndTime(),
                log.getDuration().toSeconds(),
                log.getTotalInvoicesProcessed(),
                log.getSuccessCount(),
                log.getErrorCount(),
                log.getSkippedCount(),
                log.getSuccessRate(),
                log.getControlCifrasBefore() != null ? log.getControlCifrasBefore().getSummary() : "N/A",
                log.getControlCifrasAfter() != null ? log.getControlCifrasAfter().getSummary() : "N/A"
        );
    }
}

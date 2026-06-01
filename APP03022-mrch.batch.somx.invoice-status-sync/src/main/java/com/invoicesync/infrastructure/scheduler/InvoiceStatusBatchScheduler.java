package com.invoicesync.infrastructure.scheduler;

import com.invoicesync.application.dto.BatchExecutionResponse;
import com.invoicesync.application.mapper.BatchExecutionMapper;
import com.invoicesync.domain.model.BatchExecutionLog;
import com.invoicesync.domain.port.input.InvoiceStatusBatchUseCase;
import com.invoicesync.domain.port.output.AlertNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnProperty(name = "batch.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class InvoiceStatusBatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusBatchScheduler.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InvoiceStatusBatchUseCase batchUseCase;
    private final AlertNotificationService alertService;
    private final BatchExecutionMapper mapper;

    @Value("${batch.scheduler.max-retries:2}")
    private int maxRetries;

    @Value("${batch.scheduler.retry-interval-minutes:30}")
    private int retryIntervalMinutes;

    private BatchExecutionLog lastExecutionLog;

    public InvoiceStatusBatchScheduler(
            InvoiceStatusBatchUseCase batchUseCase,
            AlertNotificationService alertService,
            BatchExecutionMapper mapper) {
        this.batchUseCase = batchUseCase;
        this.alertService = alertService;
        this.mapper = mapper;
    }

    /**
     * Ejecuta la sincronización de estados de facturas todos los días a las 07:30 AM.
     * En caso de error, reintenta hasta 2 veces con intervalos de 30 minutos.
     */
    @Scheduled(cron = "${batch.scheduler.cron:0 30 7 * * *}")
    public void executeScheduledBatch() {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║  INVOICE STATUS BATCH - Scheduled execution started           ║");
        log.info("║  Time: {}                                    ║", LocalDateTime.now().format(FORMATTER));
        log.info("╚════════════════════════════════════════════════════════════════╝");

        try {
            lastExecutionLog = batchUseCase.executeStatusSyncWithRetry(maxRetries, retryIntervalMinutes);

            logExecutionSummary(lastExecutionLog);

        } catch (Exception e) {
            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║  BATCH EXECUTION FAILED AFTER ALL RETRIES                     ║");
            log.error("║  Error: {}                                                    ", e.getMessage());
            log.error("╚════════════════════════════════════════════════════════════════╝");

            alertService.sendCriticalErrorAlert("Batch failed after all retries: " + e.getMessage());
        }
    }

    /**
     * Permite ejecutar el batch manualmente desde la API
     */
    public BatchExecutionResponse triggerManualExecution() {
        log.info("Manual batch execution triggered at {}", LocalDateTime.now().format(FORMATTER));

        if (batchUseCase.isExecutionInProgress()) {
            throw new IllegalStateException("Batch execution already in progress");
        }

        lastExecutionLog = batchUseCase.executeStatusSync();
        return mapper.toResponse(lastExecutionLog);
    }

    /**
     * Obtiene el estado del último batch ejecutado
     */
    public BatchExecutionResponse getLastExecutionStatus() {
        if (lastExecutionLog == null) {
            return null;
        }
        return mapper.toResponse(lastExecutionLog);
    }

    public boolean isExecutionInProgress() {
        return batchUseCase.isExecutionInProgress();
    }

    private void logExecutionSummary(BatchExecutionLog executionLog) {
        log.info("╔════════════════════════════════════════════════════════════════╗");
        log.info("║                    BATCH EXECUTION SUMMARY                     ║");
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║  Execution ID: {}  ║", executionLog.getExecutionId());
        log.info("║  Duration: {} seconds                                          ║", executionLog.getDuration().toSeconds());
        log.info("║  Attempt: {}/{}                                                ║", executionLog.getAttemptNumber(), maxRetries + 1);
        log.info("╠════════════════════════════════════════════════════════════════╣");
        log.info("║  Total Processed: {}                                           ║", executionLog.getTotalInvoicesProcessed());
        log.info("║  Success: {}                                                   ║", executionLog.getSuccessCount());
        log.info("║  Errors: {}                                                    ║", executionLog.getErrorCount());
        log.info("║  Skipped: {}                                                   ║", executionLog.getSkippedCount());
        log.info("║  Success Rate: {:.2f}%                                         ║", executionLog.getSuccessRate());
        log.info("╚════════════════════════════════════════════════════════════════╝");

        if (executionLog.hasErrors()) {
            log.warn("Batch completed with {} errors. Check CtrlEnlaceLog for details.", executionLog.getErrorCount());
        }
    }
}

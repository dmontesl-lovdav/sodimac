package com.invoicesync.application.usecase;

import com.invoicesync.domain.enums.InvoiceFlowStatus;
import com.invoicesync.domain.model.BatchExecutionLog;
import com.invoicesync.domain.model.ControlCifras;
import com.invoicesync.domain.model.FbcInvoice;
import com.invoicesync.domain.model.StatusTransitionResult;
import com.invoicesync.domain.port.input.InvoiceStatusBatchUseCase;
import com.invoicesync.domain.port.output.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class InvoiceStatusBatchService implements InvoiceStatusBatchUseCase {

    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusBatchService.class);
    private static final int MAX_EXECUTION_MINUTES = 15;

    private final FbcPortalClient fbcPortalClient;
    private final SodimacSapRepository sodimacSapRepository;
    private final SapitoRepository sapitoRepository;
    private final I213Repository i213Repository;
    private final ControlRepository controlRepository;
    private final AlertNotificationService alertService;

    private final AtomicBoolean executionInProgress = new AtomicBoolean(false);

    public InvoiceStatusBatchService(
            FbcPortalClient fbcPortalClient,
            SodimacSapRepository sodimacSapRepository,
            SapitoRepository sapitoRepository,
            I213Repository i213Repository,
            ControlRepository controlRepository,
            AlertNotificationService alertService) {
        this.fbcPortalClient = fbcPortalClient;
        this.sodimacSapRepository = sodimacSapRepository;
        this.sapitoRepository = sapitoRepository;
        this.i213Repository = i213Repository;
        this.controlRepository = controlRepository;
        this.alertService = alertService;
    }

    @Override
    public BatchExecutionLog executeStatusSync() {
        if (!executionInProgress.compareAndSet(false, true)) {
            log.warn("Batch execution already in progress. Skipping.");
            throw new IllegalStateException("Batch execution already in progress");
        }

        log.info("=== Starting Invoice Status Batch Synchronization ===");

        ControlCifras cifrasBefore = controlRepository.captureCurrentCifras();
        BatchExecutionLog executionLog = new BatchExecutionLog(cifrasBefore);

        try {
            log.info("Control cifras BEFORE: {}", cifrasBefore.getSummary());

            if (!fbcPortalClient.isServiceAvailable()) {
                throw new RuntimeException("FBC Portal service is not available");
            }

            processInvoicesByStatus(executionLog, InvoiceFlowStatus.PENDING_SAPITO_REGISTRATION);
            processInvoicesByStatus(executionLog, InvoiceFlowStatus.PENDING_I213_SEND);
            processInvoicesByStatus(executionLog, InvoiceFlowStatus.SENT_TO_I213);
            processInvoicesByStatus(executionLog, InvoiceFlowStatus.PENDING_SAP_ACCOUNTING);
            processInvoicesByStatus(executionLog, InvoiceFlowStatus.PENDING_PAYMENT);

            ControlCifras cifrasAfter = controlRepository.captureCurrentCifras();
            executionLog.complete(cifrasAfter);

            log.info("Control cifras AFTER: {}", cifrasAfter.getSummary());

            controlRepository.saveExecutionLog(executionLog);
            controlRepository.saveControlCifras(cifrasBefore, executionLog.getExecutionId().toString(), "BEFORE");
            controlRepository.saveControlCifras(cifrasAfter, executionLog.getExecutionId().toString(), "AFTER");

            if (executionLog.hasErrors()) {
                alertService.sendExecutionCompletedAlert(executionLog);
            }

            log.info("=== Batch completed: {} processed, {} success, {} errors, {} skipped ===",
                    executionLog.getTotalInvoicesProcessed(),
                    executionLog.getSuccessCount(),
                    executionLog.getErrorCount(),
                    executionLog.getSkippedCount());

            return executionLog;

        } catch (Exception e) {
            log.error("Batch execution failed: {}", e.getMessage(), e);
            executionLog.markAsFailed(e.getMessage());
            controlRepository.saveExecutionLog(executionLog);
            alertService.sendExecutionFailedAlert(executionLog, e.getMessage());
            throw new RuntimeException("Batch execution failed", e);
        } finally {
            executionInProgress.set(false);
        }
    }

    @Override
    public BatchExecutionLog executeStatusSyncWithRetry(int maxRetries, int retryIntervalMinutes) {
        BatchExecutionLog lastLog = null;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                log.info("Attempt {}/{} to execute batch", attempt, maxRetries + 1);
                lastLog = executeStatusSync();

                if (!lastLog.hasErrors() || attempt == maxRetries + 1) {
                    return lastLog;
                }

            } catch (Exception e) {
                lastException = e;
                log.error("Attempt {} failed: {}", attempt, e.getMessage());

                if (attempt <= maxRetries) {
                    alertService.sendRetryNotification(attempt, maxRetries + 1, e.getMessage());
                    try {
                        log.info("Waiting {} minutes before retry...", retryIntervalMinutes);
                        Thread.sleep(retryIntervalMinutes * 60 * 1000L);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        if (lastLog != null) {
            return lastLog;
        }
        throw new RuntimeException("All retry attempts failed", lastException);
    }

    @Override
    public void cancelExecution() {
        executionInProgress.set(false);
        log.info("Batch execution cancelled");
    }

    @Override
    public boolean isExecutionInProgress() {
        return executionInProgress.get();
    }

    private void processInvoicesByStatus(BatchExecutionLog executionLog, InvoiceFlowStatus status) {
        if (!executionLog.isWithinTimeLimit(MAX_EXECUTION_MINUTES)) {
            log.warn("Execution time limit exceeded. Stopping processing.");
            return;
        }

        log.info("Processing invoices with status: {} ({})", status.getCode(), status.getDescription());

        List<FbcInvoice> invoices = fbcPortalClient.fetchInvoicesByStatus(status);
        log.info("Found {} invoices with status {}", invoices.size(), status.getCode());

        for (FbcInvoice invoice : invoices) {
            if (!executionLog.isWithinTimeLimit(MAX_EXECUTION_MINUTES)) {
                log.warn("Execution time limit exceeded. Stopping processing.");
                break;
            }

            StatusTransitionResult result = processInvoice(invoice);
            executionLog.addResult(result);
            controlRepository.saveTransitionResult(result, executionLog.getExecutionId().toString());

            if (!result.success()) {
                controlRepository.saveErrorLog(
                        executionLog.getExecutionId().toString(),
                        invoice.getIdProveedor(),
                        invoice.getDocumentNumber(),
                        result.message()
                );
            }
        }
    }

    private StatusTransitionResult processInvoice(FbcInvoice invoice) {
        try {
            InvoiceFlowStatus newStatus = determineNewStatus(invoice);

            if (newStatus == invoice.getCurrentStatus()) {
                return StatusTransitionResult.noChange(invoice);
            }

            // Si el nuevo estado debe sincronizarse con FBC, usar el endpoint PUT
            if (shouldSyncStatusWithFbc(newStatus)) {
                return updateInvoiceWithFbcSync(invoice, newStatus);
            } else {
                // Para estados internos (6, 7, 8, 16), usar el método legacy
                boolean updated = fbcPortalClient.updateInvoiceStatus(
                        invoice.getIdProveedor(),
                        invoice.getDocumentNumber(),
                        newStatus
                );

                if (updated) {
                    log.debug("Invoice {} status changed from {} to {}",
                            invoice.getDocumentNumber(),
                            invoice.getCurrentStatus().getCode(),
                            newStatus.getCode());
                    return StatusTransitionResult.success(invoice, newStatus);
                } else {
                    return StatusTransitionResult.failure(invoice, "Failed to update status in FBC portal");
                }
            }

        } catch (Exception e) {
            log.error("Error processing invoice {}: {}", invoice.getDocumentNumber(), e.getMessage());
            return StatusTransitionResult.failure(invoice, e.getMessage());
        }
    }

    /**
     * Actualiza el estado de una factura en FBC usando el endpoint PUT /invoices/{uuid}/status.
     * Este método se usa para los estados finales que deben sincronizarse con FBC:
     * - PENDING_SAP_ACCOUNTING (9 → 3)
     * - PENDING_PAYMENT (10 → 7)
     * - PAID (11 → 8)
     * - ACCOUNTING_REJECTED (13 → 11)
     */
    private StatusTransitionResult updateInvoiceWithFbcSync(FbcInvoice invoice, InvoiceFlowStatus newStatus) {
        try {
            // Obtener el mapeo de estados internos a FBC
            int currentFbcStatus = getFbcStatusCode(invoice.getCurrentStatus());
            int newFbcStatus = getFbcStatusCode(newStatus);

            // Convertir idProveedor de String a int
            int numeroProveedor = parseProveedorId(invoice.getIdProveedor());

            log.info("Syncing invoice {} with FBC: internal status {} → {} | FBC status {} → {}",
                    invoice.getUuid(),
                    invoice.getCurrentStatus().getCode(), newStatus.getCode(),
                    currentFbcStatus, newFbcStatus);

            Optional<String> result = fbcPortalClient.updateInvoiceStatusByUuid(
                    invoice.getUuid(),
                    numeroProveedor,
                    currentFbcStatus,
                    newFbcStatus
            );

            if (result.isPresent()) {
                log.info("Invoice {} synced successfully with FBC: {}",
                        invoice.getUuid(), result.get());
                return StatusTransitionResult.success(invoice, newStatus);
            } else {
                log.warn("Failed to sync invoice {} with FBC", invoice.getUuid());
                return StatusTransitionResult.failure(invoice, "Failed to sync status with FBC portal");
            }

        } catch (Exception e) {
            log.error("Error syncing invoice {} with FBC: {}", invoice.getUuid(), e.getMessage(), e);
            return StatusTransitionResult.failure(invoice, "Error syncing with FBC: " + e.getMessage());
        }
    }

    /**
     * Determina si un estado interno debe sincronizarse con FBC.
     * Solo los estados finales (9, 10, 11, 13) se sincronizan.
     * Los estados intermedios (6, 7, 8, 16) son internos de SAPITO/i213.
     */
    private boolean shouldSyncStatusWithFbc(InvoiceFlowStatus status) {
        return status == InvoiceFlowStatus.PENDING_SAP_ACCOUNTING ||
               status == InvoiceFlowStatus.PENDING_PAYMENT ||
               status == InvoiceFlowStatus.PAID ||
               status == InvoiceFlowStatus.ACCOUNTING_REJECTED;
    }

    /**
     * Obtiene el código de estado FBC correspondiente a un estado interno.
     * Según HU STM-1309:
     * - PENDING_SAP_ACCOUNTING (9) → FBC 3 (Pendiente de Contabilizar)
     * - PENDING_PAYMENT (10) → FBC 7 (Pendiente de Pago)
     * - PAID (11) → FBC 8 (Pagado)
     * - ACCOUNTING_REJECTED (13) → FBC 11 (Rechazo Contable)
     */
    private int getFbcStatusCode(InvoiceFlowStatus status) {
        return switch (status) {
            case PENDING_SAP_ACCOUNTING -> 3;
            case PENDING_PAYMENT -> 7;
            case PAID -> 8;
            case ACCOUNTING_REJECTED -> 11;
            case PENDING_SAPITO_REGISTRATION -> 3;  // Estado inicial también es "Pendiente de Contabilizar"
            case PENDING_I213_SEND -> 3;
            case SENT_TO_I213 -> 3;
            case NOT_SENT_TO_I213 -> 11;  // Error de interfaz se mapea a rechazo
        };
    }

    /**
     * Convierte el ID del proveedor de String a int.
     * Si no se puede parsear, retorna 0 como valor por defecto.
     */
    private int parseProveedorId(String idProveedor) {
        try {
            return Integer.parseInt(idProveedor);
        } catch (NumberFormatException e) {
            log.warn("Invalid provider ID format: {}. Using default value 0", idProveedor);
            return 0;
        }
    }

    private InvoiceFlowStatus determineNewStatus(FbcInvoice invoice) {
        return switch (invoice.getCurrentStatus()) {
            case PENDING_SAPITO_REGISTRATION -> processStatus6(invoice);
            case PENDING_I213_SEND -> processStatus7(invoice);
            case SENT_TO_I213 -> processStatus8(invoice);
            case PENDING_SAP_ACCOUNTING -> processStatus9(invoice);
            case PENDING_PAYMENT -> processStatus10(invoice);
            default -> invoice.getCurrentStatus();
        };
    }

    private InvoiceFlowStatus processStatus6(FbcInvoice invoice) {
        boolean exists = sodimacSapRepository.existsInEnviosAp(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber(),
                invoice.getUuid()
        );
        return exists ? InvoiceFlowStatus.PENDING_I213_SEND : invoice.getCurrentStatus();
    }

    private InvoiceFlowStatus processStatus7(FbcInvoice invoice) {
        boolean pending = sapitoRepository.isPendingToSendToI213(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber(),
                invoice.getUuid()
        );
        return pending ? InvoiceFlowStatus.SENT_TO_I213 : invoice.getCurrentStatus();
    }

    private InvoiceFlowStatus processStatus8(FbcInvoice invoice) {
        int flagEnviado = sapitoRepository.getFlagEnviado(
                invoice.getIdProveedor(),
                invoice.getUuid()
        );

        return switch (flagEnviado) {
            case 1 -> InvoiceFlowStatus.PENDING_SAP_ACCOUNTING;
            case 2 -> InvoiceFlowStatus.NOT_SENT_TO_I213;
            default -> invoice.getCurrentStatus();
        };
    }

    private InvoiceFlowStatus processStatus9(FbcInvoice invoice) {
        int result = i213Repository.validateDocumentoAP(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber()
        );

        return switch (result) {
            case 1 -> InvoiceFlowStatus.PENDING_PAYMENT;
            case 0 -> InvoiceFlowStatus.ACCOUNTING_REJECTED;
            default -> invoice.getCurrentStatus();
        };
    }

    private InvoiceFlowStatus processStatus10(FbcInvoice invoice) {
        int result = i213Repository.validateDocumentoPagado(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber()
        );
        return result == 1 ? InvoiceFlowStatus.PAID : invoice.getCurrentStatus();
    }
}

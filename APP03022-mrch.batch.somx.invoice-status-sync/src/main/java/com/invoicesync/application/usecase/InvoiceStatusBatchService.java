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
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InvoiceStatusBatchService implements InvoiceStatusBatchUseCase {

    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusBatchService.class);
    private static final int MAX_EXECUTION_MINUTES = 15;
    // id_proceso en adminCatalogo (catálogo 1, elemento 2) = "Sincronizacion de Estado de Facturas"
    private static final int PROCESS_ID = 2;

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

        // Trazabilidad: alta de la ejecución en ctrlProcesoCab (int id_ejecucion).
        int idEjecucion = controlRepository.startExecution(PROCESS_ID);
        AtomicInteger secuencia = new AtomicInteger(0);

        try {
            controlRepository.saveLog(idEjecucion, "Invoice Status Sync",
                    "Inicio del proceso de sincronización de estatus", "INFO", "INICIO");
            controlRepository.saveCifras(idEjecucion, cifrasBefore, "BEFORE");
            log.info("Control cifras BEFORE: {}", cifrasBefore.getSummary());

            if (!fbcPortalClient.isServiceAvailable()) {
                throw new RuntimeException("FBC Portal service is not available");
            }

            // Tren v1.0: estatus que avanza este batch (SAPITO → i213 → pago).
            int paso = 0;
            processInvoicesByStatus(executionLog, idEjecucion, ++paso, secuencia, InvoiceFlowStatus.PENDIENTE_REGISTRO_SAPITO);
            processInvoicesByStatus(executionLog, idEjecucion, ++paso, secuencia, InvoiceFlowStatus.PENDIENTE_ENVIO_I213);
            processInvoicesByStatus(executionLog, idEjecucion, ++paso, secuencia, InvoiceFlowStatus.FACTURA_ENVIADA_I213);
            processInvoicesByStatus(executionLog, idEjecucion, ++paso, secuencia, InvoiceFlowStatus.PENDIENTE_CONTABILIZAR);
            processInvoicesByStatus(executionLog, idEjecucion, ++paso, secuencia, InvoiceFlowStatus.PENDIENTE_PAGO);

            ControlCifras cifrasAfter = controlRepository.captureCurrentCifras();
            executionLog.complete(cifrasAfter);
            controlRepository.saveCifras(idEjecucion, cifrasAfter, "AFTER");
            log.info("Control cifras AFTER: {}", cifrasAfter.getSummary());

            controlRepository.finishExecution(idEjecucion, executionLog);
            controlRepository.saveLog(idEjecucion, "Invoice Status Sync",
                    String.format("Proceso completado: %d procesadas, %d ok, %d errores, %d sin cambio",
                            executionLog.getTotalInvoicesProcessed(), executionLog.getSuccessCount(),
                            executionLog.getErrorCount(), executionLog.getSkippedCount()),
                    "INFO", "FINALIZACION");

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
            controlRepository.saveLog(idEjecucion, "Invoice Status Sync",
                    "Error fatal: " + e.getMessage(), "ERROR", "ERROR");
            controlRepository.finishExecution(idEjecucion, executionLog);
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

    private void processInvoicesByStatus(BatchExecutionLog executionLog, int idEjecucion, int paso,
                                         AtomicInteger secuencia, InvoiceFlowStatus status) {
        if (!executionLog.isWithinTimeLimit(MAX_EXECUTION_MINUTES)) {
            log.warn("Execution time limit exceeded. Stopping processing.");
            return;
        }

        log.info("Processing invoices with status: {} ({})", status.getCode(), status.getDescription());

        List<FbcInvoice> invoices = fbcPortalClient.fetchInvoicesByStatus(status);
        log.info("Found {} invoices with status {}", invoices.size(), status.getCode());
        controlRepository.saveStep(idEjecucion,
                "Procesar estatus " + status.getCode() + " (" + status.getDescription() + ")",
                paso, invoices.size(), "IN_PROGRESS");

        for (FbcInvoice invoice : invoices) {
            if (!executionLog.isWithinTimeLimit(MAX_EXECUTION_MINUTES)) {
                log.warn("Execution time limit exceeded. Stopping processing.");
                break;
            }

            StatusTransitionResult result = processInvoice(invoice);
            executionLog.addResult(result);
            controlRepository.saveTransitionResult(idEjecucion, result, secuencia.incrementAndGet());

            if (!result.success()) {
                controlRepository.saveLog(idEjecucion, "Invoice " + invoice.getDocumentNumber(),
                        "Error proveedor=" + invoice.getIdProveedor() + " doc=" + invoice.getDocumentNumber()
                                + ": " + result.message(),
                        "ERROR", "ERROR");
            }
        }
    }

    private StatusTransitionResult processInvoice(FbcInvoice invoice) {
        try {
            InvoiceFlowStatus newStatus = determineNewStatus(invoice);

            if (newStatus == invoice.getCurrentStatus()) {
                return StatusTransitionResult.noChange(invoice);
            }

            // Tren v1.0: el batch empuja directamente el código del tren a fiscal-api
            // (PUT /invoices/{uuid}/status). fiscal-api valida la transición contra
            // status_train. Sin remapeo interno→FBC (eliminado getFbcStatusCode).
            int numeroProveedor = parseProveedorId(invoice.getIdProveedor());

            log.info("Sincronizando factura {} con FBC: estatus {} → {} (Proveedor: {})",
                    invoice.getUuid(),
                    invoice.getCurrentStatus().getCode(), newStatus.getCode(), numeroProveedor);

            Optional<String> result = fbcPortalClient.updateInvoiceStatusByUuid(
                    invoice.getUuid(),
                    numeroProveedor,
                    invoice.getCurrentStatus().getCode(),
                    newStatus.getCode());

            if (result.isPresent()) {
                log.debug("Factura {} sincronizada con FBC: {}", invoice.getUuid(), result.get());
                return StatusTransitionResult.success(invoice, newStatus);
            } else {
                log.warn("No se pudo sincronizar factura {} con FBC", invoice.getUuid());
                return StatusTransitionResult.failure(invoice, "Failed to sync status with FBC portal");
            }

        } catch (Exception e) {
            log.error("Error processing invoice {}: {}", invoice.getDocumentNumber(), e.getMessage());
            return StatusTransitionResult.failure(invoice, e.getMessage());
        }
    }

    /**
     * Determina el siguiente estatus según el escenario del tren v1.0.
     */
    private InvoiceFlowStatus determineNewStatus(FbcInvoice invoice) {
        return switch (invoice.getCurrentStatus()) {
            case PENDIENTE_REGISTRO_SAPITO -> processRegistroSapito(invoice);
            case PENDIENTE_ENVIO_I213 -> processEnvioI213(invoice);
            case FACTURA_ENVIADA_I213 -> processEnviadaI213(invoice);
            case PENDIENTE_CONTABILIZAR -> processContabilizar(invoice);
            case PENDIENTE_PAGO -> processPago(invoice);
            default -> invoice.getCurrentStatus();
        };
    }

    // Estatus 7 → 8 : registrada en SODIMAC_SAP (Envios_Ap) → pendiente envío a i213
    private InvoiceFlowStatus processRegistroSapito(FbcInvoice invoice) {
        boolean exists = sodimacSapRepository.existsInEnviosAp(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber(),
                invoice.getUuid()
        );
        return exists ? InvoiceFlowStatus.PENDIENTE_ENVIO_I213 : invoice.getCurrentStatus();
    }

    // Estatus 8 → 9 : pendiente de enviar de SAPITO a i213 (FLAG_ENVIADO=0)
    private InvoiceFlowStatus processEnvioI213(FbcInvoice invoice) {
        boolean pending = sapitoRepository.isPendingToSendToI213(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber(),
                invoice.getUuid()
        );
        return pending ? InvoiceFlowStatus.FACTURA_ENVIADA_I213 : invoice.getCurrentStatus();
    }

    // Estatus 9 → 10 (enviada) / 17 (error envío) : FLAG_ENVIADO 1=enviada, 2=no enviada
    private InvoiceFlowStatus processEnviadaI213(FbcInvoice invoice) {
        int flagEnviado = sapitoRepository.getFlagEnviado(
                invoice.getIdProveedor(),
                invoice.getUuid()
        );

        return switch (flagEnviado) {
            case 1 -> InvoiceFlowStatus.PENDIENTE_CONTABILIZAR;
            case 2 -> InvoiceFlowStatus.ERROR_ENVIO_I213;
            default -> invoice.getCurrentStatus();
        };
    }

    // Estatus 10 → 11 (pendiente pago) / 14 (rechazo contable) : SP i123_Valida_Documento_AP
    private InvoiceFlowStatus processContabilizar(FbcInvoice invoice) {
        int result = i213Repository.validateDocumentoAP(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber()
        );

        return switch (result) {
            case 1 -> InvoiceFlowStatus.PENDIENTE_PAGO;
            case 0 -> InvoiceFlowStatus.RECHAZO_CONTABLE;
            default -> invoice.getCurrentStatus();
        };
    }

    // Estatus 11 → 12 (pendiente complemento) : SP i213_Valida_Documento_Pagado_AP
    private InvoiceFlowStatus processPago(FbcInvoice invoice) {
        int result = i213Repository.validateDocumentoPagado(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber()
        );
        return result == 1 ? InvoiceFlowStatus.PENDIENTE_COMPLEMENTO : invoice.getCurrentStatus();
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
}

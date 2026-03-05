package com.sodimac.batch.process.service.impl;

import com.sodimac.batch.process.client.FiscalApiClient;
import com.sodimac.batch.process.model.dto.InvoiceSearchResponseDto;
import com.sodimac.batch.process.model.dto.StatusUpdateResponseDto;
import com.sodimac.batch.process.model.entity.batch.CtrlProcesoCabEntity;
import com.sodimac.batch.process.service.BatchTraceService;
import com.sodimac.batch.process.service.CfdiDesgloseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class CreditNoteDownloadBatchService {

    private static final Logger log = LoggerFactory.getLogger(CreditNoteDownloadBatchService.class);

    private final FiscalApiClient fiscalApiClient;
    private final CfdiDesgloseService cfdiDesgloseService;
    private final BatchTraceService traceService;

    @Value("${batch.search.months-back:6}")
    private int monthsBack;

    private static final int STATUS_PENDIENTE_CONTABILIZAR = 3;
    private static final int STATUS_PROCESO_DESCARGA = 4;
    private static final int STATUS_DESGLOSE_NC = 5;
    private static final int STATUS_ERROR_NC = 11;
    private static final int STATUS_PENDIENTE_ADDENDA = 1;

    private static final String PROCESS_NAME = "CreditNote Download";
    private static final String DOC_TYPE = "E";

    public CreditNoteDownloadBatchService(FiscalApiClient fiscalApiClient,
                                           CfdiDesgloseService cfdiDesgloseService,
                                           BatchTraceService traceService) {
        this.fiscalApiClient = fiscalApiClient;
        this.cfdiDesgloseService = cfdiDesgloseService;
        this.traceService = traceService;
    }

    public void ejecutar() {
        log.info("=== Inicio proceso batch: Descarga de Notas de Credito ===");

        CtrlProcesoCabEntity ejecucion = traceService.startExecution(
                BatchTraceService.PROCESS_CREDITNOTE_DOWNLOAD);
        int idEjecucion = ejecucion.getIdEjecucion();

        traceService.logInfo(idEjecucion, PROCESS_NAME,
                "Inicio del proceso de descarga de notas de credito", "INICIO");

        int totalOrigen = 0;
        int totalDestino = 0;
        int totalErrores = 0;
        int secuencia = 0;

        try {
            LocalDate dateTo = LocalDate.now();
            LocalDate dateFrom = dateTo.minusMonths(monthsBack);

            traceService.addStep(idEjecucion, "Buscar NC estatus 3", ++secuencia,
                    "dateFrom=" + dateFrom + " dateTo=" + dateTo, null, 0, "IN_PROGRESS");

            List<InvoiceSearchResponseDto> notasCredito = fiscalApiClient.searchAllByStatusAndType(
                    STATUS_PENDIENTE_CONTABILIZAR, DOC_TYPE, dateFrom, dateTo);

            totalOrigen = notasCredito.size();
            traceService.logInfo(idEjecucion, PROCESS_NAME,
                    "Encontradas " + totalOrigen + " NC con estatus 3", "EXTRACT");

            if (notasCredito.isEmpty()) {
                log.info("No hay notas de credito pendientes de procesar");
                traceService.finishExecution(idEjecucion, "SUCCESS", 0, 0, null);
                return;
            }

            traceService.addStep(idEjecucion, "Procesar NC", ++secuencia,
                    "total=" + totalOrigen, null, 0, "IN_PROGRESS");

            int docSecuencia = 0;
            for (InvoiceSearchResponseDto nc : notasCredito) {
                docSecuencia++;
                String uuid = nc.getFiscalUuid() != null ? nc.getFiscalUuid().toString() : "unknown";
                BigDecimal numProveedor = nc.getNumeroProveedor();

                try {
                    procesarNotaCredito(nc, idEjecucion, uuid, numProveedor, docSecuencia);
                    totalDestino++;
                } catch (Exception e) {
                    totalErrores++;
                    log.error("Error procesando NC uuid={}: {}", uuid, e.getMessage());
                    traceService.logError(idEjecucion, PROCESS_NAME,
                            "Error en NC " + uuid + ": " + e.getMessage(), "ERROR");
                    traceService.addElement(idEjecucion, uuid, nc.getSeries() + "-" + nc.getFolio(),
                            docSecuencia, "FAILED", e.getMessage());
                }
            }

            String estatus = totalErrores == 0 ? "SUCCESS"
                    : (totalDestino > 0 ? "PARTIAL" : "FAILED");
            String mensaje = totalErrores > 0
                    ? "Completado con " + totalErrores + " errores de " + totalOrigen
                    : null;

            traceService.finishExecution(idEjecucion, estatus, totalOrigen, totalDestino, mensaje);
            traceService.logInfo(idEjecucion, PROCESS_NAME,
                    String.format("Proceso completado: %d/%d exitosos, %d errores",
                            totalDestino, totalOrigen, totalErrores),
                    "FINALIZACION");

        } catch (Exception e) {
            log.error("Error fatal en proceso batch de NC: {}", e.getMessage(), e);
            traceService.logError(idEjecucion, PROCESS_NAME,
                    "Error fatal: " + e.getMessage(), "ERROR");
            traceService.finishExecution(idEjecucion, "FAILED", totalOrigen, totalDestino, e.getMessage());
        }

        log.info("=== Fin proceso batch: NC — origen={} destino={} errores={} ===",
                totalOrigen, totalDestino, totalErrores);
    }

    private void procesarNotaCredito(InvoiceSearchResponseDto nc, int idEjecucion,
                                      String uuid, BigDecimal numProveedor, int secuencia) throws Exception {

        StatusUpdateResponseDto statusResp = fiscalApiClient.updateStatus(
                uuid, numProveedor, STATUS_PENDIENTE_CONTABILIZAR, STATUS_PROCESO_DESCARGA,
                "Proceso batch: inicio de descarga NC");

        if (statusResp == null || !Boolean.TRUE.equals(statusResp.getSuccess())) {
            throw new RuntimeException("No se pudo actualizar estatus 3->4: " +
                    (statusResp != null ? statusResp.getMessage() : "sin respuesta"));
        }

        String xmlContent = nc.getXmlContent();
        if (xmlContent == null || xmlContent.isEmpty()) {
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, STATUS_ERROR_NC, "XML no disponible");
            throw new RuntimeException("XML no disponible para NC " + uuid);
        }

        List<String> erroresAddenda = cfdiDesgloseService.validarAddenda(xmlContent, DOC_TYPE);
        if (!erroresAddenda.isEmpty()) {
            String errorMsg = String.join("; ", erroresAddenda);
            log.warn("NC {} sin addenda valida: {}", uuid, errorMsg);
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, STATUS_PENDIENTE_ADDENDA,
                    "Addenda invalida: " + errorMsg);
            traceService.addElement(idEjecucion, uuid, nc.getSeries() + "-" + nc.getFolio(),
                    secuencia, "REJECTED", "Sin addenda: " + errorMsg);
            return;
        }

        try {
            cfdiDesgloseService.desglosar(xmlContent,
                    nc.getInvoiceUuid() != null ? nc.getInvoiceUuid().toString() : null);
        } catch (Exception e) {
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, STATUS_ERROR_NC,
                    "Error desglose: " + e.getMessage());
            throw new RuntimeException("Error en desglose CFDI NC: " + e.getMessage(), e);
        }

        fiscalApiClient.updateStatus(uuid, numProveedor,
                STATUS_PROCESO_DESCARGA, STATUS_DESGLOSE_NC,
                "Desglose NC completado exitosamente");

        traceService.addElement(idEjecucion, uuid, nc.getSeries() + "-" + nc.getFolio(),
                secuencia, "PROCESSED", null);

        log.debug("NC procesada exitosamente: {}", uuid);
    }
}

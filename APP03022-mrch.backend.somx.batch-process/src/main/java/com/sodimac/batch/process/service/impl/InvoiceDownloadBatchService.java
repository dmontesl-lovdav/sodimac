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
public class InvoiceDownloadBatchService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceDownloadBatchService.class);

    private final FiscalApiClient fiscalApiClient;
    private final CfdiDesgloseService cfdiDesgloseService;
    private final BatchTraceService traceService;

    @Value("${batch.search.months-back:6}")
    private int monthsBack;

    private static final int STATUS_PENDIENTE_CONTABILIZAR = 3;
    private static final int STATUS_PROCESO_DESCARGA = 4;
    private static final int STATUS_DESGLOSE_FACTURA = 5;
    private static final int STATUS_ERROR_DESGLOSE = 14;
    private static final int STATUS_PENDIENTE_ADDENDA = 1;

    private static final String PROCESS_NAME = "Invoice Download";
    private static final String DOC_TYPE = "I";

    public InvoiceDownloadBatchService(FiscalApiClient fiscalApiClient,
                                        CfdiDesgloseService cfdiDesgloseService,
                                        BatchTraceService traceService) {
        this.fiscalApiClient = fiscalApiClient;
        this.cfdiDesgloseService = cfdiDesgloseService;
        this.traceService = traceService;
    }

    public void ejecutar() {
        log.info("=== Inicio proceso batch: Descarga de Facturas ===");

        CtrlProcesoCabEntity ejecucion = traceService.startExecution(
                BatchTraceService.PROCESS_INVOICE_DOWNLOAD);
        int idEjecucion = ejecucion.getIdEjecucion();

        traceService.logInfo(idEjecucion, PROCESS_NAME,
                "Inicio del proceso de descarga de facturas", "INICIO");

        int totalOrigen = 0;
        int totalDestino = 0;
        int totalErrores = 0;
        int secuencia = 0;

        try {
            LocalDate dateTo = LocalDate.now();
            LocalDate dateFrom = dateTo.minusMonths(monthsBack);

            traceService.addStep(idEjecucion, "Buscar facturas estatus 3", ++secuencia,
                    "dateFrom=" + dateFrom + " dateTo=" + dateTo, null, 0, "IN_PROGRESS");

            List<InvoiceSearchResponseDto> facturas = fiscalApiClient.searchAllByStatusAndType(
                    STATUS_PENDIENTE_CONTABILIZAR, DOC_TYPE, dateFrom, dateTo);

            totalOrigen = facturas.size();
            traceService.logInfo(idEjecucion, PROCESS_NAME,
                    "Encontradas " + totalOrigen + " facturas con estatus 3", "EXTRACT");

            if (facturas.isEmpty()) {
                log.info("No hay facturas pendientes de procesar");
                traceService.finishExecution(idEjecucion, "SUCCESS", 0, 0, null);
                return;
            }

            traceService.addStep(idEjecucion, "Procesar facturas", ++secuencia,
                    "total=" + totalOrigen, null, 0, "IN_PROGRESS");

            int docSecuencia = 0;
            for (InvoiceSearchResponseDto factura : facturas) {
                docSecuencia++;
                String uuid = factura.getFiscalUuid() != null ? factura.getFiscalUuid().toString() : "unknown";
                BigDecimal numProveedor = factura.getNumeroProveedor();

                try {
                    procesarFactura(factura, idEjecucion, uuid, numProveedor, docSecuencia);
                    totalDestino++;
                } catch (Exception e) {
                    totalErrores++;
                    log.error("Error procesando factura uuid={}: {}", uuid, e.getMessage());
                    traceService.logError(idEjecucion, PROCESS_NAME,
                            "Error en factura " + uuid + ": " + e.getMessage(), "ERROR");
                    traceService.addElement(idEjecucion, uuid, factura.getSeries() + "-" + factura.getFolio(),
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
            log.error("Error fatal en proceso batch de facturas: {}", e.getMessage(), e);
            traceService.logError(idEjecucion, PROCESS_NAME,
                    "Error fatal: " + e.getMessage(), "ERROR");
            traceService.finishExecution(idEjecucion, "FAILED", totalOrigen, totalDestino, e.getMessage());
        }

        log.info("=== Fin proceso batch: Facturas — origen={} destino={} errores={} ===",
                totalOrigen, totalDestino, totalErrores);
    }

    private void procesarFactura(InvoiceSearchResponseDto factura, int idEjecucion,
                                  String uuid, BigDecimal numProveedor, int secuencia) throws Exception {

        StatusUpdateResponseDto statusResp = fiscalApiClient.updateStatus(
                uuid, numProveedor, STATUS_PENDIENTE_CONTABILIZAR, STATUS_PROCESO_DESCARGA,
                "Proceso batch: inicio de descarga");

        if (statusResp == null || !Boolean.TRUE.equals(statusResp.getSuccess())) {
            throw new RuntimeException("No se pudo actualizar estatus 3->4: " +
                    (statusResp != null ? statusResp.getMessage() : "sin respuesta"));
        }

        String xmlContent = factura.getXmlContent();
        if (xmlContent == null || xmlContent.isEmpty()) {
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, STATUS_ERROR_DESGLOSE, "XML no disponible");
            throw new RuntimeException("XML no disponible para factura " + uuid);
        }

        List<String> erroresAddenda = cfdiDesgloseService.validarAddenda(xmlContent, DOC_TYPE);
        if (!erroresAddenda.isEmpty()) {
            String errorMsg = String.join("; ", erroresAddenda);
            log.warn("Factura {} sin addenda valida: {}", uuid, errorMsg);
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, STATUS_PENDIENTE_ADDENDA,
                    "Addenda invalida: " + errorMsg);
            traceService.addElement(idEjecucion, uuid, factura.getSeries() + "-" + factura.getFolio(),
                    secuencia, "REJECTED", "Sin addenda: " + errorMsg);
            return;
        }

        try {
            cfdiDesgloseService.desglosar(xmlContent,
                    factura.getInvoiceUuid() != null ? factura.getInvoiceUuid().toString() : null);
        } catch (Exception e) {
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, STATUS_ERROR_DESGLOSE,
                    "Error desglose: " + e.getMessage());
            throw new RuntimeException("Error en desglose CFDI: " + e.getMessage(), e);
        }

        fiscalApiClient.updateStatus(uuid, numProveedor,
                STATUS_PROCESO_DESCARGA, STATUS_DESGLOSE_FACTURA,
                "Desglose completado exitosamente");

        traceService.addElement(idEjecucion, uuid, factura.getSeries() + "-" + factura.getFolio(),
                secuencia, "PROCESSED", null);

        log.debug("Factura procesada exitosamente: {}", uuid);
    }
}

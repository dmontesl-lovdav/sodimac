package com.sodimac.batch.fiscal.download.service.impl;

import com.sodimac.batch.fiscal.download.client.FiscalApiClient;
import com.sodimac.batch.fiscal.download.model.dto.InvoiceSearchResponseDto;
import com.sodimac.batch.fiscal.download.model.dto.StatusUpdateResponseDto;
import com.sodimac.batch.fiscal.download.model.entity.batch.CtrlProcesoCabEntity;
import com.sodimac.batch.fiscal.download.service.BatchTraceService;
import com.sodimac.batch.fiscal.download.service.CfdiDesgloseService;
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

    // Tren de Estatus v1.0 (status_train option_id=1). Recalibrado: antes 14=error desglose
    // y 1=pendiente addenda (obsoleto). En v1.0 el error de desglose es 16 (Estructura inválida,
    // 5->16, reintenta 16->5); el estatus 6 (Error desglose) es huérfano en el tren (no usar).
    private static final int STATUS_RECIBIDA = 3;             // entrada del batch
    private static final int STATUS_PROCESO_DESCARGA = 4;
    private static final int STATUS_DESGLOSE_FACTURA = 5;
    private static final int STATUS_ESTRUCTURA_INVALIDA = 16; // error de desglose

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

            traceService.addStep(idEjecucion, "Buscar facturas estatus 3 (Recibida)", ++secuencia,
                    "dateFrom=" + dateFrom + " dateTo=" + dateTo, null, 0, "IN_PROGRESS");

            List<InvoiceSearchResponseDto> facturas = fiscalApiClient.searchAllByStatusAndType(
                    STATUS_RECIBIDA, DOC_TYPE, dateFrom, dateTo);

            totalOrigen = facturas.size();
            traceService.logInfo(idEjecucion, PROCESS_NAME,
                    "Encontradas " + totalOrigen + " facturas en estatus 3 (Recibida)", "EXTRACT");

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

        // 3 Recibida -> 4 En proceso de descarga
        StatusUpdateResponseDto statusResp = fiscalApiClient.updateStatus(
                uuid, numProveedor, STATUS_RECIBIDA, STATUS_PROCESO_DESCARGA,
                "Proceso batch: inicio de descarga");

        if (statusResp == null || !Boolean.TRUE.equals(statusResp.getSuccess())) {
            throw new RuntimeException("No se pudo actualizar estatus 3->4: " +
                    (statusResp != null ? statusResp.getMessage() : "sin respuesta"));
        }

        String xmlContent = factura.getXmlContent();
        if (xmlContent == null || xmlContent.isEmpty()) {
            // XML faltante: se deja en estatus 4 (En proceso de descarga) para reintento.
            // El tren v1.0 no tiene transición de error desde 4; no se fabrica estatus.
            traceService.addElement(idEjecucion, uuid, factura.getSeries() + "-" + factura.getFolio(),
                    secuencia, "RETRY", "XML no disponible (se reintenta)");
            throw new RuntimeException("XML no disponible para factura " + uuid);
        }

        // v1.0: la addenda se registra junto con la factura (fiscal-api /register).
        // fiscal-download ya NO valida ni bloquea por addenda.

        try {
            cfdiDesgloseService.desglosar(xmlContent,
                    factura.getInvoiceUuid() != null ? factura.getInvoiceUuid().toString() : null);
        } catch (Exception e) {
            // Error de estructura en el desglose. El tren v1.0 sólo permite llegar a 16 desde 5:
            // 4 -> 5 (entró a desglose) -> 16 (Estructura inválida). 16 -> 5 permite reintento.
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, STATUS_DESGLOSE_FACTURA, "Desglose con error de estructura");
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_DESGLOSE_FACTURA, STATUS_ESTRUCTURA_INVALIDA,
                    "Estructura inválida: " + e.getMessage());
            traceService.addElement(idEjecucion, uuid, factura.getSeries() + "-" + factura.getFolio(),
                    secuencia, "REJECTED", "Estructura inválida: " + e.getMessage());
            throw new RuntimeException("Error en desglose CFDI (estructura inválida): " + e.getMessage(), e);
        }

        // 4 -> 5 Desglose de factura
        fiscalApiClient.updateStatus(uuid, numProveedor,
                STATUS_PROCESO_DESCARGA, STATUS_DESGLOSE_FACTURA,
                "Desglose completado exitosamente");

        traceService.addElement(idEjecucion, uuid, factura.getSeries() + "-" + factura.getFolio(),
                secuencia, "PROCESSED", null);

        log.debug("Factura procesada exitosamente: {}", uuid);
    }
}

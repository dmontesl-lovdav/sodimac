package com.sodimac.batch.fiscal.download.service.impl;

import com.sodimac.batch.fiscal.download.client.FiscalApiClient;
import com.sodimac.batch.fiscal.download.model.dto.InvoiceSearchResponseDto;
import com.sodimac.batch.fiscal.download.model.dto.StatusUpdateResponseDto;
import com.sodimac.batch.fiscal.download.model.entity.batch.CtrlProcesoCabEntity;
import com.sodimac.batch.fiscal.download.service.BatchTraceService;
import com.sodimac.batch.fiscal.download.service.CfdiDesgloseService;
import com.sodimac.batch.fiscal.download.service.CfdiEstructuraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceDownloadBatchService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceDownloadBatchService.class);

    private final FiscalApiClient fiscalApiClient;
    private final CfdiDesgloseService cfdiDesgloseService;
    private final BatchTraceService traceService;

    @Value("${batch.search.months-back:6}")
    private int monthsBack;

    // STM-719 (actualización 26/ago/2026): el batch toma como base estatus 3 Y 4, y el
    // error de desglose va a 6 "Error en el desglose XML" (tren definitivo; antes 16 del
    // tren v1.0 vía 5->16). El 4->6 ya está cargado en el tren del portal.
    private static final int STATUS_RECIBIDA = 3;             // entrada del batch
    private static final int STATUS_PROCESO_DESCARGA = 4;     // también entrada (reanudación)
    private static final int STATUS_DESGLOSE_FACTURA = 5;

    @Value("${batch.status.error-desglose-factura:6}")
    private int statusErrorDesglose;

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

            traceService.addStep(idEjecucion, "Buscar facturas estatus 3 y 4", ++secuencia,
                    "dateFrom=" + dateFrom + " dateTo=" + dateTo, null, 0, "IN_PROGRESS");

            List<InvoiceSearchResponseDto> facturas = new ArrayList<>();
            facturas.addAll(fiscalApiClient.searchAllByStatusAndType(
                    STATUS_RECIBIDA, DOC_TYPE, dateFrom, dateTo));
            facturas.addAll(fiscalApiClient.searchAllByStatusAndType(
                    STATUS_PROCESO_DESCARGA, DOC_TYPE, dateFrom, dateTo));

            totalOrigen = facturas.size();
            traceService.logInfo(idEjecucion, PROCESS_NAME,
                    "Encontradas " + totalOrigen + " facturas en estatus 3 y 4", "EXTRACT");

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

    /**
     * Orden resiliente: primero el trabajo local (desglose transaccional e idempotente
     * por Uuid), y el estatus en el portal se avanza SOLO tras confirmar el commit.
     * - Falla transitoria (BD SAP caída, etc.): el documento permanece en su estatus de
     *   entrada y la siguiente corrida lo reintenta sola; no hay nada que compensar.
     * - Falla de estructura (permanente): sí se marca en el tren (3->)4->6.
     * - Desglose OK pero falla la confirmación de estatus: permanece en 3 o 4; la siguiente
     *   corrida detecta el comprobante ya insertado (dedup), omite el desglose y solo
     *   reintenta la confirmación (entrada en 4).
     */
    private void procesarFactura(InvoiceSearchResponseDto factura, int idEjecucion,
                                  String uuid, BigDecimal numProveedor, int secuencia) throws Exception {

        String serieFolio = factura.getSeries() + "-" + factura.getFolio();
        int estatusEntrada = factura.getStatus() != null ? factura.getStatus() : STATUS_RECIBIDA;

        String xmlContent = factura.getXmlContent();
        if (xmlContent == null || xmlContent.isEmpty()) {
            // Sin XML no se inicia nada: permanece en su estatus y se reintenta en la siguiente corrida.
            traceService.addElement(idEjecucion, uuid, serieFolio,
                    secuencia, "RETRY", "XML no disponible (permanece en " + estatusEntrada + ", se reintenta)");
            throw new RuntimeException("XML no disponible para factura " + uuid);
        }

        // Addenda: si el XML no la trae válida, se genera desde los datos del portal (mismo
        // criterio que NC, Ivan 2026-08). No bloquea ni cambia estatus.
        List<String> erroresAddenda = cfdiDesgloseService.validarAddenda(xmlContent, DOC_TYPE);
        boolean addendaDesdePortal = !erroresAddenda.isEmpty();
        if (addendaDesdePortal) {
            log.info("Factura {} sin addenda valida en XML ({}); se genera desde datos del portal",
                    uuid, String.join("; ", erroresAddenda));
        }

        // 1) Trabajo local primero.
        try {
            cfdiDesgloseService.desglosar(xmlContent,
                    factura.getInvoiceUuid() != null ? factura.getInvoiceUuid().toString() : null);
        } catch (CfdiEstructuraException e) {
            // Permanente: reprocesar no lo arregla. Se marca en el tren: (3->)4->6.
            if (estatusEntrada == STATUS_RECIBIDA) {
                fiscalApiClient.updateStatus(uuid, numProveedor,
                        STATUS_RECIBIDA, STATUS_PROCESO_DESCARGA, "Desglose con error de estructura");
            }
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, statusErrorDesglose,
                    "Estructura inválida: " + e.getMessage());
            traceService.addElement(idEjecucion, uuid, serieFolio,
                    secuencia, "REJECTED", "Estructura inválida: " + e.getMessage());
            throw new RuntimeException("Error en desglose CFDI (estructura inválida): " + e.getMessage(), e);
        } catch (Exception e) {
            // Transitoria: no se toca el estatus, permanece en su entrada para reintento automático.
            traceService.addElement(idEjecucion, uuid, serieFolio,
                    secuencia, "RETRY", "Error transitorio en desglose (permanece en "
                            + estatusEntrada + "): " + e.getMessage());
            throw new RuntimeException("Error transitorio en desglose (permanece en "
                    + estatusEntrada + "): " + e.getMessage(), e);
        }

        // 1b) Addenda generada desde el portal cuando el XML no la trae (parte del trabajo local).
        if (addendaDesdePortal) {
            cfdiDesgloseService.guardarAddendaFacturaDesdePortal(uuid,
                    numProveedor != null ? numProveedor.toBigInteger().toString() : null,
                    factura.getNoOrdenCompra(), factura.getNoRecepcion(),
                    (factura.getSeries() != null ? factura.getSeries() : "")
                            + (factura.getFolio() != null ? factura.getFolio() : ""),
                    factura.getReceptorRfc(), factura.getGuiaEntrega());
        }

        // 2) Confirmación en el portal, sólo tras commit local: (3 ->) 4 -> 5.
        if (estatusEntrada == STATUS_RECIBIDA) {
            avanzarEstatus(uuid, numProveedor, STATUS_RECIBIDA, STATUS_PROCESO_DESCARGA,
                    "Proceso batch: descarga y desglose confirmados");
        }
        avanzarEstatus(uuid, numProveedor, STATUS_PROCESO_DESCARGA, STATUS_DESGLOSE_FACTURA,
                "Desglose completado exitosamente");

        traceService.addElement(idEjecucion, uuid, serieFolio,
                secuencia, "PROCESSED", null);

        log.debug("Factura procesada exitosamente: {}", uuid);
    }

    private void avanzarEstatus(String uuid, BigDecimal numProveedor,
                                 int origen, int destino, String comentario) {
        StatusUpdateResponseDto resp = fiscalApiClient.updateStatus(
                uuid, numProveedor, origen, destino, comentario);
        if (resp == null || !Boolean.TRUE.equals(resp.getSuccess())) {
            // Desglose ya confirmado en SAP; el documento queda en el estatus previo y la
            // siguiente corrida reintenta sólo esta confirmación (el desglose se omite por dedup).
            throw new RuntimeException("Desglose OK pero fallo la confirmacion de estatus " +
                    origen + "->" + destino + ": " +
                    (resp != null ? resp.getMessage() : "sin respuesta"));
        }
    }
}

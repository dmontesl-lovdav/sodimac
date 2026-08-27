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
public class CreditNoteDownloadBatchService {

    private static final Logger log = LoggerFactory.getLogger(CreditNoteDownloadBatchService.class);

    private final FiscalApiClient fiscalApiClient;
    private final CfdiDesgloseService cfdiDesgloseService;
    private final BatchTraceService traceService;

    @Value("${batch.search.months-back:6}")
    private int monthsBack;

    // STM-719 (actualización 26/ago/2026): base estatus 3 Y 4; el error de desglose
    // va a 6 "Error en el desglose XML" igual que facturas (antes 11).
    private static final int STATUS_PENDIENTE_CONTABILIZAR = 3;
    private static final int STATUS_PROCESO_DESCARGA = 4;    // también entrada (reanudación)
    private static final int STATUS_DESGLOSE_NC = 5;

    @Value("${batch.status.error-desglose-nc:6}")
    private int statusErrorDesglose;

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

            traceService.addStep(idEjecucion, "Buscar NC estatus 3 y 4", ++secuencia,
                    "dateFrom=" + dateFrom + " dateTo=" + dateTo, null, 0, "IN_PROGRESS");

            List<InvoiceSearchResponseDto> notasCredito = new ArrayList<>();
            notasCredito.addAll(fiscalApiClient.searchAllByStatusAndType(
                    STATUS_PENDIENTE_CONTABILIZAR, DOC_TYPE, dateFrom, dateTo));
            notasCredito.addAll(fiscalApiClient.searchAllByStatusAndType(
                    STATUS_PROCESO_DESCARGA, DOC_TYPE, dateFrom, dateTo));

            totalOrigen = notasCredito.size();
            traceService.logInfo(idEjecucion, PROCESS_NAME,
                    "Encontradas " + totalOrigen + " NC con estatus 3 y 4", "EXTRACT");

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

    /**
     * Orden resiliente (igual que facturas): trabajo local primero, estatus en el portal
     * sólo tras confirmar. Fallas transitorias dejan la NC en su estatus de entrada para
     * reintento automático; sólo los errores permanentes de estructura avanzan al
     * estatus de error de desglose.
     */
    private void procesarNotaCredito(InvoiceSearchResponseDto nc, int idEjecucion,
                                      String uuid, BigDecimal numProveedor, int secuencia) throws Exception {

        String serieFolio = nc.getSeries() + "-" + nc.getFolio();
        int estatusEntrada = nc.getStatus() != null ? nc.getStatus() : STATUS_PENDIENTE_CONTABILIZAR;

        String xmlContent = nc.getXmlContent();
        if (xmlContent == null || xmlContent.isEmpty()) {
            // Sin XML no se inicia nada: permanece en estatus de entrada y se reintenta.
            traceService.addElement(idEjecucion, uuid, serieFolio,
                    secuencia, "RETRY", "XML no disponible (permanece en " + estatusEntrada + ", se reintenta)");
            throw new RuntimeException("XML no disponible para NC " + uuid);
        }

        // NC Descuento Comercial (TipoNC=2): no aplica descarga; se evalúa ANTES de
        // validar la addenda para no rechazarla por campos que no le competen.
        // Fuente primaria: campo tipoNotaCredito del portal (tabla Addenda de FBC, numérico).
        // Respaldo: nodo TipoNC del XML, que puede venir como texto libre ("Otro") — solo
        // cuenta si es numérico.
        boolean esDescuentoComercial = nc.getTipoNotaCredito() != null && nc.getTipoNotaCredito() == 2;
        if (!esDescuentoComercial && nc.getTipoNotaCredito() == null) {
            String tipoNcXml = cfdiDesgloseService.getTipoNotaCredito(xmlContent);
            esDescuentoComercial = tipoNcXml != null && tipoNcXml.replaceFirst("^0+", "").equals("2");
        }
        if (esDescuentoComercial) {
            // Tren v1.0(5) (Ivan 2026-08): la NC de Descuento Comercial (rebate) nace en 17
            // "Pendiente de complemento" en el registro y NO pasa por este batch (que toma
            // estatus 3/4). Si por dato legado apareciera aquí, se omite SIN cambiar estatus:
            // el viejo 3->9 ya no existe en el tren (el 9 hoy es "Error registro contable").
            log.info("NC {} TipoNC=2 (Descuento Comercial): no se descarga (rebate nace en 17), se omite", uuid);
            traceService.addElement(idEjecucion, uuid, serieFolio,
                    secuencia, "SKIPPED", "TipoNC=2 Descuento Comercial: no se descarga (rebate en 17)");
            return;
        }

        // Iván 26/ago/2026 (STM-719): el rechazo por addenda YA NO APLICA — "sí o sí se
        // registra una Addenda", sin cambio de estatus a 1. Si el XML no la trae válida,
        // se genera desde los datos del portal como parte del trabajo local.
        List<String> erroresAddenda = cfdiDesgloseService.validarAddenda(xmlContent, DOC_TYPE);
        boolean addendaDesdePortal = !erroresAddenda.isEmpty();
        if (addendaDesdePortal) {
            log.info("NC {} sin addenda valida en XML ({}); se genera desde datos del portal (tipoNC={} - {})",
                    uuid, String.join("; ", erroresAddenda),
                    nc.getTipoNotaCredito(), nc.getTipoNotaCreditoDescripcion());
        }

        // 1) Trabajo local primero.
        try {
            cfdiDesgloseService.desglosar(xmlContent,
                    nc.getInvoiceUuid() != null ? nc.getInvoiceUuid().toString() : null);
        } catch (CfdiEstructuraException e) {
            // Permanente: se marca en el tren como error de desglose: (3->)4->6.
            if (estatusEntrada == STATUS_PENDIENTE_CONTABILIZAR) {
                fiscalApiClient.updateStatus(uuid, numProveedor,
                        STATUS_PENDIENTE_CONTABILIZAR, STATUS_PROCESO_DESCARGA,
                        "Proceso batch: NC con error de estructura");
            }
            fiscalApiClient.updateStatus(uuid, numProveedor,
                    STATUS_PROCESO_DESCARGA, statusErrorDesglose,
                    "Error desglose: " + e.getMessage());
            traceService.addElement(idEjecucion, uuid, serieFolio,
                    secuencia, "REJECTED", "Estructura inválida: " + e.getMessage());
            throw new RuntimeException("Error en desglose CFDI NC (estructura inválida): " + e.getMessage(), e);
        } catch (Exception e) {
            // Transitoria: permanece en estatus de entrada para reintento automático.
            traceService.addElement(idEjecucion, uuid, serieFolio,
                    secuencia, "RETRY", "Error transitorio en desglose (permanece en "
                            + estatusEntrada + "): " + e.getMessage());
            throw new RuntimeException("Error transitorio en desglose NC (permanece en "
                    + estatusEntrada + "): " + e.getMessage(), e);
        }

        // 1b) Addenda generada desde el portal cuando el XML no la trae (parte del trabajo local).
        if (addendaDesdePortal) {
            cfdiDesgloseService.guardarAddendaNcDesdePortal(uuid,
                    numProveedor != null ? numProveedor.toBigInteger().toString() : null,
                    nc.getTipoNotaCreditoDescripcion());
        }

        // 2) Confirmación en el portal, sólo tras commit local: (3 ->) 4 -> 5.
        if (estatusEntrada == STATUS_PENDIENTE_CONTABILIZAR) {
            avanzarEstatus(uuid, numProveedor, STATUS_PENDIENTE_CONTABILIZAR, STATUS_PROCESO_DESCARGA,
                    "Proceso batch: descarga y desglose NC confirmados");
        }
        avanzarEstatus(uuid, numProveedor, STATUS_PROCESO_DESCARGA, STATUS_DESGLOSE_NC,
                "Desglose NC completado exitosamente");

        traceService.addElement(idEjecucion, uuid, serieFolio,
                secuencia, "PROCESSED", null);

        log.debug("NC procesada exitosamente: {}", uuid);
    }

    private void avanzarEstatus(String uuid, BigDecimal numProveedor,
                                 int origen, int destino, String comentario) {
        StatusUpdateResponseDto resp = fiscalApiClient.updateStatus(
                uuid, numProveedor, origen, destino, comentario);
        if (resp == null || !Boolean.TRUE.equals(resp.getSuccess())) {
            throw new RuntimeException("Desglose OK pero fallo la confirmacion de estatus " +
                    origen + "->" + destino + ": " +
                    (resp != null ? resp.getMessage() : "sin respuesta"));
        }
    }
}

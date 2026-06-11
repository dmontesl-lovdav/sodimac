package com.invoicesync.infrastructure.adapter.persistence;

import com.invoicesync.domain.model.BatchExecutionLog;
import com.invoicesync.domain.model.ControlCifras;
import com.invoicesync.domain.model.StatusTransitionResult;
import com.invoicesync.domain.port.output.ControlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Trazabilidad sobre el modelo estándar CtrlProcesoCab/Det/Elemento/ctrlLog
 * en SODIMAC_BATCH_DEV (STM-1309). Sustituye al esquema propio CtrlEnlace* previo.
 */
@Repository
public class ControlRepositoryAdapter implements ControlRepository {

    private static final Logger log = LoggerFactory.getLogger(ControlRepositoryAdapter.class);

    private final JdbcTemplate batchJdbcTemplate;

    public ControlRepositoryAdapter(@Qualifier("batchJdbcTemplate") JdbcTemplate batchJdbcTemplate) {
        this.batchJdbcTemplate = batchJdbcTemplate;
    }

    @Override
    public int startExecution(int processId) {
        String sql = """
            INSERT INTO ctrlProcesoCab (id_proceso, fecha_inicio, estatus, registros_origen, registros_destino)
            OUTPUT INSERTED.id_ejecucion
            VALUES (?, GETDATE(), 'IN_PROGRESS', 0, 0)
            """;
        try {
            Integer id = batchJdbcTemplate.queryForObject(sql, Integer.class, processId);
            int idEjecucion = id != null ? id : -1;
            log.info("ctrlProcesoCab iniciado: id_ejecucion={} proceso={}", idEjecucion, processId);
            return idEjecucion;
        } catch (Exception e) {
            log.error("Error iniciando ctrlProcesoCab: {}", e.getMessage());
            return -1;
        }
    }

    @Override
    public void finishExecution(int idEjecucion, BatchExecutionLog executionLog) {
        if (idEjecucion <= 0) return;

        String estatus = executionLog.hasErrors()
                ? (executionLog.getSuccessCount() > 0 ? "PARTIAL" : "FAILED")
                : "SUCCESS";
        String mensajeError = executionLog.getErrorDetail();
        if (mensajeError != null && mensajeError.length() > 1000) {
            mensajeError = mensajeError.substring(0, 1000);
        }

        // duracion_segundos es columna COMPUTED en ctrlProcesoCab (la calcula la BD) — no se actualiza.
        String sql = """
            UPDATE ctrlProcesoCab
               SET fecha_final = GETDATE(),
                   estatus = ?,
                   registros_origen = ?,
                   registros_destino = ?,
                   mensaje_error = ?
             WHERE id_ejecucion = ?
            """;
        try {
            batchJdbcTemplate.update(sql,
                    estatus,
                    executionLog.getTotalInvoicesProcessed(),
                    executionLog.getSuccessCount(),
                    mensajeError,
                    idEjecucion);
            log.info("ctrlProcesoCab finalizado: id={} estatus={}", idEjecucion, estatus);
        } catch (Exception e) {
            log.error("Error finalizando ctrlProcesoCab {}: {}", idEjecucion, e.getMessage());
        }
    }

    @Override
    public void saveStep(int idEjecucion, String nombrePaso, int secuencia, int registrosProcesados, String estatus) {
        if (idEjecucion <= 0) return;
        String sql = """
            INSERT INTO ctrlProcesoDet
                (id_ejecucion, nombre_paso, secuencia, fecha_inicio_registro, fecha_final_registro,
                 registros_procesados, estatus)
            VALUES (?, ?, ?, GETDATE(), GETDATE(), ?, ?)
            """;
        try {
            batchJdbcTemplate.update(sql, idEjecucion, truncate(nombrePaso, 255), secuencia,
                    registrosProcesados, estatus);
        } catch (Exception e) {
            log.error("Error guardando ctrlProcesoDet: {}", e.getMessage());
        }
    }

    @Override
    public void saveTransitionResult(int idEjecucion, StatusTransitionResult result, int secuencia) {
        if (idEjecucion <= 0) return;
        String estatus = result.success() ? "PROCESSED" : "FAILED";
        String detalleError = result.success() ? null : truncate(result.message(), 500);
        String valorAlterno = result.idProveedor() + " - " + result.documentNumber()
                + " (" + result.previousStatus().getCode() + "->" + result.newStatus().getCode() + ")";

        String sql = """
            INSERT INTO ctrlProcesoElemento
                (id_ejecucion, valor, valorAlterno, secuencia, estatus, fechaRegistro, detalle_error)
            VALUES (?, ?, ?, ?, ?, GETDATE(), ?)
            """;
        try {
            batchJdbcTemplate.update(sql, idEjecucion, truncate(result.uuid(), 50),
                    truncate(valorAlterno, 250), secuencia, estatus, detalleError);
        } catch (Exception e) {
            log.error("Error guardando ctrlProcesoElemento: {}", e.getMessage());
        }
    }

    @Override
    public void saveLog(int idEjecucion, String nombre, String mensaje, String nivel, String fase) {
        if (idEjecucion <= 0) return;
        String sql = """
            INSERT INTO ctrlLog (nombre, id_ejecucion, log, nivel, fase, fecha_registro)
            VALUES (?, ?, ?, ?, ?, GETDATE())
            """;
        try {
            batchJdbcTemplate.update(sql, truncate(nombre, 100), idEjecucion, mensaje, nivel, fase);
        } catch (Exception e) {
            log.error("Error guardando ctrlLog: {}", e.getMessage());
        }
    }

    @Override
    public void saveCifras(int idEjecucion, ControlCifras cifras, String phase) {
        saveLog(idEjecucion, "Cifras Control", cifras.getSummary(), "INFO", "CIFRAS_" + phase);
    }

    @Override
    public ControlCifras captureCurrentCifras() {
        // La fuente real de conteo por estatus (servicio FBC / fiscal-api) aún no está
        // cableada en este batch. Se devuelve una fotografía vacía para no romper el flujo.
        // Pendiente: leer cifras reales por estatus desde fiscal-api.
        return ControlCifras.capture(0, Map.of(), 0, 0, 0);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}

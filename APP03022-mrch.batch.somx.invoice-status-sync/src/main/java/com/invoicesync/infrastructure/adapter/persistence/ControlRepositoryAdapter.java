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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ControlRepositoryAdapter implements ControlRepository {

    private static final Logger log = LoggerFactory.getLogger(ControlRepositoryAdapter.class);

    private final JdbcTemplate batchJdbcTemplate;

    public ControlRepositoryAdapter(@Qualifier("batchJdbcTemplate") JdbcTemplate batchJdbcTemplate) {
        this.batchJdbcTemplate = batchJdbcTemplate;
    }

    @Override
    public void saveExecutionLog(BatchExecutionLog executionLog) {
        String sql = """
            INSERT INTO CtrlEnlace (
                execution_id, start_time, end_time, total_processed,
                success_count, error_count, skipped_count, attempt_number, error_detail
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            batchJdbcTemplate.update(sql,
                    executionLog.getExecutionId().toString(),
                    Timestamp.valueOf(executionLog.getStartTime()),
                    executionLog.getEndTime() != null ? Timestamp.valueOf(executionLog.getEndTime()) : null,
                    executionLog.getTotalInvoicesProcessed(),
                    executionLog.getSuccessCount(),
                    executionLog.getErrorCount(),
                    executionLog.getSkippedCount(),
                    executionLog.getAttemptNumber(),
                    executionLog.getErrorDetail()
            );
            log.info("Execution log saved with ID: {}", executionLog.getExecutionId());
        } catch (Exception e) {
            log.error("Error saving execution log: {}", e.getMessage());
        }
    }

    @Override
    public void saveControlCifras(ControlCifras cifras, String executionId, String phase) {
        String sql = """
            INSERT INTO CtrlEnlace_CifrasControl (
                execution_id, phase, captured_at, total_invoices,
                pending_sync, synced, failed, status_breakdown
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            batchJdbcTemplate.update(sql,
                    executionId,
                    phase,
                    Timestamp.valueOf(cifras.capturedAt()),
                    cifras.totalInvoices(),
                    cifras.pendingSync(),
                    cifras.synced(),
                    cifras.failed(),
                    cifras.getSummary()
            );
            log.debug("Control cifras saved for execution {} phase {}", executionId, phase);
        } catch (Exception e) {
            log.error("Error saving control cifras: {}", e.getMessage());
        }
    }

    @Override
    public void saveTransitionResult(StatusTransitionResult result, String executionId) {
        String sql = """
            INSERT INTO CtrlEnlace_Comprobante_ControlDocumento (
                execution_id, id_proveedor, document_number, uuid,
                previous_status, new_status, success, message, processed_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            batchJdbcTemplate.update(sql,
                    executionId,
                    result.idProveedor(),
                    result.documentNumber(),
                    result.uuid(),
                    result.previousStatus().getCode(),
                    result.newStatus().getCode(),
                    result.success(),
                    result.message(),
                    Timestamp.valueOf(result.processedAt())
            );
        } catch (Exception e) {
            log.error("Error saving transition result: {}", e.getMessage());
        }
    }

    @Override
    public void saveErrorLog(String executionId, String idProveedor, String documentNumber, String errorDetail) {
        String sql = """
            INSERT INTO CtrlEnlaceLog (
                execution_id, id_proveedor, document_number, error_detail, logged_at
            ) VALUES (?, ?, ?, ?, GETDATE())
            """;

        try {
            batchJdbcTemplate.update(sql, executionId, idProveedor, documentNumber, errorDetail);
            log.debug("Error log saved for document {}", documentNumber);
        } catch (Exception e) {
            log.error("Error saving error log: {}", e.getMessage());
        }
    }

    @Override
    public void registerProcess(String processName, int catalogId) {
        String sql = """
            INSERT INTO AdminCatalogo (nombre_proceso, catalog_id, created_at)
            SELECT ?, ?, GETDATE()
            WHERE NOT EXISTS (SELECT 1 FROM AdminCatalogo WHERE nombre_proceso = ?)
            """;

        try {
            batchJdbcTemplate.update(sql, processName, catalogId, processName);
            log.info("Process {} registered with catalog ID {}", processName, catalogId);
        } catch (Exception e) {
            log.error("Error registering process: {}", e.getMessage());
        }
    }

    @Override
    public ControlCifras captureCurrentCifras() {
        try {
            Map<Integer, Integer> statusCounts = new HashMap<>();

            String countSql = "SELECT status_code, COUNT(*) as cnt FROM invoices GROUP BY status_code";
            batchJdbcTemplate.query(countSql, rs -> {
                statusCounts.put(rs.getInt("status_code"), rs.getInt("cnt"));
            });

            int total = statusCounts.values().stream().mapToInt(Integer::intValue).sum();
            int pending = statusCounts.getOrDefault(6, 0) +
                          statusCounts.getOrDefault(7, 0) +
                          statusCounts.getOrDefault(8, 0) +
                          statusCounts.getOrDefault(9, 0) +
                          statusCounts.getOrDefault(10, 0);
            int synced = statusCounts.getOrDefault(11, 0);
            int failed = statusCounts.getOrDefault(13, 0) + statusCounts.getOrDefault(16, 0);

            return ControlCifras.capture(total, statusCounts, pending, synced, failed);

        } catch (Exception e) {
            log.error("Error capturing control cifras: {}", e.getMessage());
            return ControlCifras.capture(0, Map.of(), 0, 0, 0);
        }
    }
}

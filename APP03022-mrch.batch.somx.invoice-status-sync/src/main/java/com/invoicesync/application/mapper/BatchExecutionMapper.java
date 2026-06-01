package com.invoicesync.application.mapper;

import com.invoicesync.application.dto.BatchExecutionResponse;
import com.invoicesync.application.dto.ControlCifrasResponse;
import com.invoicesync.application.dto.TransitionResultResponse;
import com.invoicesync.domain.model.BatchExecutionLog;
import com.invoicesync.domain.model.ControlCifras;
import com.invoicesync.domain.model.StatusTransitionResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BatchExecutionMapper {

    public BatchExecutionResponse toResponse(BatchExecutionLog log) {
        String status = log.getErrorDetail() != null ? "FAILED" :
                (log.hasErrors() ? "COMPLETED_WITH_ERRORS" : "SUCCESS");

        return new BatchExecutionResponse(
                log.getExecutionId(),
                log.getStartTime(),
                log.getEndTime(),
                log.getDuration().toSeconds(),
                log.getTotalInvoicesProcessed(),
                log.getSuccessCount(),
                log.getErrorCount(),
                log.getSkippedCount(),
                log.getSuccessRate(),
                log.getAttemptNumber(),
                status,
                toControlCifrasResponse(log.getControlCifrasBefore()),
                toControlCifrasResponse(log.getControlCifrasAfter()),
                toTransitionResultList(log.getResults())
        );
    }

    public ControlCifrasResponse toControlCifrasResponse(ControlCifras cifras) {
        if (cifras == null) return null;
        return new ControlCifrasResponse(
                cifras.capturedAt(),
                cifras.totalInvoices(),
                cifras.invoicesByStatus(),
                cifras.pendingSync(),
                cifras.synced(),
                cifras.failed()
        );
    }

    public TransitionResultResponse toTransitionResultResponse(StatusTransitionResult result) {
        return new TransitionResultResponse(
                result.idProveedor(),
                result.documentNumber(),
                result.uuid(),
                result.previousStatus().getCode(),
                result.newStatus().getCode(),
                result.success(),
                result.message(),
                result.processedAt()
        );
    }

    public List<TransitionResultResponse> toTransitionResultList(List<StatusTransitionResult> results) {
        return results.stream()
                .map(this::toTransitionResultResponse)
                .toList();
    }
}

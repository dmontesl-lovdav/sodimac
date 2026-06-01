package com.invoicesync.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BatchExecutionResponse(
        UUID executionId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long durationSeconds,
        int totalProcessed,
        int successCount,
        int errorCount,
        int skippedCount,
        double successRate,
        int attemptNumber,
        String status,
        ControlCifrasResponse cifrasBefore,
        ControlCifrasResponse cifrasAfter,
        List<TransitionResultResponse> results
) {}

package com.invoicesync.domain.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BatchExecutionLog {

    private final UUID executionId;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalInvoicesProcessed;
    private int successCount;
    private int errorCount;
    private int skippedCount;
    private final List<StatusTransitionResult> results;
    private final ControlCifras controlCifrasBefore;
    private ControlCifras controlCifrasAfter;
    private int attemptNumber;
    private String errorDetail;

    public BatchExecutionLog(ControlCifras controlCifrasBefore) {
        this.executionId = UUID.randomUUID();
        this.startTime = LocalDateTime.now();
        this.results = new ArrayList<>();
        this.controlCifrasBefore = controlCifrasBefore;
        this.attemptNumber = 1;
        this.totalInvoicesProcessed = 0;
        this.successCount = 0;
        this.errorCount = 0;
        this.skippedCount = 0;
    }

    public void addResult(StatusTransitionResult result) {
        this.results.add(result);
        this.totalInvoicesProcessed++;
        if (result.success()) {
            if (result.previousStatus() != result.newStatus()) {
                this.successCount++;
            } else {
                this.skippedCount++;
            }
        } else {
            this.errorCount++;
        }
    }

    public void complete(ControlCifras controlCifrasAfter) {
        this.endTime = LocalDateTime.now();
        this.controlCifrasAfter = controlCifrasAfter;
    }

    public void markAsFailed(String errorDetail) {
        this.endTime = LocalDateTime.now();
        this.errorDetail = errorDetail;
    }

    public void incrementAttempt() {
        this.attemptNumber++;
    }

    public Duration getDuration() {
        if (endTime == null) {
            return Duration.between(startTime, LocalDateTime.now());
        }
        return Duration.between(startTime, endTime);
    }

    public boolean isWithinTimeLimit(int maxMinutes) {
        return getDuration().toMinutes() <= maxMinutes;
    }

    public boolean hasErrors() {
        return errorCount > 0;
    }

    public double getSuccessRate() {
        if (totalInvoicesProcessed == 0) return 100.0;
        return (successCount * 100.0) / totalInvoicesProcessed;
    }

    // Getters
    public UUID getExecutionId() { return executionId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getTotalInvoicesProcessed() { return totalInvoicesProcessed; }
    public int getSuccessCount() { return successCount; }
    public int getErrorCount() { return errorCount; }
    public int getSkippedCount() { return skippedCount; }
    public List<StatusTransitionResult> getResults() { return Collections.unmodifiableList(results); }
    public ControlCifras getControlCifrasBefore() { return controlCifrasBefore; }
    public ControlCifras getControlCifrasAfter() { return controlCifrasAfter; }
    public int getAttemptNumber() { return attemptNumber; }
    public String getErrorDetail() { return errorDetail; }
}

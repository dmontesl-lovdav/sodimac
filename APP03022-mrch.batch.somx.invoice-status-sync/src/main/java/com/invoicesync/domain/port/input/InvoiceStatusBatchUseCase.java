package com.invoicesync.domain.port.input;

import com.invoicesync.domain.model.BatchExecutionLog;

public interface InvoiceStatusBatchUseCase {

    BatchExecutionLog executeStatusSync();

    BatchExecutionLog executeStatusSyncWithRetry(int maxRetries, int retryIntervalMinutes);

    void cancelExecution();

    boolean isExecutionInProgress();
}

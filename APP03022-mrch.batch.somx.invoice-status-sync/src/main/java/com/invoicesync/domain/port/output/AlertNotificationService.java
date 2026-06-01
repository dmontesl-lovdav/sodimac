package com.invoicesync.domain.port.output;

import com.invoicesync.domain.model.BatchExecutionLog;

public interface AlertNotificationService {

    void sendExecutionCompletedAlert(BatchExecutionLog log);

    void sendExecutionFailedAlert(BatchExecutionLog log, String errorDetail);

    void sendCriticalErrorAlert(String errorMessage);

    void sendRetryNotification(int attemptNumber, int maxAttempts, String reason);
}

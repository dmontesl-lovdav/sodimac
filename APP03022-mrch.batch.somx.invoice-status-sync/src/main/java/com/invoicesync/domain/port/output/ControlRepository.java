package com.invoicesync.domain.port.output;

import com.invoicesync.domain.model.BatchExecutionLog;
import com.invoicesync.domain.model.ControlCifras;
import com.invoicesync.domain.model.StatusTransitionResult;

public interface ControlRepository {

    void saveExecutionLog(BatchExecutionLog log);

    void saveControlCifras(ControlCifras cifras, String executionId, String phase);

    void saveTransitionResult(StatusTransitionResult result, String executionId);

    void saveErrorLog(String executionId, String idProveedor, String documentNumber, String errorDetail);

    void registerProcess(String processName, int catalogId);

    ControlCifras captureCurrentCifras();
}

package com.invoicesync.infrastructure.adapter.web;

import com.invoicesync.application.dto.BatchExecutionResponse;
import com.invoicesync.infrastructure.scheduler.InvoiceStatusBatchScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController
@RequestMapping("/api/v1/batch")
@ConditionalOnProperty(name = "batch.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BatchController {

    private static final Logger log = LoggerFactory.getLogger(BatchController.class);

    private final InvoiceStatusBatchScheduler scheduler;

    public BatchController(InvoiceStatusBatchScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @PostMapping("/sync-status/trigger")
    public ResponseEntity<?> triggerBatch() {
        log.info("Manual batch trigger requested at {}", LocalDateTime.now());

        if (scheduler.isExecutionInProgress()) {
            return ResponseEntity.status(409)
                    .body(Map.of(
                            "error", "Batch execution already in progress",
                            "message", "Please wait for the current execution to complete"
                    ));
        }

        try {
            BatchExecutionResponse response = scheduler.triggerManualExecution();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error triggering batch: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sync-status/last")
    public ResponseEntity<?> getLastExecution() {
        BatchExecutionResponse response = scheduler.getLastExecutionStatus();

        if (response == null) {
            return ResponseEntity.ok(Map.of(
                    "message", "No batch execution has been run yet"
            ));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sync-status/running")
    public ResponseEntity<Map<String, Object>> isRunning() {
        return ResponseEntity.ok(Map.of(
                "running", scheduler.isExecutionInProgress(),
                "timestamp", LocalDateTime.now()
        ));
    }
}

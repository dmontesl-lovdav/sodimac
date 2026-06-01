package com.invoicesync.application.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ControlCifrasResponse(
        LocalDateTime capturedAt,
        int totalInvoices,
        Map<Integer, Integer> invoicesByStatus,
        int pendingSync,
        int synced,
        int failed
) {}

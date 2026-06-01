package com.invoicesync.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

public record ControlCifras(
        LocalDateTime capturedAt,
        int totalInvoices,
        Map<Integer, Integer> invoicesByStatus,
        int pendingSync,
        int synced,
        int failed
) {
    public static ControlCifras capture(
            int totalInvoices,
            Map<Integer, Integer> invoicesByStatus,
            int pendingSync,
            int synced,
            int failed
    ) {
        return new ControlCifras(
                LocalDateTime.now(),
                totalInvoices,
                invoicesByStatus,
                pendingSync,
                synced,
                failed
        );
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total: ").append(totalInvoices);
        sb.append(", Pending: ").append(pendingSync);
        sb.append(", Synced: ").append(synced);
        sb.append(", Failed: ").append(failed);
        sb.append(" | By Status: ");
        invoicesByStatus.forEach((status, count) ->
            sb.append("[").append(status).append("]=").append(count).append(" ")
        );
        return sb.toString();
    }
}

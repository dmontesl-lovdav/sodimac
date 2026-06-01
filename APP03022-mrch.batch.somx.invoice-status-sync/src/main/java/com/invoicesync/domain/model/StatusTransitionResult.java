package com.invoicesync.domain.model;

import com.invoicesync.domain.enums.InvoiceFlowStatus;

import java.time.LocalDateTime;

public record StatusTransitionResult(
        String idProveedor,
        String documentNumber,
        String uuid,
        InvoiceFlowStatus previousStatus,
        InvoiceFlowStatus newStatus,
        boolean success,
        String message,
        LocalDateTime processedAt
) {
    public static StatusTransitionResult success(FbcInvoice invoice, InvoiceFlowStatus newStatus) {
        return new StatusTransitionResult(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber(),
                invoice.getUuid(),
                invoice.getCurrentStatus(),
                newStatus,
                true,
                "Status updated successfully",
                LocalDateTime.now()
        );
    }

    public static StatusTransitionResult noChange(FbcInvoice invoice) {
        return new StatusTransitionResult(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber(),
                invoice.getUuid(),
                invoice.getCurrentStatus(),
                invoice.getCurrentStatus(),
                true,
                "No status change required",
                LocalDateTime.now()
        );
    }

    public static StatusTransitionResult failure(FbcInvoice invoice, String errorMessage) {
        return new StatusTransitionResult(
                invoice.getIdProveedor(),
                invoice.getDocumentNumber(),
                invoice.getUuid(),
                invoice.getCurrentStatus(),
                invoice.getCurrentStatus(),
                false,
                errorMessage,
                LocalDateTime.now()
        );
    }
}

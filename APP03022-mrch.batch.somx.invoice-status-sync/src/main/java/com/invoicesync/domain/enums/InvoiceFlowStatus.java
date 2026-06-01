package com.invoicesync.domain.enums;

import java.util.Optional;

public enum InvoiceFlowStatus {

    PENDING_SAPITO_REGISTRATION(6, "Pendiente registro en SAPITO"),
    PENDING_I213_SEND(7, "Pendiente envío a i213"),
    SENT_TO_I213(8, "Factura enviada a la i213"),
    PENDING_SAP_ACCOUNTING(9, "Pendiente contabilizar en SAP"),
    PENDING_PAYMENT(10, "Pendiente Pago"),
    PAID(11, "Pagada"),
    ACCOUNTING_REJECTED(13, "Rechazo contable"),
    NOT_SENT_TO_I213(16, "No enviada a i213");

    private final int code;
    private final String description;

    InvoiceFlowStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<InvoiceFlowStatus> fromCode(int code) {
        for (InvoiceFlowStatus status : values()) {
            if (status.code == code) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }

    public boolean canTransitionTo(InvoiceFlowStatus targetStatus) {
        return switch (this) {
            case PENDING_SAPITO_REGISTRATION -> targetStatus == PENDING_I213_SEND;
            case PENDING_I213_SEND -> targetStatus == SENT_TO_I213;
            case SENT_TO_I213 -> targetStatus == PENDING_SAP_ACCOUNTING || targetStatus == NOT_SENT_TO_I213;
            case PENDING_SAP_ACCOUNTING -> targetStatus == PENDING_PAYMENT || targetStatus == ACCOUNTING_REJECTED;
            case PENDING_PAYMENT -> targetStatus == PAID;
            default -> false;
        };
    }

    public boolean isTerminalStatus() {
        return this == PAID || this == ACCOUNTING_REJECTED || this == NOT_SENT_TO_I213;
    }
}

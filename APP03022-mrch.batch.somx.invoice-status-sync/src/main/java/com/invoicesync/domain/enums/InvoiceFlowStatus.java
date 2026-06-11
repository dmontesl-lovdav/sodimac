package com.invoicesync.domain.enums;

import java.util.Optional;

/**
 * Estatus de factura que maneja el batch SAPITO → i213 → pago.
 *
 * Renumerado al Tren de Estatus v1.0 (Tren_Estatus_Portal_FBC_v1.0.xlsx, Ivan 2026-06-02,
 * cargado a BD shared_catalogs.status_train option_id=1 + enum fiscal-api InvoiceStatus).
 *
 * Códigos y transiciones = fuente de verdad en BD. La numeración previa (STM-1309: 6..16)
 * quedó OBSOLETA; este batch arranca en estatus 7 (Pendiente registro en SAPITO).
 */
public enum InvoiceFlowStatus {

    PENDIENTE_REGISTRO_SAPITO(7, "Pendiente registro en SAPITO"),
    PENDIENTE_ENVIO_I213(8, "Pendiente de envío a i213"),
    FACTURA_ENVIADA_I213(9, "Factura enviada a i213"),
    PENDIENTE_CONTABILIZAR(10, "Pendiente de contabilizar"),
    PENDIENTE_PAGO(11, "Pendiente de Pago"),
    PENDIENTE_COMPLEMENTO(12, "Pendiente de complemento"),
    RECHAZO_CONTABLE(14, "Rechazo Contable"),
    ERROR_ENVIO_I213(17, "Error envío i213");

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

    /**
     * Transiciones permitidas según el tren v1.0 (status_train option_id=1).
     */
    public boolean canTransitionTo(InvoiceFlowStatus targetStatus) {
        return switch (this) {
            case PENDIENTE_REGISTRO_SAPITO -> targetStatus == PENDIENTE_ENVIO_I213;
            case PENDIENTE_ENVIO_I213 -> targetStatus == FACTURA_ENVIADA_I213;
            case FACTURA_ENVIADA_I213 -> targetStatus == PENDIENTE_CONTABILIZAR || targetStatus == ERROR_ENVIO_I213;
            case PENDIENTE_CONTABILIZAR -> targetStatus == PENDIENTE_PAGO || targetStatus == RECHAZO_CONTABLE;
            case PENDIENTE_PAGO -> targetStatus == PENDIENTE_COMPLEMENTO;
            case RECHAZO_CONTABLE -> targetStatus == PENDIENTE_ENVIO_I213;
            case ERROR_ENVIO_I213 -> targetStatus == PENDIENTE_ENVIO_I213;
            default -> false;
        };
    }

    public boolean isTerminalStatus() {
        return this == PENDIENTE_COMPLEMENTO;
    }
}

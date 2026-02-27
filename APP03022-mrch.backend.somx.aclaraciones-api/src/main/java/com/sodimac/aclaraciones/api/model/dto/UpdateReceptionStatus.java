package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.constraints.NotNull;

/** Nodo interno dentro de {@link UpdatePurchaseOrderRequest}. */
public record UpdateReceptionStatus(

        /** ID de la recepción a modificar */
        @NotNull(message = "ERR_FIELD_RECEPCION|Recepción obligatoria") Long recepcion,

        /** Nuevo estatus de esa recepción */
        @NotNull(message = "ERR_FIELD_ESTATUS_RECEPCION|Estatus obligatorio") Integer estatus) {
}

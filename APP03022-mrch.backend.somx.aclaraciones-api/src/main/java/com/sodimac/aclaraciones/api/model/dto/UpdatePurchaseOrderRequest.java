package com.sodimac.aclaraciones.api.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Cuerpo del PATCH /purchase-orders/{ordenCompra}.
 * Permite actualizar:
 * • el estatus de la cabecera (OC) y
 * • opcionalmente uno o más estatus de recepción.
 */
public record UpdatePurchaseOrderRequest(

        /** Nuevo estatus de la Orden de Compra */
        @NotNull(message = "ERR_FIELD_ESTATUS_OC|Estatus OC obligatorio") Integer estatusOC,

        /**
         * (Opcional) lista de recepciones a modificar.
         * Si viene null o vacía, sólo se actualiza la cabecera.
         */
        List<@Valid UpdateReceptionStatus> recepciones) {
}

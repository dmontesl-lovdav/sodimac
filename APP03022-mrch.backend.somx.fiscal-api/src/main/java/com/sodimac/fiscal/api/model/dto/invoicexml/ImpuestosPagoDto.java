package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para impuestos del pago en el complemento Pagos v2.0.
 *
 * Contiene los impuestos retenidos y trasladados que se derivan
 * de un pago específico.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ImpuestosPagoDto {

    @XmlElement(name = "RetencionesP")
    private List<RetencionPagoDto> retenciones;

    @XmlElement(name = "TrasladosP")
    private List<TrasladoPagoDto> traslados;
}
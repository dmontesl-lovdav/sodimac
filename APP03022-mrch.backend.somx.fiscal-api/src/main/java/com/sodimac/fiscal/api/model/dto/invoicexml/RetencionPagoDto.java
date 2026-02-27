package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para retenciones del pago en Pagos v2.0.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class RetencionPagoDto {

    @XmlAttribute(name = "ImpuestoP")
    private String impuestoP;

    @XmlAttribute(name = "ImporteP")
    private String importeP;
}
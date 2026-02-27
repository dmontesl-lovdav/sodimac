package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para traslados del pago en Pagos v2.0.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TrasladoPagoDto {

    @XmlAttribute(name = "BaseP")
    private String baseP;

    @XmlAttribute(name = "ImpuestoP")
    private String impuestoP;

    @XmlAttribute(name = "TipoFactorP")
    private String tipoFactorP;

    @XmlAttribute(name = "TasaOCuotaP")
    private String tasaOCuotaP;

    @XmlAttribute(name = "ImporteP")
    private String importeP;
}
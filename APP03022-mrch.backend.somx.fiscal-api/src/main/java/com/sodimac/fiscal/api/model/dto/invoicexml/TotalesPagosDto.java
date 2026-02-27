package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para los totales del complemento Pagos v2.0.
 *
 * Contiene los montos totales de impuestos retenidos y trasladados
 * que se desprenden de los pagos efectuados, expresados en MXN.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TotalesPagosDto {

    @XmlAttribute(name = "TotalRetencionesIVA")
    private String totalRetencionesIVA;

    @XmlAttribute(name = "TotalRetencionesISR")
    private String totalRetencionesISR;

    @XmlAttribute(name = "TotalRetencionesIEPS")
    private String totalRetencionesIEPS;

    @XmlAttribute(name = "TotalTrasladosBaseIVA16")
    private String totalTrasladosBaseIVA16;

    @XmlAttribute(name = "TotalTrasladosImpuestoIVA16")
    private String totalTrasladosImpuestoIVA16;

    @XmlAttribute(name = "TotalTrasladosBaseIVA8")
    private String totalTrasladosBaseIVA8;

    @XmlAttribute(name = "TotalTrasladosImpuestoIVA8")
    private String totalTrasladosImpuestoIVA8;

    @XmlAttribute(name = "TotalTrasladosBaseIVA0")
    private String totalTrasladosBaseIVA0;

    @XmlAttribute(name = "TotalTrasladosImpuestoIVA0")
    private String totalTrasladosImpuestoIVA0;

    @XmlAttribute(name = "TotalTrasladosBaseIVAExento")
    private String totalTrasladosBaseIVAExento;

    @XmlAttribute(name = "MontoTotalPagos")
    private String montoTotalPagos;
}
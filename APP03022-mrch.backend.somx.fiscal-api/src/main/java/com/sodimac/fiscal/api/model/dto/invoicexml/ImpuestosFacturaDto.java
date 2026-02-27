package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para Impuestos consolidados a nivel factura - CRÍTICO para cumplimiento SAT
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ImpuestosFacturaDto {
    @XmlAttribute(name = "TotalImpuestosRetenidos")
    private String totalImpuestosRetenidos;    // Total de impuestos retenidos

    @XmlAttribute(name = "TotalImpuestosTrasladados")
    private String totalImpuestosTrasladados;  // Total de impuestos trasladados

    @XmlElementWrapper(name = "Retenciones", namespace = "http://www.sat.gob.mx/cfd/4")
    @XmlElement(name = "Retencion", namespace = "http://www.sat.gob.mx/cfd/4")
    private List<RetencionDto> retenciones;    // Retenciones consolidadas

    @XmlElementWrapper(name = "Traslados", namespace = "http://www.sat.gob.mx/cfd/4")
    @XmlElement(name = "Traslado", namespace = "http://www.sat.gob.mx/cfd/4")
    private List<TrasladoDto> traslados;       // Traslados consolidados
}
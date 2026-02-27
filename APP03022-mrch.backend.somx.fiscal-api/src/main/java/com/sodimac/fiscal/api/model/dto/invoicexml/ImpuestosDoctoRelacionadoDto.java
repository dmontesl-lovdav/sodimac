package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para impuestos del documento relacionado en Pagos v2.0.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ImpuestosDoctoRelacionadoDto {

    @XmlElement(name = "RetencionesDR")
    private List<RetencionDoctoRelacionadoDto> retenciones;

    @XmlElement(name = "TrasladosDR")
    private List<TrasladoDoctoRelacionadoDto> traslados;
}
package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para traslados del documento relacionado en Pagos v2.0.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TrasladoDoctoRelacionadoDto {

    @XmlAttribute(name = "BaseDR")
    private String baseDR;

    @XmlAttribute(name = "ImpuestoDR")
    private String impuestoDR;

    @XmlAttribute(name = "TipoFactorDR")
    private String tipoFactorDR;

    @XmlAttribute(name = "TasaOCuotaDR")
    private String tasaOCuotaDR;

    @XmlAttribute(name = "ImporteDR")
    private String importeDR;
}
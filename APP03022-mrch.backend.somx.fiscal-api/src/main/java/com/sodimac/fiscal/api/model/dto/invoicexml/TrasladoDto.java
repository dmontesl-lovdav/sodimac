package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para los traslados de impuestos en conceptos CFDI v4.0
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class TrasladoDto {
    @XmlAttribute(name = "Base")
    private String base;

    @XmlAttribute(name = "Impuesto")
    private String impuesto;

    @XmlAttribute(name = "TipoFactor")
    private String tipoFactor;

    @XmlAttribute(name = "TasaOCuota")
    private String tasaOCuota;

    @XmlAttribute(name = "Importe")
    private String importe;
}
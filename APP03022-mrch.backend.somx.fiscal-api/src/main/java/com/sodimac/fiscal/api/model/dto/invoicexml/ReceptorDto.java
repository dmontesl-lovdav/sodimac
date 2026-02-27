package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para el elemento Receptor del CFDI v4.0
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ReceptorDto {

    @XmlAttribute(name = "Rfc")
    private String rfc;

    @XmlAttribute(name = "Nombre")
    private String nombre;

    @XmlAttribute(name = "UsoCFDI")
    private String usoCFDI;

    @XmlAttribute(name = "DomicilioFiscalReceptor")
    private String domicilioFiscalReceptor;

    @XmlAttribute(name = "RegimenFiscalReceptor")
    private String regimenFiscalReceptor;
}

package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para Mercancías en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class MercanciasDto {
    @XmlAttribute(name = "NumTotalMercancias")
    private String numTotalMercancias;

    @XmlAttribute(name = "UnidadPeso")
    private String unidadPeso;

    @XmlAttribute(name = "PesoBrutoTotal")
    private String pesoBrutoTotal;

    @XmlElement(name = "Mercancia")
    private List<MercanciaDto> mercancias;

    @XmlElement(name = "Autotransporte")
    private AutotransporteDto autotransporte;
}
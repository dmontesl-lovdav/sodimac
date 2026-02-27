package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para Domicilio en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class DomicilioDto {
    @XmlAttribute(name = "Calle")
    private String calle;

    @XmlAttribute(name = "Estado")
    private String estado;

    @XmlAttribute(name = "Pais")
    private String pais;

    @XmlAttribute(name = "CodigoPostal")
    private String codigoPostal;

    @XmlAttribute(name = "Colonia")
    private String colonia;

    @XmlAttribute(name = "Municipio")
    private String municipio;

    @XmlAttribute(name = "NumeroExterior")
    private String numeroExterior;

    @XmlAttribute(name = "NumeroInterior")
    private String numeroInterior;
}
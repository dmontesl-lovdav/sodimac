package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

/**
 * DTO para Ubicaciones en CartaPorte
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class UbicacionDto {
    @XmlAttribute(name = "TipoUbicacion")
    private String tipoUbicacion;

    @XmlAttribute(name = "RFCRemitenteDestinatario")
    private String rfcRemitenteDestinatario;

    @XmlAttribute(name = "FechaHoraSalidaLlegada")
    private String fechaHoraSalidaLlegada;

    @XmlAttribute(name = "NombreRemitenteDestinatario")
    private String nombreRemitenteDestinatario;

    @XmlAttribute(name = "DistanciaRecorrida")
    private String distanciaRecorrida;

    @XmlElement(name = "Domicilio")
    private DomicilioDto domicilio;
}
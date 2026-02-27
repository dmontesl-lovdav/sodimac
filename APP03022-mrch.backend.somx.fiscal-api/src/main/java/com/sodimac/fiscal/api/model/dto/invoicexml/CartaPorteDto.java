package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.xml.bind.annotation.*;
import java.util.List;

/**
 * DTO para el complemento CartaPorte CFDI v4.0 - Documento electrónico de transporte.
 *
 * El complemento CartaPorte es utilizado en México para documentar el traslado
 * de mercancías por vías terrestres, férreas, marítimas o aéreas. Contiene
 * información detallada sobre el transporte, ubicaciones, mercancías y
 * participantes en la operación logística.
 *
 * Componentes principales:
 * - Información general del transporte (versión, tipo, distancias)
 * - Ubicaciones de origen y destino con datos completos
 * - Detalle de mercancías transportadas (peso, tipo, valor)
 * - Figuras de transporte (operadores, vehículos, seguros)
 * - Datos del autotransporte cuando aplique
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlRootElement(name = "CartaPorte", namespace = "http://www.sat.gob.mx/CartaPorte31")
@XmlAccessorType(XmlAccessType.FIELD)
public class CartaPorteDto {
    @XmlAttribute(name = "Version")
    private String version;

    @XmlAttribute(name = "TranspInternac")
    private String transpInternac;

    @XmlAttribute(name = "TotalDistRec")
    private String totalDistRec;

    @XmlAttribute(name = "IdCCP")
    private String idCCP;

    @XmlElement(name = "Ubicaciones")
    private List<UbicacionDto> ubicaciones;

    @XmlElement(name = "Mercancias")
    private MercanciasDto mercancias;

    @XmlElement(name = "FiguraTransporte")
    private List<FiguraTransporteDto> figurasTransporte;
}
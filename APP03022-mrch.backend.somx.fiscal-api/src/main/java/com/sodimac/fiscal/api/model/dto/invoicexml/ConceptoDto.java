package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para representar los conceptos (líneas de productos/servicios) de facturas CFDI v4.0.
 *
 * Cada concepto representa un producto o servicio facturado, incluyendo su descripción,
 * cantidad, precios, unidades de medida y tratamiento fiscal. Los conceptos son
 * elementos fundamentales de una factura que determinan el cálculo de impuestos.
 *
 * Campos principales:
 * - Identificación: clave de producto/servicio SAT, número de identificación
 * - Cantidades: cantidad, unidad, clave de unidad SAT
 * - Precios: valor unitario, importe total
 * - Fiscales: objeto de impuesto, traslados y retenciones aplicables
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ConceptoDto {
    @XmlAttribute(name = "ClaveProdServ")
    private String claveProdServ;

    @XmlAttribute(name = "NoIdentificacion")
    private String noIdentificacion;

    @XmlAttribute(name = "Cantidad")
    private String cantidad;

    @XmlAttribute(name = "ClaveUnidad")
    private String claveUnidad;

    @XmlAttribute(name = "Unidad")
    private String unidad;

    @XmlAttribute(name = "Descripcion")
    private String descripcion;

    @XmlAttribute(name = "ValorUnitario")
    private String valorUnitario;

    @XmlAttribute(name = "Importe")
    private String importe;

    @XmlAttribute(name = "ObjetoImp")
    private String objetoImp;

    @XmlElement(name = "Traslado")
    private List<TrasladoDto> traslados;

    @XmlElement(name = "Retencion")
    private List<RetencionDto> retenciones;
}
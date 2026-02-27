package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para documentos relacionados en el complemento Pagos v2.0.
 *
 * Representa cada documento (factura, nota de crédito, etc.) que es
 * liquidado parcial o totalmente por un pago específico.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class DoctoRelacionadoDto {

    @XmlAttribute(name = "IdDocumento")
    private String idDocumento;

    @XmlAttribute(name = "Serie")
    private String serie;

    @XmlAttribute(name = "Folio")
    private String folio;

    @XmlAttribute(name = "MonedaDR")
    private String monedaDR;

    @XmlAttribute(name = "EquivalenciaDR")
    private String equivalenciaDR;

    @XmlAttribute(name = "NumParcialidad")
    private String numParcialidad;

    @XmlAttribute(name = "ImpSaldoAnt")
    private String impSaldoAnt;

    @XmlAttribute(name = "ImpPagado")
    private String impPagado;

    @XmlAttribute(name = "ImpSaldoInsoluto")
    private String impSaldoInsoluto;

    @XmlAttribute(name = "ObjetoImpDR")
    private String objetoImpDR;

    @XmlElement(name = "ImpuestosDR")
    private ImpuestosDoctoRelacionadoDto impuestos;
}
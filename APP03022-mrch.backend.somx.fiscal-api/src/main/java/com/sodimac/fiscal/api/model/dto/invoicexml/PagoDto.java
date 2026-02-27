package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO para cada pago individual dentro del complemento Pagos v2.0.
 *
 * Representa un pago específico con su forma de pago, monto, fecha
 * y documentos relacionados que fueron liquidados con este pago.
 *
 * @author g_dco018
 * @version 1.0
 * @since 2025
 */
@Data
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class PagoDto {

    @XmlAttribute(name = "FechaPago")
    private String fechaPago;

    @XmlAttribute(name = "FormaDePagoP")
    private String formaDePagoP;

    @XmlAttribute(name = "MonedaP")
    private String monedaP;

    @XmlAttribute(name = "TipoCambioP")
    private String tipoCambioP;

    @XmlAttribute(name = "Monto")
    private String monto;

    @XmlAttribute(name = "NumOperacion")
    private String numOperacion;

    @XmlAttribute(name = "RfcEmisorCtaOrd")
    private String rfcEmisorCtaOrd;

    @XmlAttribute(name = "NomBancoOrdExt")
    private String nomBancoOrdExt;

    @XmlAttribute(name = "CtaOrdenante")
    private String ctaOrdenante;

    @XmlAttribute(name = "RfcEmisorCtaBen")
    private String rfcEmisorCtaBen;

    @XmlAttribute(name = "CtaBeneficiario")
    private String ctaBeneficiario;

    @XmlElement(name = "DoctoRelacionado")
    private List<DoctoRelacionadoDto> doctosRelacionados;

    @XmlElement(name = "ImpuestosP")
    private ImpuestosPagoDto impuestos;
}
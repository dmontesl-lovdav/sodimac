package com.sodimac.fiscal.api.model.dto.invoicexml;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.xml.bind.annotation.*;

import java.util.List;

/**
 * DTO principal para almacenar información específica extraída de facturas XML CFDI v4.0.
 *
 * Esta clase encapsula todos los datos relevantes de una factura electrónica mexicana,
 * incluyendo información del comprobante, emisor, receptor, conceptos, impuestos,
 * complementos y addendas. Proporciona una estructura organizada para el manejo
 * de datos fiscales complejos.
 *
 * Secciones incluidas:
 * - Datos generales del comprobante (folio, fecha, totales, etc.)
 * - Información del emisor y receptor
 * - Lista de conceptos facturados
 * - Impuestos (traslados y retenciones)
 * - Complementos especializados (CartaPorte, TimbreFiscalDigital)
 * - Addendas personalizadas de proveedores
 *
 * @author g_dco018
 * @version 1.0
 * @since 2024
 */
@Data
@NoArgsConstructor
@XmlRootElement(name = "Comprobante", namespace = "http://www.sat.gob.mx/cfd/4")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceXmlDto {
    @XmlAttribute(name = "Version")
    private String version;

    @XmlAttribute(name = "Serie")
    private String serie;

    @XmlAttribute(name = "Folio")
    private String folio;

    @XmlAttribute(name = "Fecha")
    private String fecha;

    @XmlAttribute(name = "SubTotal")
    private String subTotal;

    @XmlAttribute(name = "Total")
    private String total;

    @XmlAttribute(name = "TipoDeComprobante")
    private String tipoDeComprobante;

    @XmlAttribute(name = "MetodoPago")
    private String metodoPago;

    @XmlAttribute(name = "FormaPago")
    private String formaPago;

    @XmlAttribute(name = "CondicionesDePago")
    private String condicionesDePago;

    @XmlAttribute(name = "Moneda")
    private String moneda;

    @XmlAttribute(name = "TipoCambio")
    private String tipoCambio;

    @XmlAttribute(name = "NoCertificado")
    private String noCertificado;

    @XmlAttribute(name = "Certificado")
    private String certificado;

    @XmlAttribute(name = "Sello")
    private String sello;

    @XmlAttribute(name = "LugarExpedicion")
    private String lugarExpedicion;

    @XmlAttribute(name = "Exportacion")
    private String exportacion;

    @XmlElement(name = "CfdiRelacionados", namespace = "http://www.sat.gob.mx/cfd/4")
    private CfdiRelacionadosDto cfdiRelacionados;

    @XmlElement(name = "Emisor", namespace = "http://www.sat.gob.mx/cfd/4")
    private EmisorDto emisor;

    @XmlElement(name = "Receptor", namespace = "http://www.sat.gob.mx/cfd/4")
    private ReceptorDto receptor;

    @XmlAttribute(name = "Descuento")
    private String descuento;

    @XmlElementWrapper(name = "Conceptos", namespace = "http://www.sat.gob.mx/cfd/4")
    @XmlElement(name = "Concepto", namespace = "http://www.sat.gob.mx/cfd/4")
    private List<ConceptoDto> conceptos;

    @XmlElement(name = "Addenda", namespace = "http://www.sat.gob.mx/cfd/4")
    private List<AddendaDto> addendas;

    @XmlElement(name = "Complemento", namespace = "http://www.sat.gob.mx/cfd/4")
    private ComplementoDto complemento;

    @XmlElement(name = "Impuestos", namespace = "http://www.sat.gob.mx/cfd/4")
    private ImpuestosFacturaDto impuestosFactura;

    // Métodos de compatibilidad para acceso a datos de Emisor y Receptor
    public String getEmisorRfc() {
        return emisor != null ? emisor.getRfc() : null;
    }

    public String getEmisorNombre() {
        return emisor != null ? emisor.getNombre() : null;
    }

    public String getEmisorRegimenFiscal() {
        return emisor != null ? emisor.getRegimenFiscal() : null;
    }

    public String getReceptorRfc() {
        return receptor != null ? receptor.getRfc() : null;
    }

    public String getReceptorNombre() {
        return receptor != null ? receptor.getNombre() : null;
    }

    public String getReceptorUsoCFDI() {
        return receptor != null ? receptor.getUsoCFDI() : null;
    }

    public String getReceptorDomicilioFiscalReceptor() {
        return receptor != null ? receptor.getDomicilioFiscalReceptor() : null;
    }

    public String getReceptorRegimenFiscalReceptor() {
        return receptor != null ? receptor.getRegimenFiscalReceptor() : null;
    }

    // Métodos de compatibilidad para acceso a Complemento
    public TimbreFiscalDigitalDto getTimbreFiscalDigital() {
        return complemento != null ? complemento.getTimbreFiscalDigital() : null;
    }

    public CartaPorteDto getCartaPorte() {
        return complemento != null ? complemento.getCartaPorte() : null;
    }
}
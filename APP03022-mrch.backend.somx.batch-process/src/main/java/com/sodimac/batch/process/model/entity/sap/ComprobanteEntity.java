package com.sodimac.batch.process.model.entity.sap;

import javax.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comprobante")
public class ComprobanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer idComprobante;

    @Column(name = "fiscal_uuid", nullable = false, length = 36)
    private String fiscalUuid;

    @Column(name = "invoice_uuid", length = 36)
    private String invoiceUuid;

    @Column(name = "version", nullable = false, length = 5)
    private String version;

    @Column(name = "serie", length = 25)
    private String serie;

    @Column(name = "folio", length = 49)
    private String folio;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "subtotal", nullable = false, precision = 18, scale = 6)
    private BigDecimal subtotal;

    @Column(name = "total", nullable = false, precision = 18, scale = 6)
    private BigDecimal total;

    @Column(name = "descuento", precision = 18, scale = 6)
    private BigDecimal descuento;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "tipo_cambio", precision = 18, scale = 6)
    private BigDecimal tipoCambio;

    @Column(name = "tipo_comprobante", nullable = false, length = 2)
    private String tipoComprobante;

    @Column(name = "metodo_pago", length = 3)
    private String metodoPago;

    @Column(name = "forma_pago", length = 10)
    private String formaPago;

    @Column(name = "condiciones_pago", length = 255)
    private String condicionesPago;

    @Column(name = "lugar_expedicion", length = 5)
    private String lugarExpedicion;

    @Column(name = "exportacion", length = 2)
    private String exportacion;

    @Column(name = "no_certificado", length = 30)
    private String noCertificado;

    @Column(name = "sello", columnDefinition = "VARCHAR(MAX)")
    private String sello;

    @Column(name = "certificado", columnDefinition = "VARCHAR(MAX)")
    private String certificado;

    @Column(name = "xml_completo", columnDefinition = "VARCHAR(MAX)")
    private String xmlCompleto;

    @Column(name = "fecha_timbrado")
    private LocalDateTime fechaTimbrado;

    @Column(name = "rfc_prov_certif", length = 13)
    private String rfcProvCertif;

    @Column(name = "no_certificado_sat", length = 30)
    private String noCertificadoSat;

    @Column(name = "estatus_proceso", nullable = false, length = 20)
    private String estatusProceso;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    public ComprobanteEntity() {}

    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }

    public String getFiscalUuid() { return fiscalUuid; }
    public void setFiscalUuid(String fiscalUuid) { this.fiscalUuid = fiscalUuid; }

    public String getInvoiceUuid() { return invoiceUuid; }
    public void setInvoiceUuid(String invoiceUuid) { this.invoiceUuid = invoiceUuid; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public BigDecimal getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

    public String getCondicionesPago() { return condicionesPago; }
    public void setCondicionesPago(String condicionesPago) { this.condicionesPago = condicionesPago; }

    public String getLugarExpedicion() { return lugarExpedicion; }
    public void setLugarExpedicion(String lugarExpedicion) { this.lugarExpedicion = lugarExpedicion; }

    public String getExportacion() { return exportacion; }
    public void setExportacion(String exportacion) { this.exportacion = exportacion; }

    public String getNoCertificado() { return noCertificado; }
    public void setNoCertificado(String noCertificado) { this.noCertificado = noCertificado; }

    public String getSello() { return sello; }
    public void setSello(String sello) { this.sello = sello; }

    public String getCertificado() { return certificado; }
    public void setCertificado(String certificado) { this.certificado = certificado; }

    public String getXmlCompleto() { return xmlCompleto; }
    public void setXmlCompleto(String xmlCompleto) { this.xmlCompleto = xmlCompleto; }

    public LocalDateTime getFechaTimbrado() { return fechaTimbrado; }
    public void setFechaTimbrado(LocalDateTime fechaTimbrado) { this.fechaTimbrado = fechaTimbrado; }

    public String getRfcProvCertif() { return rfcProvCertif; }
    public void setRfcProvCertif(String rfcProvCertif) { this.rfcProvCertif = rfcProvCertif; }

    public String getNoCertificadoSat() { return noCertificadoSat; }
    public void setNoCertificadoSat(String noCertificadoSat) { this.noCertificadoSat = noCertificadoSat; }

    public String getEstatusProceso() { return estatusProceso; }
    public void setEstatusProceso(String estatusProceso) { this.estatusProceso = estatusProceso; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}

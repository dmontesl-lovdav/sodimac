package com.sodimac.batch.fiscal.download.model.entity.sap;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Comprobante en el destino SAP legado (SODIMAC_SAP_DEV.dbo.Comprobante).
 *
 * Esquema legado (Detecno): la PK es {@code Uuid} (folio fiscal SAT); no existe columna
 * identity. Las tablas hijas (Emisor, Concepto, DetalleImpuesto, Addenda) se ligan por Uuid.
 * Estatus: 0 = pendiente de envío (FechaEnvio null), 1 = enviado.
 * fiscal_uuid / invoice_uuid: trazabilidad contra el portal FBC (fiscal-api).
 * FechaCadena: la Fecha del CFDI en formato d/M/yyyy H:mm:ss (sin ceros a la izquierda).
 * TipoCambio es varchar en el esquema legado, no numérico.
 */
@Entity
@Table(name = "Comprobante")
public class ComprobanteEntity {

    @Id
    @Column(name = "Uuid", length = 36)
    private String uuid;

    @Column(name = "fiscal_uuid", length = 36)
    private String fiscalUuid;

    @Column(name = "invoice_uuid", length = 36)
    private String invoiceUuid;

    @Column(name = "Version", length = 5)
    private String version;

    @Column(name = "Serie", length = 25)
    private String serie;

    @Column(name = "Folio", length = 40)
    private String folio;

    @Column(name = "Fecha")
    private LocalDateTime fecha;

    @Column(name = "FechaCadena", length = 50)
    private String fechaCadena;

    @Column(name = "Subtotal", precision = 18, scale = 6)
    private BigDecimal subTotal;

    @Column(name = "Total", precision = 18, scale = 6)
    private BigDecimal total;

    @Column(name = "Descuento", precision = 18, scale = 6)
    private BigDecimal descuento;

    @Column(name = "Moneda", length = 3)
    private String moneda;

    @Column(name = "TipoCambio", length = 18)
    private String tipoCambio;

    @Column(name = "TipoDeComprobante", length = 1)
    private String tipoDeComprobante;

    @Column(name = "MetodoPago", length = 3)
    private String metodoPago;

    @Column(name = "FormaPago", length = 2)
    private String formaPago;

    @Column(name = "CondicionDePago", length = 1000)
    private String condicionDePago;

    @Column(name = "LugarExpedicion", length = 5)
    private String lugarExpedicion;

    @Column(name = "Xml")
    private String xmlCompleto;

    @Column(name = "Estatus")
    private Integer estatus;

    @Column(name = "FechaEnvio")
    private LocalDateTime fechaEnvio;

    public ComprobanteEntity() {}

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

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

    public String getFechaCadena() { return fechaCadena; }
    public void setFechaCadena(String fechaCadena) { this.fechaCadena = fechaCadena; }

    public BigDecimal getSubTotal() { return subTotal; }
    public void setSubTotal(BigDecimal subTotal) { this.subTotal = subTotal; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(String tipoCambio) { this.tipoCambio = tipoCambio; }

    public String getTipoDeComprobante() { return tipoDeComprobante; }
    public void setTipoDeComprobante(String tipoDeComprobante) { this.tipoDeComprobante = tipoDeComprobante; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }

    public String getCondicionDePago() { return condicionDePago; }
    public void setCondicionDePago(String condicionDePago) { this.condicionDePago = condicionDePago; }

    public String getLugarExpedicion() { return lugarExpedicion; }
    public void setLugarExpedicion(String lugarExpedicion) { this.lugarExpedicion = lugarExpedicion; }

    public String getXmlCompleto() { return xmlCompleto; }
    public void setXmlCompleto(String xmlCompleto) { this.xmlCompleto = xmlCompleto; }

    public Integer getEstatus() { return estatus; }
    public void setEstatus(Integer estatus) { this.estatus = estatus; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}

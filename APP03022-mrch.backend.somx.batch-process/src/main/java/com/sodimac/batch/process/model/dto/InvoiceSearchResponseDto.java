package com.sodimac.batch.process.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceSearchResponseDto {

    // Comprobante
    private UUID invoiceUuid;
    private UUID fiscalUuid;
    private String documentType;
    private String series;
    private String folio;
    private BigDecimal version;
    private LocalDate issueDate;
    private LocalDateTime certificationDate;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private String currency;
    private BigDecimal exchangeRate;
    private String paymentMethod;
    private String paymentForm;
    private String paymentConditions;
    private String placeOfIssue;
    private Integer status;
    private String statusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Emisor
    private String emisorRfc;
    private String emisorName;
    private String emisorTaxRegime;

    // Receptor
    private String receptorRfc;
    private String receptorName;
    private String receptorTaxRegime;

    // Addenda
    private Boolean hasAddenda;
    private UUID addendaUuid;
    private Integer addendaType;
    private String addendaTypeName;
    private String noOrdenCompra;
    private String noRecepcion;
    private BigDecimal numeroProveedor;
    private String tipoProveedor;
    private String guiaEntrega;

    // XML completo
    private String xmlContent;

    public InvoiceSearchResponseDto() {}

    public UUID getInvoiceUuid() { return invoiceUuid; }
    public void setInvoiceUuid(UUID invoiceUuid) { this.invoiceUuid = invoiceUuid; }

    public UUID getFiscalUuid() { return fiscalUuid; }
    public void setFiscalUuid(UUID fiscalUuid) { this.fiscalUuid = fiscalUuid; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public BigDecimal getVersion() { return version; }
    public void setVersion(BigDecimal version) { this.version = version; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDateTime getCertificationDate() { return certificationDate; }
    public void setCertificationDate(LocalDateTime certificationDate) { this.certificationDate = certificationDate; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentForm() { return paymentForm; }
    public void setPaymentForm(String paymentForm) { this.paymentForm = paymentForm; }

    public String getPaymentConditions() { return paymentConditions; }
    public void setPaymentConditions(String paymentConditions) { this.paymentConditions = paymentConditions; }

    public String getPlaceOfIssue() { return placeOfIssue; }
    public void setPlaceOfIssue(String placeOfIssue) { this.placeOfIssue = placeOfIssue; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getEmisorRfc() { return emisorRfc; }
    public void setEmisorRfc(String emisorRfc) { this.emisorRfc = emisorRfc; }

    public String getEmisorName() { return emisorName; }
    public void setEmisorName(String emisorName) { this.emisorName = emisorName; }

    public String getEmisorTaxRegime() { return emisorTaxRegime; }
    public void setEmisorTaxRegime(String emisorTaxRegime) { this.emisorTaxRegime = emisorTaxRegime; }

    public String getReceptorRfc() { return receptorRfc; }
    public void setReceptorRfc(String receptorRfc) { this.receptorRfc = receptorRfc; }

    public String getReceptorName() { return receptorName; }
    public void setReceptorName(String receptorName) { this.receptorName = receptorName; }

    public String getReceptorTaxRegime() { return receptorTaxRegime; }
    public void setReceptorTaxRegime(String receptorTaxRegime) { this.receptorTaxRegime = receptorTaxRegime; }

    public Boolean getHasAddenda() { return hasAddenda; }
    public void setHasAddenda(Boolean hasAddenda) { this.hasAddenda = hasAddenda; }

    public UUID getAddendaUuid() { return addendaUuid; }
    public void setAddendaUuid(UUID addendaUuid) { this.addendaUuid = addendaUuid; }

    public Integer getAddendaType() { return addendaType; }
    public void setAddendaType(Integer addendaType) { this.addendaType = addendaType; }

    public String getAddendaTypeName() { return addendaTypeName; }
    public void setAddendaTypeName(String addendaTypeName) { this.addendaTypeName = addendaTypeName; }

    public String getNoOrdenCompra() { return noOrdenCompra; }
    public void setNoOrdenCompra(String noOrdenCompra) { this.noOrdenCompra = noOrdenCompra; }

    public String getNoRecepcion() { return noRecepcion; }
    public void setNoRecepcion(String noRecepcion) { this.noRecepcion = noRecepcion; }

    public BigDecimal getNumeroProveedor() { return numeroProveedor; }
    public void setNumeroProveedor(BigDecimal numeroProveedor) { this.numeroProveedor = numeroProveedor; }

    public String getTipoProveedor() { return tipoProveedor; }
    public void setTipoProveedor(String tipoProveedor) { this.tipoProveedor = tipoProveedor; }

    public String getGuiaEntrega() { return guiaEntrega; }
    public void setGuiaEntrega(String guiaEntrega) { this.guiaEntrega = guiaEntrega; }

    public String getXmlContent() { return xmlContent; }
    public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }
}

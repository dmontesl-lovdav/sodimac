
package com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para respuestaXml complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="respuestaXml"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CadenaOriginal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="DomicilioFiscalReceptor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorDesc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="EstatusId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Exportacion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="FacAtrAdquirente" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="FacturaId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="FacturaIdOriginal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Folio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="MontoOperacion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="MontoRetencion" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Qr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="RechazoId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="RegimenFiscalReceptor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="RfcEmisor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="RfcReceptor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Sello" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Serie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Total1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Total2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Uuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="UuidOriginal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Validate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Xml" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="XmlAcuseCancelacionCfdi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "respuestaXml", propOrder = {
    "cadenaOriginal",
    "domicilioFiscalReceptor",
    "errorDesc",
    "errorMessage",
    "estatusId",
    "exportacion",
    "facAtrAdquirente",
    "facturaId",
    "facturaIdOriginal",
    "folio",
    "montoOperacion",
    "montoRetencion",
    "qr",
    "rechazoId",
    "regimenFiscalReceptor",
    "rfcEmisor",
    "rfcReceptor",
    "sello",
    "serie",
    "total1",
    "total2",
    "uuid",
    "uuidOriginal",
    "validate",
    "version",
    "xml",
    "xmlAcuseCancelacionCfdi"
})
public class RespuestaXml {

    @XmlElement(name = "CadenaOriginal", nillable = true)
    protected String cadenaOriginal;
    @XmlElement(name = "DomicilioFiscalReceptor", nillable = true)
    protected String domicilioFiscalReceptor;
    @XmlElement(name = "ErrorDesc", nillable = true)
    protected String errorDesc;
    @XmlElement(name = "ErrorMessage", nillable = true)
    protected String errorMessage;
    @XmlElement(name = "EstatusId", nillable = true)
    protected String estatusId;
    @XmlElement(name = "Exportacion", nillable = true)
    protected String exportacion;
    @XmlElement(name = "FacAtrAdquirente", nillable = true)
    protected String facAtrAdquirente;
    @XmlElement(name = "FacturaId", nillable = true)
    protected String facturaId;
    @XmlElement(name = "FacturaIdOriginal", nillable = true)
    protected String facturaIdOriginal;
    @XmlElement(name = "Folio", nillable = true)
    protected String folio;
    @XmlElement(name = "MontoOperacion", nillable = true)
    protected String montoOperacion;
    @XmlElement(name = "MontoRetencion", nillable = true)
    protected String montoRetencion;
    @XmlElement(name = "Qr", nillable = true)
    protected String qr;
    @XmlElement(name = "RechazoId", nillable = true)
    protected String rechazoId;
    @XmlElement(name = "RegimenFiscalReceptor", nillable = true)
    protected String regimenFiscalReceptor;
    @XmlElement(name = "RfcEmisor", nillable = true)
    protected String rfcEmisor;
    @XmlElement(name = "RfcReceptor", nillable = true)
    protected String rfcReceptor;
    @XmlElement(name = "Sello", nillable = true)
    protected String sello;
    @XmlElement(name = "Serie", nillable = true)
    protected String serie;
    @XmlElement(name = "Total1", nillable = true)
    protected String total1;
    @XmlElement(name = "Total2", nillable = true)
    protected String total2;
    @XmlElement(name = "Uuid", nillable = true)
    protected String uuid;
    @XmlElement(name = "UuidOriginal", nillable = true)
    protected String uuidOriginal;
    @XmlElement(name = "Validate")
    protected Boolean validate;
    @XmlElement(name = "Version", nillable = true)
    protected String version;
    @XmlElement(name = "Xml", nillable = true)
    protected String xml;
    @XmlElement(name = "XmlAcuseCancelacionCfdi", nillable = true)
    protected String xmlAcuseCancelacionCfdi;

    /**
     * Obtiene el valor de la propiedad cadenaOriginal.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCadenaOriginal() {
        return cadenaOriginal;
    }

    /**
     * Define el valor de la propiedad cadenaOriginal.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCadenaOriginal(String value) {
        this.cadenaOriginal = value;
    }

    /**
     * Obtiene el valor de la propiedad domicilioFiscalReceptor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomicilioFiscalReceptor() {
        return domicilioFiscalReceptor;
    }

    /**
     * Define el valor de la propiedad domicilioFiscalReceptor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomicilioFiscalReceptor(String value) {
        this.domicilioFiscalReceptor = value;
    }

    /**
     * Obtiene el valor de la propiedad errorDesc.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorDesc() {
        return errorDesc;
    }

    /**
     * Define el valor de la propiedad errorDesc.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorDesc(String value) {
        this.errorDesc = value;
    }

    /**
     * Obtiene el valor de la propiedad errorMessage.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Define el valor de la propiedad errorMessage.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Obtiene el valor de la propiedad estatusId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEstatusId() {
        return estatusId;
    }

    /**
     * Define el valor de la propiedad estatusId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEstatusId(String value) {
        this.estatusId = value;
    }

    /**
     * Obtiene el valor de la propiedad exportacion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExportacion() {
        return exportacion;
    }

    /**
     * Define el valor de la propiedad exportacion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExportacion(String value) {
        this.exportacion = value;
    }

    /**
     * Obtiene el valor de la propiedad facAtrAdquirente.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFacAtrAdquirente() {
        return facAtrAdquirente;
    }

    /**
     * Define el valor de la propiedad facAtrAdquirente.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFacAtrAdquirente(String value) {
        this.facAtrAdquirente = value;
    }

    /**
     * Obtiene el valor de la propiedad facturaId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFacturaId() {
        return facturaId;
    }

    /**
     * Define el valor de la propiedad facturaId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFacturaId(String value) {
        this.facturaId = value;
    }

    /**
     * Obtiene el valor de la propiedad facturaIdOriginal.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFacturaIdOriginal() {
        return facturaIdOriginal;
    }

    /**
     * Define el valor de la propiedad facturaIdOriginal.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFacturaIdOriginal(String value) {
        this.facturaIdOriginal = value;
    }

    /**
     * Obtiene el valor de la propiedad folio.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFolio() {
        return folio;
    }

    /**
     * Define el valor de la propiedad folio.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFolio(String value) {
        this.folio = value;
    }

    /**
     * Obtiene el valor de la propiedad montoOperacion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMontoOperacion() {
        return montoOperacion;
    }

    /**
     * Define el valor de la propiedad montoOperacion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMontoOperacion(String value) {
        this.montoOperacion = value;
    }

    /**
     * Obtiene el valor de la propiedad montoRetencion.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMontoRetencion() {
        return montoRetencion;
    }

    /**
     * Define el valor de la propiedad montoRetencion.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMontoRetencion(String value) {
        this.montoRetencion = value;
    }

    /**
     * Obtiene el valor de la propiedad qr.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQr() {
        return qr;
    }

    /**
     * Define el valor de la propiedad qr.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQr(String value) {
        this.qr = value;
    }

    /**
     * Obtiene el valor de la propiedad rechazoId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRechazoId() {
        return rechazoId;
    }

    /**
     * Define el valor de la propiedad rechazoId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRechazoId(String value) {
        this.rechazoId = value;
    }

    /**
     * Obtiene el valor de la propiedad regimenFiscalReceptor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRegimenFiscalReceptor() {
        return regimenFiscalReceptor;
    }

    /**
     * Define el valor de la propiedad regimenFiscalReceptor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRegimenFiscalReceptor(String value) {
        this.regimenFiscalReceptor = value;
    }

    /**
     * Obtiene el valor de la propiedad rfcEmisor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRfcEmisor() {
        return rfcEmisor;
    }

    /**
     * Define el valor de la propiedad rfcEmisor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRfcEmisor(String value) {
        this.rfcEmisor = value;
    }

    /**
     * Obtiene el valor de la propiedad rfcReceptor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRfcReceptor() {
        return rfcReceptor;
    }

    /**
     * Define el valor de la propiedad rfcReceptor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRfcReceptor(String value) {
        this.rfcReceptor = value;
    }

    /**
     * Obtiene el valor de la propiedad sello.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSello() {
        return sello;
    }

    /**
     * Define el valor de la propiedad sello.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSello(String value) {
        this.sello = value;
    }

    /**
     * Obtiene el valor de la propiedad serie.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSerie() {
        return serie;
    }

    /**
     * Define el valor de la propiedad serie.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSerie(String value) {
        this.serie = value;
    }

    /**
     * Obtiene el valor de la propiedad total1.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotal1() {
        return total1;
    }

    /**
     * Define el valor de la propiedad total1.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotal1(String value) {
        this.total1 = value;
    }

    /**
     * Obtiene el valor de la propiedad total2.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotal2() {
        return total2;
    }

    /**
     * Define el valor de la propiedad total2.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotal2(String value) {
        this.total2 = value;
    }

    /**
     * Obtiene el valor de la propiedad uuid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Define el valor de la propiedad uuid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Obtiene el valor de la propiedad uuidOriginal.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUuidOriginal() {
        return uuidOriginal;
    }

    /**
     * Define el valor de la propiedad uuidOriginal.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUuidOriginal(String value) {
        this.uuidOriginal = value;
    }

    /**
     * Obtiene el valor de la propiedad validate.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isValidate() {
        return validate;
    }

    /**
     * Define el valor de la propiedad validate.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setValidate(Boolean value) {
        this.validate = value;
    }

    /**
     * Obtiene el valor de la propiedad version.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Define el valor de la propiedad version.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Obtiene el valor de la propiedad xml.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXml() {
        return xml;
    }

    /**
     * Define el valor de la propiedad xml.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXml(String value) {
        this.xml = value;
    }

    /**
     * Obtiene el valor de la propiedad xmlAcuseCancelacionCfdi.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlAcuseCancelacionCfdi() {
        return xmlAcuseCancelacionCfdi;
    }

    /**
     * Define el valor de la propiedad xmlAcuseCancelacionCfdi.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlAcuseCancelacionCfdi(String value) {
        this.xmlAcuseCancelacionCfdi = value;
    }

}

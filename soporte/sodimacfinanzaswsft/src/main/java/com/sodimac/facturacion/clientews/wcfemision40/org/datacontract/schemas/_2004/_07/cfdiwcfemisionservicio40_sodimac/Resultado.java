
package com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para resultado complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="resultado"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CadenaOriginal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreditoActual" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreditoDisponible" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreditoFinal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Emisor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorDesc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorModule" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="FacturaId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Folio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="PDF64" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Receptor" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Serie" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Uuid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Validate" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultado", propOrder = {
    "cadenaOriginal",
    "creditoActual",
    "creditoDisponible",
    "creditoFinal",
    "emisor",
    "errorCode",
    "errorDesc",
    "errorMessage",
    "errorModule",
    "errorType",
    "facturaId",
    "folio",
    "pdf64",
    "receptor",
    "serie",
    "uuid",
    "validate",
    "version"
})
public class Resultado {

    @XmlElement(name = "CadenaOriginal", nillable = true)
    protected String cadenaOriginal;
    @XmlElement(name = "CreditoActual", nillable = true)
    protected String creditoActual;
    @XmlElement(name = "CreditoDisponible", nillable = true)
    protected String creditoDisponible;
    @XmlElement(name = "CreditoFinal", nillable = true)
    protected String creditoFinal;
    @XmlElement(name = "Emisor", nillable = true)
    protected String emisor;
    @XmlElement(name = "ErrorCode", nillable = true)
    protected String errorCode;
    @XmlElement(name = "ErrorDesc", nillable = true)
    protected String errorDesc;
    @XmlElement(name = "ErrorMessage", nillable = true)
    protected String errorMessage;
    @XmlElement(name = "ErrorModule", nillable = true)
    protected String errorModule;
    @XmlElement(name = "ErrorType", nillable = true)
    protected String errorType;
    @XmlElement(name = "FacturaId", nillable = true)
    protected String facturaId;
    @XmlElement(name = "Folio", nillable = true)
    protected String folio;
    @XmlElement(name = "PDF64", nillable = true)
    protected String pdf64;
    @XmlElement(name = "Receptor", nillable = true)
    protected String receptor;
    @XmlElement(name = "Serie", nillable = true)
    protected String serie;
    @XmlElement(name = "Uuid", nillable = true)
    protected String uuid;
    @XmlElement(name = "Validate")
    protected Boolean validate;
    @XmlElement(name = "Version", nillable = true)
    protected String version;

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
     * Obtiene el valor de la propiedad creditoActual.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreditoActual() {
        return creditoActual;
    }

    /**
     * Define el valor de la propiedad creditoActual.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreditoActual(String value) {
        this.creditoActual = value;
    }

    /**
     * Obtiene el valor de la propiedad creditoDisponible.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreditoDisponible() {
        return creditoDisponible;
    }

    /**
     * Define el valor de la propiedad creditoDisponible.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreditoDisponible(String value) {
        this.creditoDisponible = value;
    }

    /**
     * Obtiene el valor de la propiedad creditoFinal.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreditoFinal() {
        return creditoFinal;
    }

    /**
     * Define el valor de la propiedad creditoFinal.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreditoFinal(String value) {
        this.creditoFinal = value;
    }

    /**
     * Obtiene el valor de la propiedad emisor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmisor() {
        return emisor;
    }

    /**
     * Define el valor de la propiedad emisor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmisor(String value) {
        this.emisor = value;
    }

    /**
     * Obtiene el valor de la propiedad errorCode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Define el valor de la propiedad errorCode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorCode(String value) {
        this.errorCode = value;
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
     * Obtiene el valor de la propiedad errorModule.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorModule() {
        return errorModule;
    }

    /**
     * Define el valor de la propiedad errorModule.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorModule(String value) {
        this.errorModule = value;
    }

    /**
     * Obtiene el valor de la propiedad errorType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Define el valor de la propiedad errorType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorType(String value) {
        this.errorType = value;
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
     * Obtiene el valor de la propiedad pdf64.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPDF64() {
        return pdf64;
    }

    /**
     * Define el valor de la propiedad pdf64.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPDF64(String value) {
        this.pdf64 = value;
    }

    /**
     * Obtiene el valor de la propiedad receptor.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceptor() {
        return receptor;
    }

    /**
     * Define el valor de la propiedad receptor.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceptor(String value) {
        this.receptor = value;
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

}

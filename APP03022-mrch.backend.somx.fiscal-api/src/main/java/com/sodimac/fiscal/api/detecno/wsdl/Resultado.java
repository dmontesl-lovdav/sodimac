
package com.sodimac.fiscal.api.detecno.wsdl;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Resultado complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Resultado"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CreditoActual" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreditoDisponible" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreditoFinal" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CreditoInicio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorDesc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorModule" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ErrorType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Validate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Resultado", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", propOrder = {
    "creditoActual",
    "creditoDisponible",
    "creditoFinal",
    "creditoInicio",
    "errorCode",
    "errorDesc",
    "errorMessage",
    "errorModule",
    "errorType",
    "status",
    "validate"
})
public class Resultado {

    @XmlElementRef(name = "CreditoActual", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> creditoActual;
    @XmlElementRef(name = "CreditoDisponible", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> creditoDisponible;
    @XmlElementRef(name = "CreditoFinal", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> creditoFinal;
    @XmlElementRef(name = "CreditoInicio", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> creditoInicio;
    @XmlElementRef(name = "ErrorCode", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> errorCode;
    @XmlElementRef(name = "ErrorDesc", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> errorDesc;
    @XmlElementRef(name = "ErrorMessage", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> errorMessage;
    @XmlElementRef(name = "ErrorModule", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> errorModule;
    @XmlElementRef(name = "ErrorType", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> errorType;
    @XmlElementRef(name = "Status", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> status;
    @XmlElementRef(name = "Validate", namespace = "http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio", type = JAXBElement.class, required = false)
    protected JAXBElement<String> validate;

    /**
     * Obtiene el valor de la propiedad creditoActual.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCreditoActual() {
        return creditoActual;
    }

    /**
     * Define el valor de la propiedad creditoActual.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCreditoActual(JAXBElement<String> value) {
        this.creditoActual = value;
    }

    /**
     * Obtiene el valor de la propiedad creditoDisponible.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCreditoDisponible() {
        return creditoDisponible;
    }

    /**
     * Define el valor de la propiedad creditoDisponible.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCreditoDisponible(JAXBElement<String> value) {
        this.creditoDisponible = value;
    }

    /**
     * Obtiene el valor de la propiedad creditoFinal.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCreditoFinal() {
        return creditoFinal;
    }

    /**
     * Define el valor de la propiedad creditoFinal.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCreditoFinal(JAXBElement<String> value) {
        this.creditoFinal = value;
    }

    /**
     * Obtiene el valor de la propiedad creditoInicio.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCreditoInicio() {
        return creditoInicio;
    }

    /**
     * Define el valor de la propiedad creditoInicio.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCreditoInicio(JAXBElement<String> value) {
        this.creditoInicio = value;
    }

    /**
     * Obtiene el valor de la propiedad errorCode.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getErrorCode() {
        return errorCode;
    }

    /**
     * Define el valor de la propiedad errorCode.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setErrorCode(JAXBElement<String> value) {
        this.errorCode = value;
    }

    /**
     * Obtiene el valor de la propiedad errorDesc.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getErrorDesc() {
        return errorDesc;
    }

    /**
     * Define el valor de la propiedad errorDesc.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setErrorDesc(JAXBElement<String> value) {
        this.errorDesc = value;
    }

    /**
     * Obtiene el valor de la propiedad errorMessage.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Define el valor de la propiedad errorMessage.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setErrorMessage(JAXBElement<String> value) {
        this.errorMessage = value;
    }

    /**
     * Obtiene el valor de la propiedad errorModule.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getErrorModule() {
        return errorModule;
    }

    /**
     * Define el valor de la propiedad errorModule.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setErrorModule(JAXBElement<String> value) {
        this.errorModule = value;
    }

    /**
     * Obtiene el valor de la propiedad errorType.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getErrorType() {
        return errorType;
    }

    /**
     * Define el valor de la propiedad errorType.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setErrorType(JAXBElement<String> value) {
        this.errorType = value;
    }

    /**
     * Obtiene el valor de la propiedad status.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStatus() {
        return status;
    }

    /**
     * Define el valor de la propiedad status.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStatus(JAXBElement<String> value) {
        this.status = value;
    }

    /**
     * Obtiene el valor de la propiedad validate.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getValidate() {
        return validate;
    }

    /**
     * Define el valor de la propiedad validate.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setValidate(JAXBElement<String> value) {
        this.validate = value;
    }

	@Override
	public String toString() {
		return "Resultado [creditoActual=" + creditoActual.getValue() + ", creditoDisponible=" + creditoDisponible.getValue()
				+ ", creditoFinal=" + creditoFinal.getValue() + ", creditoInicio=" + creditoInicio.getValue() + ", errorCode=" + errorCode.getValue()
				+ ", errorDesc=" + errorDesc.getValue() + ", errorMessage=" + errorMessage.getValue() + ", errorModule=" + errorModule.getValue()
				+ ", errorType=" + errorType.getValue() + ", status=" + status.getValue() + ", validate=" + validate.getValue() + "]";
	}

    
    
}

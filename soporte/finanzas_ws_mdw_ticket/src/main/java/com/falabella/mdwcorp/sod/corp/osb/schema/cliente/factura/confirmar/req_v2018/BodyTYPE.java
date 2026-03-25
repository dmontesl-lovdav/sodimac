
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para body_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="body_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="comprobanteFiscalDigital">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="36"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="comprobantes" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Factura/Confirmar/Req-v2018.02}comprobantes_TYPE"/>
 *         &lt;element name="codigoError" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="detalleError" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "body_TYPE", propOrder = {
    "comprobanteFiscalDigital",
    "comprobantes",
    "codigoError",
    "detalleError"
})
public class BodyTYPE {

    @XmlElement(required = true)
    protected String comprobanteFiscalDigital;
    @XmlElement(required = true)
    protected ComprobantesTYPE comprobantes;
    @XmlElement(required = true)
    protected String codigoError;
    @XmlElement(required = true)
    protected String detalleError;

    /**
     * Obtiene el valor de la propiedad comprobanteFiscalDigital.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComprobanteFiscalDigital() {
        return comprobanteFiscalDigital;
    }

    /**
     * Define el valor de la propiedad comprobanteFiscalDigital.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComprobanteFiscalDigital(String value) {
        this.comprobanteFiscalDigital = value;
    }

    /**
     * Obtiene el valor de la propiedad comprobantes.
     * 
     * @return
     *     possible object is
     *     {@link ComprobantesTYPE }
     *     
     */
    public ComprobantesTYPE getComprobantes() {
        return comprobantes;
    }

    /**
     * Define el valor de la propiedad comprobantes.
     * 
     * @param value
     *     allowed object is
     *     {@link ComprobantesTYPE }
     *     
     */
    public void setComprobantes(ComprobantesTYPE value) {
        this.comprobantes = value;
    }

    /**
     * Obtiene el valor de la propiedad codigoError.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodigoError() {
        return codigoError;
    }

    /**
     * Define el valor de la propiedad codigoError.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodigoError(String value) {
        this.codigoError = value;
    }

    /**
     * Obtiene el valor de la propiedad detalleError.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetalleError() {
        return detalleError;
    }

    /**
     * Define el valor de la propiedad detalleError.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetalleError(String value) {
        this.detalleError = value;
    }

}

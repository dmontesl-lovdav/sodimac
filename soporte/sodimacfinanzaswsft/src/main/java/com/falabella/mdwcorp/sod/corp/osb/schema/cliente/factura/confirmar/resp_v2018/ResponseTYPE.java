
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para response_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="response_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codigoRespuesta" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="mensajeRespuesta">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="100"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "response_TYPE", propOrder = {
    "codigoRespuesta",
    "mensajeRespuesta"
})
public class ResponseTYPE {

    @XmlElement(required = true)
    protected BigInteger codigoRespuesta;
    @XmlElement(required = true)
    protected String mensajeRespuesta;

    /**
     * Obtiene el valor de la propiedad codigoRespuesta.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getCodigoRespuesta() {
        return codigoRespuesta;
    }

    /**
     * Define el valor de la propiedad codigoRespuesta.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setCodigoRespuesta(BigInteger value) {
        this.codigoRespuesta = value;
    }

    /**
     * Obtiene el valor de la propiedad mensajeRespuesta.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMensajeRespuesta() {
        return mensajeRespuesta;
    }

    /**
     * Define el valor de la propiedad mensajeRespuesta.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMensajeRespuesta(String value) {
        this.mensajeRespuesta = value;
    }

}

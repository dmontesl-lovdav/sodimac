
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para comprobante_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="comprobante_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="numeroTicket">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;maxLength value="30"/>
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
@XmlType(name = "comprobante_TYPE", propOrder = {
    "numeroTicket"
})
public class ComprobanteTYPE {

    @XmlElement(required = true)
    protected String numeroTicket;

    /**
     * Obtiene el valor de la propiedad numeroTicket.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumeroTicket() {
        return numeroTicket;
    }

    /**
     * Define el valor de la propiedad numeroTicket.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumeroTicket(String value) {
        this.numeroTicket = value;
    }

}

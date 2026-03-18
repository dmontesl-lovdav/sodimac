
package com.sodimac.facturacion.clientews.wcfemision40.org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="licencia" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="facturaId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "licencia",
    "facturaId"
})
@XmlRootElement(name = "ComprobanteBuscar40")
public class ComprobanteBuscar40 {

    @XmlElement(nillable = true)
    protected String licencia;
    @XmlElement(nillable = true)
    protected String facturaId;

    /**
     * Obtiene el valor de la propiedad licencia.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicencia() {
        return licencia;
    }

    /**
     * Define el valor de la propiedad licencia.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicencia(String value) {
        this.licencia = value;
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

}

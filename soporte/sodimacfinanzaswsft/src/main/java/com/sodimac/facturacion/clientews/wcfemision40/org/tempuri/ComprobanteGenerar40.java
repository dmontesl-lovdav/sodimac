
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
 *         &lt;element name="cerBytes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="keyBytes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="passBytes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="xml" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
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
    "cerBytes",
    "keyBytes",
    "passBytes",
    "xml"
})
@XmlRootElement(name = "ComprobanteGenerar40")
public class ComprobanteGenerar40 {

    @XmlElement(nillable = true)
    protected String licencia;
    @XmlElement(nillable = true)
    protected String cerBytes;
    @XmlElement(nillable = true)
    protected String keyBytes;
    @XmlElement(nillable = true)
    protected String passBytes;
    @XmlElement(nillable = true)
    protected String xml;

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
     * Obtiene el valor de la propiedad cerBytes.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCerBytes() {
        return cerBytes;
    }

    /**
     * Define el valor de la propiedad cerBytes.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCerBytes(String value) {
        this.cerBytes = value;
    }

    /**
     * Obtiene el valor de la propiedad keyBytes.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyBytes() {
        return keyBytes;
    }

    /**
     * Define el valor de la propiedad keyBytes.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyBytes(String value) {
        this.keyBytes = value;
    }

    /**
     * Obtiene el valor de la propiedad passBytes.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassBytes() {
        return passBytes;
    }

    /**
     * Define el valor de la propiedad passBytes.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassBytes(String value) {
        this.passBytes = value;
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

}

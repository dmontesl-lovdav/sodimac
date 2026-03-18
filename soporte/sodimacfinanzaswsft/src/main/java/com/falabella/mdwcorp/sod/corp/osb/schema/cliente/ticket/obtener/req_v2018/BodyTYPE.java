
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018;

import java.math.BigInteger;
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
 *         &lt;element name="i_num_oc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="i_num_ticket" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="i_num_tienda" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="i_num_caja" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fecha_trx" minOccurs="0">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;pattern value="[0-9]{2}-[0-9]{2}-[0-9]{4}"/>
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
@XmlType(name = "body_TYPE", propOrder = {
    "iNumOc",
    "iNumTicket",
    "iNumTienda",
    "iNumCaja",
    "fechaTrx"
})
public class BodyTYPE {

    @XmlElement(name = "i_num_oc")
    protected String iNumOc;
    @XmlElement(name = "i_num_ticket")
    protected BigInteger iNumTicket;
    @XmlElement(name = "i_num_tienda")
    protected String iNumTienda;
    @XmlElement(name = "i_num_caja")
    protected String iNumCaja;
    @XmlElement(name = "fecha_trx")
    protected String fechaTrx;

    /**
     * Obtiene el valor de la propiedad iNumOc.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getINumOc() {
        return iNumOc;
    }

    /**
     * Define el valor de la propiedad iNumOc.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setINumOc(String value) {
        this.iNumOc = value;
    }

    /**
     * Obtiene el valor de la propiedad iNumTicket.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getINumTicket() {
        return iNumTicket;
    }

    /**
     * Define el valor de la propiedad iNumTicket.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setINumTicket(BigInteger value) {
        this.iNumTicket = value;
    }

    /**
     * Obtiene el valor de la propiedad iNumTienda.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getINumTienda() {
        return iNumTienda;
    }

    /**
     * Define el valor de la propiedad iNumTienda.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setINumTienda(String value) {
        this.iNumTienda = value;
    }

    /**
     * Obtiene el valor de la propiedad iNumCaja.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getINumCaja() {
        return iNumCaja;
    }

    /**
     * Define el valor de la propiedad iNumCaja.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setINumCaja(String value) {
        this.iNumCaja = value;
    }

    /**
     * Obtiene el valor de la propiedad fechaTrx.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFechaTrx() {
        return fechaTrx;
    }

    /**
     * Define el valor de la propiedad fechaTrx.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFechaTrx(String value) {
        this.fechaTrx = value;
    }

}

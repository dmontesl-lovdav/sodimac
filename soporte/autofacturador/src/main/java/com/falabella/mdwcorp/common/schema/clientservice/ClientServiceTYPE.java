
package com.falabella.mdwcorp.common.schema.clientservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Clase Java para ClientService_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClientService_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="country" type="{http://mdwcorp.falabella.com/common/schema/clientservice}Country_TYPE"/>
 *         &lt;element name="commerce" type="{http://mdwcorp.falabella.com/common/schema/clientservice}Commerce_TYPE"/>
 *         &lt;element name="channel" type="{http://mdwcorp.falabella.com/common/schema/clientservice}Channel_TYPE"/>
 *         &lt;element name="storeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="terminalId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="hour" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClientService_TYPE", propOrder = {
    "country",
    "commerce",
    "channel",
    "storeId",
    "terminalId",
    "date",
    "hour"
})
public class ClientServiceTYPE {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected CountryTYPE country;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected CommerceTYPE commerce;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ChannelTYPE channel;
    protected String storeId;
    protected String terminalId;
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar date;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar hour;

    /**
     * Obtiene el valor de la propiedad country.
     * 
     * @return
     *     possible object is
     *     {@link CountryTYPE }
     *     
     */
    public CountryTYPE getCountry() {
        return country;
    }

    /**
     * Define el valor de la propiedad country.
     * 
     * @param value
     *     allowed object is
     *     {@link CountryTYPE }
     *     
     */
    public void setCountry(CountryTYPE value) {
        this.country = value;
    }

    /**
     * Obtiene el valor de la propiedad commerce.
     * 
     * @return
     *     possible object is
     *     {@link CommerceTYPE }
     *     
     */
    public CommerceTYPE getCommerce() {
        return commerce;
    }

    /**
     * Define el valor de la propiedad commerce.
     * 
     * @param value
     *     allowed object is
     *     {@link CommerceTYPE }
     *     
     */
    public void setCommerce(CommerceTYPE value) {
        this.commerce = value;
    }

    /**
     * Obtiene el valor de la propiedad channel.
     * 
     * @return
     *     possible object is
     *     {@link ChannelTYPE }
     *     
     */
    public ChannelTYPE getChannel() {
        return channel;
    }

    /**
     * Define el valor de la propiedad channel.
     * 
     * @param value
     *     allowed object is
     *     {@link ChannelTYPE }
     *     
     */
    public void setChannel(ChannelTYPE value) {
        this.channel = value;
    }

    /**
     * Obtiene el valor de la propiedad storeId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStoreId() {
        return storeId;
    }

    /**
     * Define el valor de la propiedad storeId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStoreId(String value) {
        this.storeId = value;
    }

    /**
     * Obtiene el valor de la propiedad terminalId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerminalId() {
        return terminalId;
    }

    /**
     * Define el valor de la propiedad terminalId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminalId(String value) {
        this.terminalId = value;
    }

    /**
     * Obtiene el valor de la propiedad date.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDate() {
        return date;
    }

    /**
     * Define el valor de la propiedad date.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(XMLGregorianCalendar value) {
        this.date = value;
    }

    /**
     * Obtiene el valor de la propiedad hour.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getHour() {
        return hour;
    }

    /**
     * Define el valor de la propiedad hour.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setHour(XMLGregorianCalendar value) {
        this.hour = value;
    }

}

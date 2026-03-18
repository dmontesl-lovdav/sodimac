
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.req_v2018;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ClienteTicketObtenerExpReq_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClienteTicketObtenerExpReq_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="body" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Req-v2018.02}body_TYPE" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClienteTicketObtenerExpReq_TYPE", propOrder = {
    "body"
})
public class ClienteTicketObtenerExpReqTYPE {

    protected BodyTYPE body;

    /**
     * Obtiene el valor de la propiedad body.
     * 
     * @return
     *     possible object is
     *     {@link BodyTYPE }
     *     
     */
    public BodyTYPE getBody() {
        return body;
    }

    /**
     * Define el valor de la propiedad body.
     * 
     * @param value
     *     allowed object is
     *     {@link BodyTYPE }
     *     
     */
    public void setBody(BodyTYPE value) {
        this.body = value;
    }

}

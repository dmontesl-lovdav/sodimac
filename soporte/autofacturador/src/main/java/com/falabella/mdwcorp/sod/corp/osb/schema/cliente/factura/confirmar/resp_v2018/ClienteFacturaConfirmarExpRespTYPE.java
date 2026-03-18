
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ClienteFacturaConfirmarExpResp_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClienteFacturaConfirmarExpResp_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="response" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Factura/Confirmar/Resp-v2018.02}response_TYPE"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClienteFacturaConfirmarExpResp_TYPE", propOrder = {
    "response"
})
public class ClienteFacturaConfirmarExpRespTYPE {

    @XmlElement(required = true)
    protected ResponseTYPE response;

    /**
     * Obtiene el valor de la propiedad response.
     * 
     * @return
     *     possible object is
     *     {@link ResponseTYPE }
     *     
     */
    public ResponseTYPE getResponse() {
        return response;
    }

    /**
     * Define el valor de la propiedad response.
     * 
     * @param value
     *     allowed object is
     *     {@link ResponseTYPE }
     *     
     */
    public void setResponse(ResponseTYPE value) {
        this.response = value;
    }

}

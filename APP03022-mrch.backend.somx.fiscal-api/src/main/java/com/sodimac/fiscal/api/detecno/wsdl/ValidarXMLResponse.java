
package com.sodimac.fiscal.api.detecno.wsdl;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


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
 *         &lt;element name="validarXMLResult" type="{http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio}Resultado" minOccurs="0"/&gt;
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
    "validarXMLResult"
})
@XmlRootElement(name = "validarXMLResponse")
public class ValidarXMLResponse {

    @XmlElementRef(name = "validarXMLResult", namespace = "http://tempuri.org/", type = JAXBElement.class, required = false)
    protected JAXBElement<Resultado> validarXMLResult;

    /**
     * Obtiene el valor de la propiedad validarXMLResult.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Resultado }{@code >}
     *     
     */
    public JAXBElement<Resultado> getValidarXMLResult() {
        return validarXMLResult;
    }

    /**
     * Define el valor de la propiedad validarXMLResult.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Resultado }{@code >}
     *     
     */
    public void setValidarXMLResult(JAXBElement<Resultado> value) {
        this.validarXMLResult = value;
    }

}

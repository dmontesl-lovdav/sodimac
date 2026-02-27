
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
 *         &lt;element name="TestBDResult" type="{http://schemas.datacontract.org/2004/07/cfdiWcfRecepcion_Servicio}Resultado" minOccurs="0"/&gt;
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
    "testBDResult"
})
@XmlRootElement(name = "TestBDResponse")
public class TestBDResponse {

    @XmlElementRef(name = "TestBDResult", namespace = "http://tempuri.org/", type = JAXBElement.class, required = false)
    protected JAXBElement<Resultado> testBDResult;

    /**
     * Obtiene el valor de la propiedad testBDResult.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Resultado }{@code >}
     *     
     */
    public JAXBElement<Resultado> getTestBDResult() {
        return testBDResult;
    }

    /**
     * Define el valor de la propiedad testBDResult.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Resultado }{@code >}
     *     
     */
    public void setTestBDResult(JAXBElement<Resultado> value) {
        this.testBDResult = value;
    }

}

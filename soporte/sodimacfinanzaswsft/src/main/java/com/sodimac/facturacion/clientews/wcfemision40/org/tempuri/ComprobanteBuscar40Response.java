
package com.sodimac.facturacion.clientews.wcfemision40.org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.RespuestaXml;


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
 *         &lt;element name="ComprobanteBuscar40Result" type="{http://schemas.datacontract.org/2004/07/cfdiWcfEmisionServicio40_Sodimac.Clases}respuestaXml" minOccurs="0"/&gt;
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
    "comprobanteBuscar40Result"
})
@XmlRootElement(name = "ComprobanteBuscar40Response")
public class ComprobanteBuscar40Response {

    @XmlElement(name = "ComprobanteBuscar40Result", nillable = true)
    protected RespuestaXml comprobanteBuscar40Result;

    /**
     * Obtiene el valor de la propiedad comprobanteBuscar40Result.
     * 
     * @return
     *     possible object is
     *     {@link RespuestaXml }
     *     
     */
    public RespuestaXml getComprobanteBuscar40Result() {
        return comprobanteBuscar40Result;
    }

    /**
     * Define el valor de la propiedad comprobanteBuscar40Result.
     * 
     * @param value
     *     allowed object is
     *     {@link RespuestaXml }
     *     
     */
    public void setComprobanteBuscar40Result(RespuestaXml value) {
        this.comprobanteBuscar40Result = value;
    }

}

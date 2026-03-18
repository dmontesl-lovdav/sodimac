
package com.sodimac.facturacion.clientews.wcfemision40.org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.sodimac.facturacion.clientews.wcfemision40.org.datacontract.schemas._2004._07.cfdiwcfemisionservicio40_sodimac.Resultado;


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
 *         &lt;element name="ComprobanteCancelar40Result" type="{http://schemas.datacontract.org/2004/07/cfdiWcfEmisionServicio40_Sodimac.Clases}resultado" minOccurs="0"/&gt;
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
    "comprobanteCancelar40Result"
})
@XmlRootElement(name = "ComprobanteCancelar40Response")
public class ComprobanteCancelar40Response {

    @XmlElement(name = "ComprobanteCancelar40Result", nillable = true)
    protected Resultado comprobanteCancelar40Result;

    /**
     * Obtiene el valor de la propiedad comprobanteCancelar40Result.
     * 
     * @return
     *     possible object is
     *     {@link Resultado }
     *     
     */
    public Resultado getComprobanteCancelar40Result() {
        return comprobanteCancelar40Result;
    }

    /**
     * Define el valor de la propiedad comprobanteCancelar40Result.
     * 
     * @param value
     *     allowed object is
     *     {@link Resultado }
     *     
     */
    public void setComprobanteCancelar40Result(Resultado value) {
        this.comprobanteCancelar40Result = value;
    }

}

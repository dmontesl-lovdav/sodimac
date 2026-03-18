
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
 *         &lt;element name="ComprobanteGenerar40Result" type="{http://schemas.datacontract.org/2004/07/cfdiWcfEmisionServicio40_Sodimac.Clases}resultado" minOccurs="0"/&gt;
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
    "comprobanteGenerar40Result"
})
@XmlRootElement(name = "ComprobanteGenerar40Response")
public class ComprobanteGenerar40Response {

    @XmlElement(name = "ComprobanteGenerar40Result", nillable = true)
    protected Resultado comprobanteGenerar40Result;

    /**
     * Obtiene el valor de la propiedad comprobanteGenerar40Result.
     * 
     * @return
     *     possible object is
     *     {@link Resultado }
     *     
     */
    public Resultado getComprobanteGenerar40Result() {
        return comprobanteGenerar40Result;
    }

    /**
     * Define el valor de la propiedad comprobanteGenerar40Result.
     * 
     * @param value
     *     allowed object is
     *     {@link Resultado }
     *     
     */
    public void setComprobanteGenerar40Result(Resultado value) {
        this.comprobanteGenerar40Result = value;
    }

}

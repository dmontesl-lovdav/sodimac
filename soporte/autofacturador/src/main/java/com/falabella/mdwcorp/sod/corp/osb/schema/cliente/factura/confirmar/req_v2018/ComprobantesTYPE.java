
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.req_v2018;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para comprobantes_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="comprobantes_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="comprobante" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Factura/Confirmar/Req-v2018.02}comprobante_TYPE" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "comprobantes_TYPE", propOrder = {
    "comprobante"
})
public class ComprobantesTYPE {

    @XmlElement(required = true)
    protected List<ComprobanteTYPE> comprobante;

    /**
     * Gets the value of the comprobante property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comprobante property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComprobante().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ComprobanteTYPE }
     * 
     * 
     */
    public List<ComprobanteTYPE> getComprobante() {
        if (comprobante == null) {
            comprobante = new ArrayList<ComprobanteTYPE>();
        }
        return this.comprobante;
    }
}

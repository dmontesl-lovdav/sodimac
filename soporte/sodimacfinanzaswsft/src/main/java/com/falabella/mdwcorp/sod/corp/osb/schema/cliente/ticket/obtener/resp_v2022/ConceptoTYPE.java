
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Concepto_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Concepto_TYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Impuestos" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2022.01}Concepto_impuestos_TYPE" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Concepto_TYPE", propOrder = {
    "impuestos"
})
@XmlSeeAlso({
    com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022.ConceptosTYPE.Concepto.class
})
public class ConceptoTYPE {

    @XmlElement(name = "Impuestos")
    protected ConceptoImpuestosTYPE impuestos;

    /**
     * Obtiene el valor de la propiedad impuestos.
     * 
     * @return
     *     possible object is
     *     {@link ConceptoImpuestosTYPE }
     *     
     */
    public ConceptoImpuestosTYPE getImpuestos() {
        return impuestos;
    }

    /**
     * Define el valor de la propiedad impuestos.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptoImpuestosTYPE }
     *     
     */
    public void setImpuestos(ConceptoImpuestosTYPE value) {
        this.impuestos = value;
    }

}

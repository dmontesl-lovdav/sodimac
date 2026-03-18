
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Concepto_impuestos_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Concepto_impuestos_TYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Traslados" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2022.01}Concepto_impuestos_Traslados_TYPE"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Concepto_impuestos_TYPE", propOrder = {
    "traslados"
})
public class ConceptoImpuestosTYPE {

    @XmlElement(name = "Traslados", required = true)
    protected ConceptoImpuestosTrasladosTYPE traslados;

    /**
     * Obtiene el valor de la propiedad traslados.
     * 
     * @return
     *     possible object is
     *     {@link ConceptoImpuestosTrasladosTYPE }
     *     
     */
    public ConceptoImpuestosTrasladosTYPE getTraslados() {
        return traslados;
    }

    /**
     * Define el valor de la propiedad traslados.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptoImpuestosTrasladosTYPE }
     *     
     */
    public void setTraslados(ConceptoImpuestosTrasladosTYPE value) {
        this.traslados = value;
    }

}

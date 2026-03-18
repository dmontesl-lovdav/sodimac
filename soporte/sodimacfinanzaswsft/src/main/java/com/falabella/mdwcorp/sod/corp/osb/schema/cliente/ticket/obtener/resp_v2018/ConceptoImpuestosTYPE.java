
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018;

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
 * &lt;complexType name="Concepto_impuestos_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Traslados" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2018.02}Concepto_impuestos_Traslados_TYPE"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
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

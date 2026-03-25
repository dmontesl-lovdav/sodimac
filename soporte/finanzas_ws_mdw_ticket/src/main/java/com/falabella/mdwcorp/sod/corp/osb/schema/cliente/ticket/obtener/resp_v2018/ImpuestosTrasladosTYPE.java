
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Impuestos_traslados_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Impuestos_traslados_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Traslado" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="impuesto" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="tipoFactor" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="tasaCuota" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="importe" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="ordenador" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Impuestos_traslados_TYPE", propOrder = {
    "traslado"
})
public class ImpuestosTrasladosTYPE {

    @XmlElement(name = "Traslado", required = true)
    protected List<ImpuestosTrasladosTYPE.Traslado> traslado;

    /**
     * Gets the value of the traslado property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the traslado property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTraslado().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ImpuestosTrasladosTYPE.Traslado }
     * 
     * 
     */
    public List<ImpuestosTrasladosTYPE.Traslado> getTraslado() {
        if (traslado == null) {
            traslado = new ArrayList<ImpuestosTrasladosTYPE.Traslado>();
        }
        return this.traslado;
    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="impuesto" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="tipoFactor" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="tasaCuota" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="importe" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="ordenador" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Traslado {

        @XmlAttribute(name = "impuesto")
        @XmlSchemaType(name = "anySimpleType")
        protected String impuesto;
        @XmlAttribute(name = "tipoFactor")
        @XmlSchemaType(name = "anySimpleType")
        protected String tipoFactor;
        @XmlAttribute(name = "tasaCuota")
        @XmlSchemaType(name = "anySimpleType")
        protected String tasaCuota;
        @XmlAttribute(name = "importe")
        @XmlSchemaType(name = "anySimpleType")
        protected String importe;
        @XmlAttribute(name = "ordenador")
        @XmlSchemaType(name = "anySimpleType")
        protected String ordenador;

        /**
         * Obtiene el valor de la propiedad impuesto.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getImpuesto() {
            return impuesto;
        }

        /**
         * Define el valor de la propiedad impuesto.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setImpuesto(String value) {
            this.impuesto = value;
        }

        /**
         * Obtiene el valor de la propiedad tipoFactor.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTipoFactor() {
            return tipoFactor;
        }

        /**
         * Define el valor de la propiedad tipoFactor.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTipoFactor(String value) {
            this.tipoFactor = value;
        }

        /**
         * Obtiene el valor de la propiedad tasaCuota.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTasaCuota() {
            return tasaCuota;
        }

        /**
         * Define el valor de la propiedad tasaCuota.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTasaCuota(String value) {
            this.tasaCuota = value;
        }

        /**
         * Obtiene el valor de la propiedad importe.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getImporte() {
            return importe;
        }

        /**
         * Define el valor de la propiedad importe.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setImporte(String value) {
            this.importe = value;
        }

        /**
         * Obtiene el valor de la propiedad ordenador.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOrdenador() {
            return ordenador;
        }

        /**
         * Define el valor de la propiedad ordenador.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOrdenador(String value) {
            this.ordenador = value;
        }

    }

}

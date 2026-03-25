
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
 * <p>Clase Java para Conceptos_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Conceptos_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Concepto" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2018.02}Concepto_TYPE">
 *                 &lt;attribute name="claveProdServ" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="noIdentificacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="cantidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="claveUnidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="unidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="descripcion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="valorUnitario" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="importe" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="descuento" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="ordenador" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="padre" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="nivel" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/extension>
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
@XmlType(name = "Conceptos_TYPE", propOrder = {
    "concepto"
})
public class ConceptosTYPE {

    @XmlElement(name = "Concepto", required = true)
    protected List<ConceptosTYPE.Concepto> concepto;

    /**
     * Gets the value of the concepto property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the concepto property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConcepto().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConceptosTYPE.Concepto }
     * 
     * 
     */
    public List<ConceptosTYPE.Concepto> getConcepto() {
        if (concepto == null) {
            concepto = new ArrayList<ConceptosTYPE.Concepto>();
        }
        return this.concepto;
    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2018.02}Concepto_TYPE">
     *       &lt;attribute name="claveProdServ" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="noIdentificacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="cantidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="claveUnidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="unidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="descripcion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="valorUnitario" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="importe" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="descuento" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="ordenador" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="padre" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="nivel" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Concepto
        extends ConceptoTYPE
    {

        @XmlAttribute(name = "claveProdServ")
        @XmlSchemaType(name = "anySimpleType")
        protected String claveProdServ;
        @XmlAttribute(name = "noIdentificacion")
        @XmlSchemaType(name = "anySimpleType")
        protected String noIdentificacion;
        @XmlAttribute(name = "cantidad")
        @XmlSchemaType(name = "anySimpleType")
        protected String cantidad;
        @XmlAttribute(name = "claveUnidad")
        @XmlSchemaType(name = "anySimpleType")
        protected String claveUnidad;
        @XmlAttribute(name = "unidad")
        @XmlSchemaType(name = "anySimpleType")
        protected String unidad;
        @XmlAttribute(name = "descripcion")
        @XmlSchemaType(name = "anySimpleType")
        protected String descripcion;
        @XmlAttribute(name = "valorUnitario")
        @XmlSchemaType(name = "anySimpleType")
        protected String valorUnitario;
        @XmlAttribute(name = "importe")
        @XmlSchemaType(name = "anySimpleType")
        protected String importe;
        @XmlAttribute(name = "descuento")
        @XmlSchemaType(name = "anySimpleType")
        protected String descuento;
        @XmlAttribute(name = "ordenador")
        @XmlSchemaType(name = "anySimpleType")
        protected String ordenador;
        @XmlAttribute(name = "padre")
        @XmlSchemaType(name = "anySimpleType")
        protected String padre;
        @XmlAttribute(name = "nivel")
        @XmlSchemaType(name = "anySimpleType")
        protected String nivel;

        /**
         * Obtiene el valor de la propiedad claveProdServ.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getClaveProdServ() {
            return claveProdServ;
        }

        /**
         * Define el valor de la propiedad claveProdServ.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setClaveProdServ(String value) {
            this.claveProdServ = value;
        }

        /**
         * Obtiene el valor de la propiedad noIdentificacion.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNoIdentificacion() {
            return noIdentificacion;
        }

        /**
         * Define el valor de la propiedad noIdentificacion.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNoIdentificacion(String value) {
            this.noIdentificacion = value;
        }

        /**
         * Obtiene el valor de la propiedad cantidad.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCantidad() {
            return cantidad;
        }

        /**
         * Define el valor de la propiedad cantidad.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCantidad(String value) {
            this.cantidad = value;
        }

        /**
         * Obtiene el valor de la propiedad claveUnidad.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getClaveUnidad() {
            return claveUnidad;
        }

        /**
         * Define el valor de la propiedad claveUnidad.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setClaveUnidad(String value) {
            this.claveUnidad = value;
        }

        /**
         * Obtiene el valor de la propiedad unidad.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getUnidad() {
            return unidad;
        }

        /**
         * Define el valor de la propiedad unidad.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setUnidad(String value) {
            this.unidad = value;
        }

        /**
         * Obtiene el valor de la propiedad descripcion.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescripcion() {
            return descripcion;
        }

        /**
         * Define el valor de la propiedad descripcion.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescripcion(String value) {
            this.descripcion = value;
        }

        /**
         * Obtiene el valor de la propiedad valorUnitario.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValorUnitario() {
            return valorUnitario;
        }

        /**
         * Define el valor de la propiedad valorUnitario.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValorUnitario(String value) {
            this.valorUnitario = value;
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
         * Obtiene el valor de la propiedad descuento.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDescuento() {
            return descuento;
        }

        /**
         * Define el valor de la propiedad descuento.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDescuento(String value) {
            this.descuento = value;
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

        /**
         * Obtiene el valor de la propiedad padre.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPadre() {
            return padre;
        }

        /**
         * Define el valor de la propiedad padre.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPadre(String value) {
            this.padre = value;
        }

        /**
         * Obtiene el valor de la propiedad nivel.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNivel() {
            return nivel;
        }

        /**
         * Define el valor de la propiedad nivel.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNivel(String value) {
            this.nivel = value;
        }

    }

}

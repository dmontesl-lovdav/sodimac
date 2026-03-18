
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para Comprobante_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="Comprobante_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Emisor">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="rfc" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="nombre" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="regimenFiscal" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Conceptos" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2018.02}Conceptos_TYPE"/>
 *         &lt;element name="Impuestos" type="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2018.02}Impuestos_TYPE" minOccurs="0"/>
 *         &lt;element name="Totales">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="subTotal" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="descuento" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="moneda" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="tipoCambio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="total" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="importeLetra" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="totalImpuestosRetenidos" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="totalImpuestosTrasladados" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Control">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="cfdId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="estatusId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="estatusIdImpresion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="estatusIdCorreo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="estatusIdArchivo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="rechazoId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="complementoId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
@XmlType(name = "Comprobante_TYPE", propOrder = {
    "emisor",
    "conceptos",
    "impuestos",
    "totales",
    "control"
})
@XmlSeeAlso({
    com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE.Comprobante.class
})
public class ComprobanteTYPE {

    @XmlElement(name = "Emisor", required = true)
    protected ComprobanteTYPE.Emisor emisor;
    @XmlElement(name = "Conceptos", required = true)
    protected ConceptosTYPE conceptos;
    @XmlElement(name = "Impuestos")
    protected ImpuestosTYPE impuestos;
    @XmlElement(name = "Totales", required = true)
    protected ComprobanteTYPE.Totales totales;
    @XmlElement(name = "Control", required = true)
    protected ComprobanteTYPE.Control control;

    /**
     * Obtiene el valor de la propiedad emisor.
     * 
     * @return
     *     possible object is
     *     {@link ComprobanteTYPE.Emisor }
     *     
     */
    public ComprobanteTYPE.Emisor getEmisor() {
        return emisor;
    }

    /**
     * Define el valor de la propiedad emisor.
     * 
     * @param value
     *     allowed object is
     *     {@link ComprobanteTYPE.Emisor }
     *     
     */
    public void setEmisor(ComprobanteTYPE.Emisor value) {
        this.emisor = value;
    }

    /**
     * Obtiene el valor de la propiedad conceptos.
     * 
     * @return
     *     possible object is
     *     {@link ConceptosTYPE }
     *     
     */
    public ConceptosTYPE getConceptos() {
        return conceptos;
    }

    /**
     * Define el valor de la propiedad conceptos.
     * 
     * @param value
     *     allowed object is
     *     {@link ConceptosTYPE }
     *     
     */
    public void setConceptos(ConceptosTYPE value) {
        this.conceptos = value;
    }

    /**
     * Obtiene el valor de la propiedad impuestos.
     * 
     * @return
     *     possible object is
     *     {@link ImpuestosTYPE }
     *     
     */
    public ImpuestosTYPE getImpuestos() {
        return impuestos;
    }

    /**
     * Define el valor de la propiedad impuestos.
     * 
     * @param value
     *     allowed object is
     *     {@link ImpuestosTYPE }
     *     
     */
    public void setImpuestos(ImpuestosTYPE value) {
        this.impuestos = value;
    }

    /**
     * Obtiene el valor de la propiedad totales.
     * 
     * @return
     *     possible object is
     *     {@link ComprobanteTYPE.Totales }
     *     
     */
    public ComprobanteTYPE.Totales getTotales() {
        return totales;
    }

    /**
     * Define el valor de la propiedad totales.
     * 
     * @param value
     *     allowed object is
     *     {@link ComprobanteTYPE.Totales }
     *     
     */
    public void setTotales(ComprobanteTYPE.Totales value) {
        this.totales = value;
    }

    /**
     * Obtiene el valor de la propiedad control.
     * 
     * @return
     *     possible object is
     *     {@link ComprobanteTYPE.Control }
     *     
     */
    public ComprobanteTYPE.Control getControl() {
        return control;
    }

    /**
     * Define el valor de la propiedad control.
     * 
     * @param value
     *     allowed object is
     *     {@link ComprobanteTYPE.Control }
     *     
     */
    public void setControl(ComprobanteTYPE.Control value) {
        this.control = value;
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
     *       &lt;attribute name="cfdId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="estatusId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="estatusIdImpresion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="estatusIdCorreo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="estatusIdArchivo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="rechazoId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="complementoId" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Control {

        @XmlAttribute(name = "cfdId")
        @XmlSchemaType(name = "anySimpleType")
        protected String cfdId;
        @XmlAttribute(name = "estatusId")
        @XmlSchemaType(name = "anySimpleType")
        protected String estatusId;
        @XmlAttribute(name = "estatusIdImpresion")
        @XmlSchemaType(name = "anySimpleType")
        protected String estatusIdImpresion;
        @XmlAttribute(name = "estatusIdCorreo")
        @XmlSchemaType(name = "anySimpleType")
        protected String estatusIdCorreo;
        @XmlAttribute(name = "estatusIdArchivo")
        @XmlSchemaType(name = "anySimpleType")
        protected String estatusIdArchivo;
        @XmlAttribute(name = "rechazoId")
        @XmlSchemaType(name = "anySimpleType")
        protected String rechazoId;
        @XmlAttribute(name = "complementoId")
        @XmlSchemaType(name = "anySimpleType")
        protected String complementoId;

        /**
         * Obtiene el valor de la propiedad cfdId.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCfdId() {
            return cfdId;
        }

        /**
         * Define el valor de la propiedad cfdId.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCfdId(String value) {
            this.cfdId = value;
        }

        /**
         * Obtiene el valor de la propiedad estatusId.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEstatusId() {
            return estatusId;
        }

        /**
         * Define el valor de la propiedad estatusId.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEstatusId(String value) {
            this.estatusId = value;
        }

        /**
         * Obtiene el valor de la propiedad estatusIdImpresion.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEstatusIdImpresion() {
            return estatusIdImpresion;
        }

        /**
         * Define el valor de la propiedad estatusIdImpresion.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEstatusIdImpresion(String value) {
            this.estatusIdImpresion = value;
        }

        /**
         * Obtiene el valor de la propiedad estatusIdCorreo.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEstatusIdCorreo() {
            return estatusIdCorreo;
        }

        /**
         * Define el valor de la propiedad estatusIdCorreo.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEstatusIdCorreo(String value) {
            this.estatusIdCorreo = value;
        }

        /**
         * Obtiene el valor de la propiedad estatusIdArchivo.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEstatusIdArchivo() {
            return estatusIdArchivo;
        }

        /**
         * Define el valor de la propiedad estatusIdArchivo.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEstatusIdArchivo(String value) {
            this.estatusIdArchivo = value;
        }

        /**
         * Obtiene el valor de la propiedad rechazoId.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRechazoId() {
            return rechazoId;
        }

        /**
         * Define el valor de la propiedad rechazoId.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRechazoId(String value) {
            this.rechazoId = value;
        }

        /**
         * Obtiene el valor de la propiedad complementoId.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getComplementoId() {
            return complementoId;
        }

        /**
         * Define el valor de la propiedad complementoId.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setComplementoId(String value) {
            this.complementoId = value;
        }

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
     *       &lt;attribute name="rfc" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="nombre" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="regimenFiscal" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Emisor {

        @XmlAttribute(name = "rfc")
        @XmlSchemaType(name = "anySimpleType")
        protected String rfc;
        @XmlAttribute(name = "nombre")
        @XmlSchemaType(name = "anySimpleType")
        protected String nombre;
        @XmlAttribute(name = "regimenFiscal")
        @XmlSchemaType(name = "anySimpleType")
        protected String regimenFiscal;

        /**
         * Obtiene el valor de la propiedad rfc.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRfc() {
            return rfc;
        }

        /**
         * Define el valor de la propiedad rfc.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRfc(String value) {
            this.rfc = value;
        }

        /**
         * Obtiene el valor de la propiedad nombre.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNombre() {
            return nombre;
        }

        /**
         * Define el valor de la propiedad nombre.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNombre(String value) {
            this.nombre = value;
        }

        /**
         * Obtiene el valor de la propiedad regimenFiscal.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getRegimenFiscal() {
            return regimenFiscal;
        }

        /**
         * Define el valor de la propiedad regimenFiscal.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setRegimenFiscal(String value) {
            this.regimenFiscal = value;
        }

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
     *       &lt;attribute name="subTotal" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="descuento" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="moneda" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="tipoCambio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="total" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="importeLetra" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="totalImpuestosRetenidos" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="totalImpuestosTrasladados" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Totales {

        @XmlAttribute(name = "subTotal")
        @XmlSchemaType(name = "anySimpleType")
        protected String subTotal;
        @XmlAttribute(name = "descuento")
        @XmlSchemaType(name = "anySimpleType")
        protected String descuento;
        @XmlAttribute(name = "moneda")
        @XmlSchemaType(name = "anySimpleType")
        protected String moneda;
        @XmlAttribute(name = "tipoCambio")
        @XmlSchemaType(name = "anySimpleType")
        protected String tipoCambio;
        @XmlAttribute(name = "total")
        @XmlSchemaType(name = "anySimpleType")
        protected String total;
        @XmlAttribute(name = "importeLetra")
        @XmlSchemaType(name = "anySimpleType")
        protected String importeLetra;
        @XmlAttribute(name = "totalImpuestosRetenidos")
        @XmlSchemaType(name = "anySimpleType")
        protected String totalImpuestosRetenidos;
        @XmlAttribute(name = "totalImpuestosTrasladados")
        @XmlSchemaType(name = "anySimpleType")
        protected String totalImpuestosTrasladados;

        /**
         * Obtiene el valor de la propiedad subTotal.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSubTotal() {
            return subTotal;
        }

        /**
         * Define el valor de la propiedad subTotal.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSubTotal(String value) {
            this.subTotal = value;
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
         * Obtiene el valor de la propiedad moneda.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMoneda() {
            return moneda;
        }

        /**
         * Define el valor de la propiedad moneda.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMoneda(String value) {
            this.moneda = value;
        }

        /**
         * Obtiene el valor de la propiedad tipoCambio.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTipoCambio() {
            return tipoCambio;
        }

        /**
         * Define el valor de la propiedad tipoCambio.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTipoCambio(String value) {
            this.tipoCambio = value;
        }

        /**
         * Obtiene el valor de la propiedad total.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTotal() {
            return total;
        }

        /**
         * Define el valor de la propiedad total.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTotal(String value) {
            this.total = value;
        }

        /**
         * Obtiene el valor de la propiedad importeLetra.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getImporteLetra() {
            return importeLetra;
        }

        /**
         * Define el valor de la propiedad importeLetra.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setImporteLetra(String value) {
            this.importeLetra = value;
        }

        /**
         * Obtiene el valor de la propiedad totalImpuestosRetenidos.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTotalImpuestosRetenidos() {
            return totalImpuestosRetenidos;
        }

        /**
         * Define el valor de la propiedad totalImpuestosRetenidos.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTotalImpuestosRetenidos(String value) {
            this.totalImpuestosRetenidos = value;
        }

        /**
         * Obtiene el valor de la propiedad totalImpuestosTrasladados.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTotalImpuestosTrasladados() {
            return totalImpuestosTrasladados;
        }

        /**
         * Define el valor de la propiedad totalImpuestosTrasladados.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTotalImpuestosTrasladados(String value) {
            this.totalImpuestosTrasladados = value;
        }

    }

}

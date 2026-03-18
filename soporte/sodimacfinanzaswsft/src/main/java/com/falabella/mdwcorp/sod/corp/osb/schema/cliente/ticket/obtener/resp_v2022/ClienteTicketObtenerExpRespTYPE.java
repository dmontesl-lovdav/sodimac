
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2022;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para ClienteTicketObtenerExpResp_TYPE complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="ClienteTicketObtenerExpResp_TYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Comprobante" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2022.01}Comprobante_TYPE"&gt;
 *                 &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="serie" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="folio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="fecha" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="formaPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="condicionesDePago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="tipoComprobante" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="exportacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="metodoPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="lugarExpedicion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="confirmacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="calle" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="noExterior" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="noInterior" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="colonia" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="localidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="referencia" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="municipio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="estado" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="pais" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="DatosExtraCFD" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="extra1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra3" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra4" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra5" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra6" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra7" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra8" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="extra9" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Respuesta"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="codigo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *                 &lt;attribute name="descripcion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClienteTicketObtenerExpResp_TYPE", propOrder = {
    "comprobante",
    "datosExtraCFD",
    "respuesta"
})
public class ClienteTicketObtenerExpRespTYPE {

    @XmlElement(name = "Comprobante")
    protected ClienteTicketObtenerExpRespTYPE.Comprobante comprobante;
    @XmlElement(name = "DatosExtraCFD")
    protected ClienteTicketObtenerExpRespTYPE.DatosExtraCFD datosExtraCFD;
    @XmlElement(name = "Respuesta", required = true)
    protected ClienteTicketObtenerExpRespTYPE.Respuesta respuesta;

    /**
     * Obtiene el valor de la propiedad comprobante.
     * 
     * @return
     *     possible object is
     *     {@link ClienteTicketObtenerExpRespTYPE.Comprobante }
     *     
     */
    public ClienteTicketObtenerExpRespTYPE.Comprobante getComprobante() {
        return comprobante;
    }

    /**
     * Define el valor de la propiedad comprobante.
     * 
     * @param value
     *     allowed object is
     *     {@link ClienteTicketObtenerExpRespTYPE.Comprobante }
     *     
     */
    public void setComprobante(ClienteTicketObtenerExpRespTYPE.Comprobante value) {
        this.comprobante = value;
    }

    /**
     * Obtiene el valor de la propiedad datosExtraCFD.
     * 
     * @return
     *     possible object is
     *     {@link ClienteTicketObtenerExpRespTYPE.DatosExtraCFD }
     *     
     */
    public ClienteTicketObtenerExpRespTYPE.DatosExtraCFD getDatosExtraCFD() {
        return datosExtraCFD;
    }

    /**
     * Define el valor de la propiedad datosExtraCFD.
     * 
     * @param value
     *     allowed object is
     *     {@link ClienteTicketObtenerExpRespTYPE.DatosExtraCFD }
     *     
     */
    public void setDatosExtraCFD(ClienteTicketObtenerExpRespTYPE.DatosExtraCFD value) {
        this.datosExtraCFD = value;
    }

    /**
     * Obtiene el valor de la propiedad respuesta.
     * 
     * @return
     *     possible object is
     *     {@link ClienteTicketObtenerExpRespTYPE.Respuesta }
     *     
     */
    public ClienteTicketObtenerExpRespTYPE.Respuesta getRespuesta() {
        return respuesta;
    }

    /**
     * Define el valor de la propiedad respuesta.
     * 
     * @param value
     *     allowed object is
     *     {@link ClienteTicketObtenerExpRespTYPE.Respuesta }
     *     
     */
    public void setRespuesta(ClienteTicketObtenerExpRespTYPE.Respuesta value) {
        this.respuesta = value;
    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2022.01}Comprobante_TYPE"&gt;
     *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="serie" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="folio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="fecha" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="formaPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="condicionesDePago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="tipoComprobante" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="exportacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="metodoPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="lugarExpedicion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="confirmacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="calle" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="noExterior" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="noInterior" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="colonia" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="localidad" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="referencia" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="municipio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="estado" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="pais" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Comprobante
        extends ComprobanteTYPE
    {

        @XmlAttribute(name = "version")
        @XmlSchemaType(name = "anySimpleType")
        protected String version;
        @XmlAttribute(name = "serie")
        @XmlSchemaType(name = "anySimpleType")
        protected String serie;
        @XmlAttribute(name = "folio")
        @XmlSchemaType(name = "anySimpleType")
        protected String folio;
        @XmlAttribute(name = "fecha")
        @XmlSchemaType(name = "anySimpleType")
        protected String fecha;
        @XmlAttribute(name = "formaPago")
        @XmlSchemaType(name = "anySimpleType")
        protected String formaPago;
        @XmlAttribute(name = "condicionesDePago")
        @XmlSchemaType(name = "anySimpleType")
        protected String condicionesDePago;
        @XmlAttribute(name = "tipoComprobante")
        @XmlSchemaType(name = "anySimpleType")
        protected String tipoComprobante;
        @XmlAttribute(name = "exportacion")
        @XmlSchemaType(name = "anySimpleType")
        protected String exportacion;
        @XmlAttribute(name = "metodoPago")
        @XmlSchemaType(name = "anySimpleType")
        protected String metodoPago;
        @XmlAttribute(name = "lugarExpedicion")
        @XmlSchemaType(name = "anySimpleType")
        protected String lugarExpedicion;
        @XmlAttribute(name = "confirmacion")
        @XmlSchemaType(name = "anySimpleType")
        protected String confirmacion;
        @XmlAttribute(name = "calle")
        @XmlSchemaType(name = "anySimpleType")
        protected String calle;
        @XmlAttribute(name = "noExterior")
        @XmlSchemaType(name = "anySimpleType")
        protected String noExterior;
        @XmlAttribute(name = "noInterior")
        @XmlSchemaType(name = "anySimpleType")
        protected String noInterior;
        @XmlAttribute(name = "colonia")
        @XmlSchemaType(name = "anySimpleType")
        protected String colonia;
        @XmlAttribute(name = "localidad")
        @XmlSchemaType(name = "anySimpleType")
        protected String localidad;
        @XmlAttribute(name = "referencia")
        @XmlSchemaType(name = "anySimpleType")
        protected String referencia;
        @XmlAttribute(name = "municipio")
        @XmlSchemaType(name = "anySimpleType")
        protected String municipio;
        @XmlAttribute(name = "estado")
        @XmlSchemaType(name = "anySimpleType")
        protected String estado;
        @XmlAttribute(name = "pais")
        @XmlSchemaType(name = "anySimpleType")
        protected String pais;

        /**
         * Obtiene el valor de la propiedad version.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getVersion() {
            return version;
        }

        /**
         * Define el valor de la propiedad version.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setVersion(String value) {
            this.version = value;
        }

        /**
         * Obtiene el valor de la propiedad serie.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSerie() {
            return serie;
        }

        /**
         * Define el valor de la propiedad serie.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSerie(String value) {
            this.serie = value;
        }

        /**
         * Obtiene el valor de la propiedad folio.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFolio() {
            return folio;
        }

        /**
         * Define el valor de la propiedad folio.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFolio(String value) {
            this.folio = value;
        }

        /**
         * Obtiene el valor de la propiedad fecha.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFecha() {
            return fecha;
        }

        /**
         * Define el valor de la propiedad fecha.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFecha(String value) {
            this.fecha = value;
        }

        /**
         * Obtiene el valor de la propiedad formaPago.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getFormaPago() {
            return formaPago;
        }

        /**
         * Define el valor de la propiedad formaPago.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setFormaPago(String value) {
            this.formaPago = value;
        }

        /**
         * Obtiene el valor de la propiedad condicionesDePago.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCondicionesDePago() {
            return condicionesDePago;
        }

        /**
         * Define el valor de la propiedad condicionesDePago.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCondicionesDePago(String value) {
            this.condicionesDePago = value;
        }

        /**
         * Obtiene el valor de la propiedad tipoComprobante.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTipoComprobante() {
            return tipoComprobante;
        }

        /**
         * Define el valor de la propiedad tipoComprobante.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTipoComprobante(String value) {
            this.tipoComprobante = value;
        }

        /**
         * Obtiene el valor de la propiedad exportacion.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExportacion() {
            return exportacion;
        }

        /**
         * Define el valor de la propiedad exportacion.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExportacion(String value) {
            this.exportacion = value;
        }

        /**
         * Obtiene el valor de la propiedad metodoPago.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMetodoPago() {
            return metodoPago;
        }

        /**
         * Define el valor de la propiedad metodoPago.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMetodoPago(String value) {
            this.metodoPago = value;
        }

        /**
         * Obtiene el valor de la propiedad lugarExpedicion.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLugarExpedicion() {
            return lugarExpedicion;
        }

        /**
         * Define el valor de la propiedad lugarExpedicion.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLugarExpedicion(String value) {
            this.lugarExpedicion = value;
        }

        /**
         * Obtiene el valor de la propiedad confirmacion.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getConfirmacion() {
            return confirmacion;
        }

        /**
         * Define el valor de la propiedad confirmacion.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setConfirmacion(String value) {
            this.confirmacion = value;
        }

        /**
         * Obtiene el valor de la propiedad calle.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCalle() {
            return calle;
        }

        /**
         * Define el valor de la propiedad calle.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCalle(String value) {
            this.calle = value;
        }

        /**
         * Obtiene el valor de la propiedad noExterior.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNoExterior() {
            return noExterior;
        }

        /**
         * Define el valor de la propiedad noExterior.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNoExterior(String value) {
            this.noExterior = value;
        }

        /**
         * Obtiene el valor de la propiedad noInterior.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNoInterior() {
            return noInterior;
        }

        /**
         * Define el valor de la propiedad noInterior.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNoInterior(String value) {
            this.noInterior = value;
        }

        /**
         * Obtiene el valor de la propiedad colonia.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getColonia() {
            return colonia;
        }

        /**
         * Define el valor de la propiedad colonia.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColonia(String value) {
            this.colonia = value;
        }

        /**
         * Obtiene el valor de la propiedad localidad.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLocalidad() {
            return localidad;
        }

        /**
         * Define el valor de la propiedad localidad.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLocalidad(String value) {
            this.localidad = value;
        }

        /**
         * Obtiene el valor de la propiedad referencia.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getReferencia() {
            return referencia;
        }

        /**
         * Define el valor de la propiedad referencia.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setReferencia(String value) {
            this.referencia = value;
        }

        /**
         * Obtiene el valor de la propiedad municipio.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMunicipio() {
            return municipio;
        }

        /**
         * Define el valor de la propiedad municipio.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMunicipio(String value) {
            this.municipio = value;
        }

        /**
         * Obtiene el valor de la propiedad estado.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEstado() {
            return estado;
        }

        /**
         * Define el valor de la propiedad estado.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEstado(String value) {
            this.estado = value;
        }

        /**
         * Obtiene el valor de la propiedad pais.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPais() {
            return pais;
        }

        /**
         * Define el valor de la propiedad pais.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPais(String value) {
            this.pais = value;
        }

    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="extra1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra3" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra4" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra5" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra6" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra7" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra8" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="extra9" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class DatosExtraCFD {

        @XmlAttribute(name = "extra1")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra1;
        @XmlAttribute(name = "extra2")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra2;
        @XmlAttribute(name = "extra3")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra3;
        @XmlAttribute(name = "extra4")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra4;
        @XmlAttribute(name = "extra5")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra5;
        @XmlAttribute(name = "extra6")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra6;
        @XmlAttribute(name = "extra7")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra7;
        @XmlAttribute(name = "extra8")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra8;
        @XmlAttribute(name = "extra9")
        @XmlSchemaType(name = "anySimpleType")
        protected String extra9;

        /**
         * Obtiene el valor de la propiedad extra1.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra1() {
            return extra1;
        }

        /**
         * Define el valor de la propiedad extra1.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra1(String value) {
            this.extra1 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra2.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra2() {
            return extra2;
        }

        /**
         * Define el valor de la propiedad extra2.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra2(String value) {
            this.extra2 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra3.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra3() {
            return extra3;
        }

        /**
         * Define el valor de la propiedad extra3.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra3(String value) {
            this.extra3 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra4.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra4() {
            return extra4;
        }

        /**
         * Define el valor de la propiedad extra4.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra4(String value) {
            this.extra4 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra5.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra5() {
            return extra5;
        }

        /**
         * Define el valor de la propiedad extra5.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra5(String value) {
            this.extra5 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra6.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra6() {
            return extra6;
        }

        /**
         * Define el valor de la propiedad extra6.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra6(String value) {
            this.extra6 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra7.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra7() {
            return extra7;
        }

        /**
         * Define el valor de la propiedad extra7.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra7(String value) {
            this.extra7 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra8.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra8() {
            return extra8;
        }

        /**
         * Define el valor de la propiedad extra8.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra8(String value) {
            this.extra8 = value;
        }

        /**
         * Obtiene el valor de la propiedad extra9.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtra9() {
            return extra9;
        }

        /**
         * Define el valor de la propiedad extra9.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtra9(String value) {
            this.extra9 = value;
        }

    }


    /**
     * <p>Clase Java para anonymous complex type.
     * 
     * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="codigo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *       &lt;attribute name="descripcion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Respuesta {

        @XmlAttribute(name = "codigo")
        @XmlSchemaType(name = "anySimpleType")
        protected String codigo;
        @XmlAttribute(name = "descripcion")
        @XmlSchemaType(name = "anySimpleType")
        protected String descripcion;

        /**
         * Obtiene el valor de la propiedad codigo.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCodigo() {
            return codigo;
        }

        /**
         * Define el valor de la propiedad codigo.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCodigo(String value) {
            this.codigo = value;
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

    }

}

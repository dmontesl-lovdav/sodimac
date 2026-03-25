
package com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018;

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
 * &lt;complexType name="ClienteTicketObtenerExpResp_TYPE">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Comprobante" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2018.02}Comprobante_TYPE">
 *                 &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="serie" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="folio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="fecha" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="formaPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="condicionesDePago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="tipoComprobante" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="metodoPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="lugarExpedicion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="confirmacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="DatosExtraCFD" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="extra1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="extra2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Respuesta">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="codigo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;attribute name="descripcion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
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
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;extension base="{http://mdwcorp.falabella.com/SOD/CORP/OSB/schema/Cliente/Ticket/Obtener/Resp-v2018.02}Comprobante_TYPE">
     *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="serie" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="folio" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="fecha" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="formaPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="condicionesDePago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="tipoComprobante" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="metodoPago" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="lugarExpedicion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="confirmacion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/extension>
     *   &lt;/complexContent>
     * &lt;/complexType>
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
        @XmlAttribute(name = "metodoPago")
        @XmlSchemaType(name = "anySimpleType")
        protected String metodoPago;
        @XmlAttribute(name = "lugarExpedicion")
        @XmlSchemaType(name = "anySimpleType")
        protected String lugarExpedicion;
        @XmlAttribute(name = "confirmacion")
        @XmlSchemaType(name = "anySimpleType")
        protected String confirmacion;

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
     *       &lt;attribute name="extra1" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="extra2" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
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
     *       &lt;attribute name="codigo" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;attribute name="descripcion" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
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

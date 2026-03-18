package com.sodimac.facturacion.entity.fis;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "complementos")
public class ComplementosEntity {

	@Id
	@Column(name = "idComplemento")
	private int idComplemento;

	@Column(name = "idPac")
	private int idPac;

	@Column(name = "idCliente", nullable=true)
	private Integer idCliente;

	@Column(name = "rfc")
	private String rfc;

	@Column(name = "email")
	private String email;

	@Column(name = "ticket")
	private String ticket;

	@Column(name = "idVersionFacturaSodimac")
	private int idVersionFacturaSodimac;

	@Column(name = "idFacturaPac")
	private int idFacturaPac;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "fechaTimbrado")
	private java.util.Date fechaTimbrado;

	@Column(name = "versionFacturacionSat")
	private String versionFacturacionSat;

	@Lob
	@Column(name = "xml")
	private String xml;

	@Column(name = "fechaCompra")
	private java.util.Date fechaCompra;

	@Column(name = "idOrigen")
	private int idOrigen;

	@Column(name = "idEstatusFactura")
	private int idEstatusFactura;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Column(name = "nombreArchivo")
	private String nombreArchivo;

	@Column(name = "ticketBct")
	private String ticketBct;

	@Column(name = "versionFactura")
	private String versionFactura;

	@Column(name = "transaccion")
	private String transaccion;

	@Column(name = "nombreObra")
	private String nombreObra;

	@Column(name = "responsableObra")
	private String responsableObra;

	@Column(name = "idComprobante", length=1, columnDefinition="CHAR")
	private String idComprobante;

	@Column(name = "uuidRelacionado")
	private String uuidRelacionado;

	@Column(name = "acuse")
	private String acuse;

	@Column(name = "serie")
	private String serie;

	@Column(name = "folio")
	private int folio;

	@Column(name = "total", precision=11, scale=2)
	private BigDecimal total;

	@Column(name = "subTotal", precision=11, scale=2)
	private BigDecimal subTotal;

	public ComplementosEntity() {

	}

	public int getIdComplemento() {
		return idComplemento;
	}

	public void setIdComplemento(int idComplemento) {
		this.idComplemento = idComplemento;
	}

	public int getIdPac() {
		return idPac;
	}

	public void setIdPac(int idPac) {
		this.idPac = idPac;
	}

	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public int getIdVersionFacturaSodimac() {
		return idVersionFacturaSodimac;
	}

	public void setIdVersionFacturaSodimac(int idVersionFacturaSodimac) {
		this.idVersionFacturaSodimac = idVersionFacturaSodimac;
	}

	public int getIdFacturaPac() {
		return idFacturaPac;
	}

	public void setIdFacturaPac(int idFacturaPac) {
		this.idFacturaPac = idFacturaPac;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public java.util.Date getFechaTimbrado() {
		return fechaTimbrado;
	}

	public void setFechaTimbrado(java.util.Date fechaTimbrado) {
		this.fechaTimbrado = fechaTimbrado;
	}

	public String getVersionFacturacionSat() {
		return versionFacturacionSat;
	}

	public void setVersionFacturacionSat(String versionFacturacionSat) {
		this.versionFacturacionSat = versionFacturacionSat;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public java.util.Date getFechaCompra() {
		return fechaCompra;
	}

	public void setFechaCompra(java.util.Date fechaCompra) {
		this.fechaCompra = fechaCompra;
	}

	public int getIdOrigen() {
		return idOrigen;
	}

	public void setIdOrigen(int idOrigen) {
		this.idOrigen = idOrigen;
	}

	public int getIdEstatusFactura() {
		return idEstatusFactura;
	}

	public void setIdEstatusFactura(int idEstatusFactura) {
		this.idEstatusFactura = idEstatusFactura;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getTicketBct() {
		return ticketBct;
	}

	public void setTicketBct(String ticketBct) {
		this.ticketBct = ticketBct;
	}

	public String getVersionFactura() {
		return versionFactura;
	}

	public void setVersionFactura(String versionFactura) {
		this.versionFactura = versionFactura;
	}

	public String getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}

	public String getNombreObra() {
		return nombreObra;
	}

	public void setNombreObra(String nombreObra) {
		this.nombreObra = nombreObra;
	}

	public String getResponsableObra() {
		return responsableObra;
	}

	public void setResponsableObra(String responsableObra) {
		this.responsableObra = responsableObra;
	}

	public String getIdComprobante() {
		return idComprobante;
	}

	public void setIdComprobante(String idComprobante) {
		this.idComprobante = idComprobante;
	}

	public String getUuidRelacionado() {
		return uuidRelacionado;
	}

	public void setUuidRelacionado(String uuidRelacionado) {
		this.uuidRelacionado = uuidRelacionado;
	}

	public String getAcuse() {
		return acuse;
	}

	public void setAcuse(String acuse) {
		this.acuse = acuse;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public int getFolio() {
		return folio;
	}

	public void setFolio(int folio) {
		this.folio = folio;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(BigDecimal subTotal) {
		this.subTotal = subTotal;
	}

}

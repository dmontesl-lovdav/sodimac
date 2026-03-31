package com.sodimac.bctfacturacion.model;

import java.util.Date;

public class FacturaModel {

	private int idFactura;
	private int idPac;
	private Integer idCliente;
	private String rfc;
	private String email;
	private String ticket;
	private int idVersionFacturaSodimac;
	private int idFacturaPac;
	private String uuid;
	private Date fechaTimbrado;
	private String versionFacturacionSat;
	private String xml;
	private Date fechaCompra;
	private int idOrigen;
	private int idEstatusFactura;
	private Date fechaCreacion;
	private String nombreArchivo;
	private String ticketBct;
	private String versionFactura;
	private String transaccion;
	private String nombreObra;
	private String responsableObra;
	private String idComprobante;
	private String uuidRelacionado;
	private String serie;
	private String folio;

	public int getIdFactura() {
		return idFactura;
	}

	public void setIdFactura(int idFactura) {
		this.idFactura = idFactura;
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

	public Date getFechaTimbrado() {
		return fechaTimbrado;
	}

	public void setFechaTimbrado(Date fechaTimbrado) {
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

	public Date getFechaCompra() {
		return fechaCompra;
	}

	public void setFechaCompra(Date fechaCompra) {
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

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
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

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	@Override
	public String toString() {
		return "FacturaModel [idFactura=" + idFactura + ", idPac=" + idPac + ", idCliente=" + idCliente + ", rfc=" + rfc
				+ ", email=" + email + ", ticket=" + ticket + ", idVersionFacturaSodimac=" + idVersionFacturaSodimac
				+ ", idFacturaPac=" + idFacturaPac + ", uuid=" + uuid + ", fechaTimbrado=" + fechaTimbrado
				+ ", versionFacturacionSat=" + versionFacturacionSat + ", xml=" + xml + ", fechaCompra=" + fechaCompra
				+ ", idOrigen=" + idOrigen + ", idEstatusFactura=" + idEstatusFactura + ", fechaCreacion="
				+ fechaCreacion + ", nombreArchivo=" + nombreArchivo + ", ticketBct=" + ticketBct + ", versionFactura="
				+ versionFactura + ", transaccion=" + transaccion + ", nombreObra=" + nombreObra + ", responsableObra="
				+ responsableObra + ", idComprobante=" + idComprobante + ", uuidRelacionado=" + uuidRelacionado
				+ ", serie=" + serie + ", folio=" + folio + "]";
	}
}

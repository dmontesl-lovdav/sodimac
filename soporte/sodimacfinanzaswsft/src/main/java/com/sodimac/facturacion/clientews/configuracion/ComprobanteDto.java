package com.sodimac.facturacion.clientews.configuracion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ComprobanteDto {

	private String version;
	private String serie;
	private String folio;
	private String fecha;
	private String formaPago;
	private String condicionesDePago;
	private String tipoDeComprobante;
	private String metodoPago;
	private String exportacion;
	private ConfDatosEmisorTiendaDto lugarExpedicion;
	private ConfDatosEmisorDto emisorNode;
	
	public ComprobanteDto() {
		
	}
	
	public ComprobanteDto(String version, ConfDatosEmisorDto emisorNode, ConfDatosEmisorTiendaDto lugarExpedicion) {
		this.version = version;
		this.serie = "";
		this.folio = "";
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		this.fecha = dtf.format(LocalDateTime.now());
		this.formaPago = "";
		this.condicionesDePago = "";
		this.tipoDeComprobante = "";
		this.metodoPago = "";
		this.exportacion = version.equals("4.0") ? "01" : "";
		this.lugarExpedicion = lugarExpedicion;
		this.emisorNode = emisorNode;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
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
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		this.fecha = fecha;
	}
	public String getFormaPago() {
		return formaPago;
	}
	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}
	public String getCondicionesDePago() {
		return condicionesDePago;
	}
	public void setCondicionesDePago(String condicionesDePago) {
		this.condicionesDePago = condicionesDePago;
	}
	public String getTipoDeComprobante() {
		return tipoDeComprobante;
	}
	public void setTipoDeComprobante(String tipoDeComprobante) {
		this.tipoDeComprobante = tipoDeComprobante;
	}
	public String getMetodoPago() {
		return metodoPago;
	}
	public void setMetodoPago(String metodoPago) {
		this.metodoPago = metodoPago;
	}
	public String getExportacion() {
		return exportacion;
	}

	public void setExportacion(String exportacion) {
		this.exportacion = exportacion;
	}

	public ConfDatosEmisorTiendaDto getLugarExpedicion() {
		return lugarExpedicion;
	}
	public void setLugarExpedicion(ConfDatosEmisorTiendaDto lugarExpedicion) {
		this.lugarExpedicion = lugarExpedicion;
	}

	public ConfDatosEmisorDto getEmisorNode() {
		return emisorNode;
	}

	public void setEmisorNode(ConfDatosEmisorDto emisorNode) {
		this.emisorNode = emisorNode;
	}

	
	
}

package com.sodimac.wsconfiguracion.models.config;

public class EmisorReq {
	
	protected String rfc;
	protected String version;
	protected int sucursal;
	protected String tipoDeComprobante;
	protected String TipoDeOperacion;
	protected String formaPago;
	protected int idAplicacion;
	
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getSucursal() {
		return sucursal;
	}
	public void setSucursal(int sucursal) {
		this.sucursal = sucursal;
	}
	public String getTipoDeComprobante() {
		return tipoDeComprobante;
	}
	public void setTipoDeComprobante(String tipoDeComprobante) {
		this.tipoDeComprobante = tipoDeComprobante;
	}
	public String getTipoDeOperacion() {
		return TipoDeOperacion;
	}
	public void setTipoDeOperacion(String tipoDeOperacion) {
		TipoDeOperacion = tipoDeOperacion;
	}
	public String getFormaPago() {
		return formaPago;
	}
	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}
	public int getIdAplicacion() {
		return idAplicacion;
	}
	public void setIdAplicacion(int idAplicacion) {
		this.idAplicacion = idAplicacion;
	}
	

	

}

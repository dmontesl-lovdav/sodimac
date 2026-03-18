package com.sodimac.facturacion.clientews.configuracion;

public class EmisorReq {

	protected String rfc;
	protected int sucursal;
	protected String tipoDeComprobante;
	protected String tipoDeOperacion;
    protected String version;
    protected int idAplicacion;
    protected String formaPago;
    
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
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
		return tipoDeOperacion;
	}
	public void setTipoDeOperacion(String tipoDeOperacion) {
		this.tipoDeOperacion = tipoDeOperacion;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public int getIdAplicacion() {
		return idAplicacion;
	}
	public void setIdAplicacion(int idAplicacion) {
		this.idAplicacion = idAplicacion;
	}
	public String getFormaPago() {
		return formaPago;
	}
	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}
    
}
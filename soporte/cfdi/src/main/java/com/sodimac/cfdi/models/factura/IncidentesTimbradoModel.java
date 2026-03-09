package com.sodimac.cfdi.models.factura;

public class IncidentesTimbradoModel {

	private String ticket;
	private double total;
	private String rfc;
	private String razonSocial;
	private String usoCfdi;
	private String email;
	private String regimenFiscal;
	private String codigoPostal;
	private String fechaCreacion;

	private int idFacturaPac;
	private String uuid;
	private String fechaTimbrado;
	private String facturadoEn;
	private String mensajeError;
	private int intentos;
	private int nivel;
	private String atiende;
	
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public String getRfc() {
		return rfc;
	}
	public void setRfc(String rfc) {
		this.rfc = rfc;
	}
	public String getRazonSocial() {
		return razonSocial;
	}
	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}
	public String getUsoCfdi() {
		return usoCfdi;
	}
	public void setUsoCfdi(String usoCfdi) {
		this.usoCfdi = usoCfdi;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getRegimenFiscal() {
		return regimenFiscal;
	}
	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
	}
	public String getCodigoPostal() {
		return codigoPostal;
	}
	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}
	public String getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaCreacion(String fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
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
	public String getFechaTimbrado() {
		return fechaTimbrado;
	}
	public void setFechaTimbrado(String fechaTimbrado) {
		this.fechaTimbrado = fechaTimbrado;
	}
	public String getFacturadoEn() {
		return facturadoEn;
	}
	public void setFacturadoEn(String facturadoEn) {
		this.facturadoEn = facturadoEn;
	}
	public String getMensajeError() {
		return mensajeError;
	}
	public void setMensajeError(String mensajeError) {
		this.mensajeError = mensajeError;
	}
	public int getIntentos() {
		return intentos;
	}
	public void setIntentos(int intentos) {
		this.intentos = intentos;
	}
	public int getNivel() {
		return nivel;
	}
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}
	public String getAtiende() {
		return atiende;
	}
	public void setAtiende(String atiende) {
		this.atiende = atiende;
	}

}

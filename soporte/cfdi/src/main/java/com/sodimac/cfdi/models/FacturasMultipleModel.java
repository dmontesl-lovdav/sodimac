package com.sodimac.cfdi.models;

public class FacturasMultipleModel extends CodigoError {

	private int idFactura;
	
	private String uuid;
	
	private String ticket;
	
	private String razonSocial;
	
	private String fechaTimbrado;
	
	private String nombreEstatus;

	private String rfc;

	private String nombreArchivo;
	
	private String checked;
	
	private String ticketBct;
	private int idFacturaPac;
	private String versionFacturacionSat;
	private String fechaCompra;
	private String nombreObra;
	private String responsableObra;
	private String idComprobante;
	private String descripcionTimbrado;
	private String descripcionOrigen;
	private String total;

	public FacturasMultipleModel() {
		
		idFactura = 0;
		uuid = "";
		ticket = "";
		razonSocial = "";
		fechaTimbrado = "";
		nombreEstatus = "";
		rfc = "";
		nombreArchivo = "";
		checked = "";
		ticketBct = "";
		idFacturaPac = 0;
		versionFacturacionSat = "";
		fechaCompra = "";
		nombreObra = "";
		responsableObra = "";
		idComprobante = "";
		descripcionTimbrado = "";
	}

	public String getChecked() {
		return checked;
	}

	public void setChecked(String checked) {
		this.checked = checked;
	}

	public int getIdFactura() {
		return idFactura;
	}

	public void setIdFactura(int idFactura) {
		this.idFactura = idFactura;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getFechaTimbrado() {
		return fechaTimbrado;
	}

	public void setFechaTimbrado(String fechaTimbrado) {
		this.fechaTimbrado = fechaTimbrado;
	}

	public String getNombreEstatus() {
		return nombreEstatus;
	}

	public void setNombreEstatus(String nombreEstatus) {
		this.nombreEstatus = nombreEstatus;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
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

	public int getIdFacturaPac() {
		return idFacturaPac;
	}

	public void setIdFacturaPac(int idFacturaPac) {
		this.idFacturaPac = idFacturaPac;
	}

	public String getVersionFacturacionSat() {
		return versionFacturacionSat;
	}

	public void setVersionFacturacionSat(String versionFacturacionSat) {
		this.versionFacturacionSat = versionFacturacionSat;
	}

	public String getFechaCompra() {
		return fechaCompra;
	}

	public void setFechaCompra(String fechaCompra) {
		this.fechaCompra = fechaCompra;
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
	
	public String getDescripcionTimbrado() {
		return descripcionTimbrado;
	}

	public void setDescripcionTimbrado(String descripcionTimbrado) {
		this.descripcionTimbrado = descripcionTimbrado;
	}
	
	public String getDescripcionOrigen() {
		return descripcionOrigen;
	}

	public void setDescripcionOrigen(String descripcionOrigen) {
		this.descripcionOrigen = descripcionOrigen;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "FacturasMultipleModel [idFactura=" + idFactura + ", uuid=" + uuid + ", ticket=" + ticket
				+ ", razonSocial=" + razonSocial + ", fechaTimbrado=" + fechaTimbrado + ", nombreEstatus="
				+ nombreEstatus + ", rfc=" + rfc + ", nombreArchivo=" + nombreArchivo + ", checked=" + checked
				+ ", ticketBct=" + ticketBct + ", idFacturaPac=" + idFacturaPac + ", versionFacturacionSat="
				+ versionFacturacionSat + ", fechaCompra=" + fechaCompra + ", nombreObra=" + nombreObra
				+ ", responsableObra=" + responsableObra + ", idComprobante=" + idComprobante 
				+ ", descripcionTimbrado=" +descripcionTimbrado +	"]";
	}

}

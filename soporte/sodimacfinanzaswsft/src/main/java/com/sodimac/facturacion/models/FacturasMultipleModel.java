package com.sodimac.facturacion.models;

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

	@Override
	public String toString() {
		return "FacturasMultipleModel [idFactura=" + idFactura + ", uuid=" + uuid + ", ticket=" + ticket
				+ ", razonSocial=" + razonSocial + ", fechaTimbrado=" + fechaTimbrado + ", nombreEstatus="
				+ nombreEstatus + ", rfc=" + rfc + ", nombreArchivo=" + nombreArchivo + ", checked=" + checked + "]";
	}

}

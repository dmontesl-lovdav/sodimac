package com.sodimac.facturacion.models;

public class DescuentosRebatesModel {

	private String rfcProveedor;
	private String nombreProveedor;
	private String regimenFiscalProveedor;
	private String codigoPostalProveedor;
	private String numeroDocumento;
	private String numeroReferencia;
	private String ticket;
	private String moneda;
	private double tipoCambio;
	private double subTotal;
	private double iva;
	private double total;
	private java.util.Date fechaRecepcion;
	private java.util.Date fechaContable;
	private String correo;
	
	public DescuentosRebatesModel() {
		
	}

	public String getRfcProveedor() {
		return rfcProveedor;
	}

	public void setRfcProveedor(String rfcProveedor) {
		this.rfcProveedor = rfcProveedor;
	}

	public String getNombreProveedor() {
		return nombreProveedor;
	}

	public void setNombreProveedor(String nombreProveedor) {
		this.nombreProveedor = nombreProveedor;
	}

	public String getRegimenFiscalProveedor() {
		return regimenFiscalProveedor;
	}

	public void setRegimenFiscalProveedor(String regimenFiscalProveedor) {
		this.regimenFiscalProveedor = regimenFiscalProveedor;
	}

	public String getCodigoPostalProveedor() {
		return codigoPostalProveedor;
	}

	public void setCodigoPostalProveedor(String codigoPostalProveedor) {
		this.codigoPostalProveedor = codigoPostalProveedor;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getNumeroReferencia() {
		return numeroReferencia;
	}

	public void setNumeroReferencia(String numeroReferencia) {
		this.numeroReferencia = numeroReferencia;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public double getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(double tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public double getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public double getIva() {
		return iva;
	}

	public void setIva(double iva) {
		this.iva = iva;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public java.util.Date getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(java.util.Date fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	public java.util.Date getFechaContable() {
		return fechaContable;
	}

	public void setFechaContable(java.util.Date fechaContable) {
		this.fechaContable = fechaContable;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}
	
}

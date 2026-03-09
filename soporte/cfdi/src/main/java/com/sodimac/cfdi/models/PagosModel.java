package com.sodimac.cfdi.models;

public class PagosModel {
	
	private String idPago;
	private String numeroCuenta;
	private String fechaHoraMovimiento;
	private String concepto;
	private String importe;
	private String folioBanco;
	private String refInterbancaria;
	private String folioCliente;
	private String tipoDivisa;
	private String folioOperacion;
	private String formaPago;
	private String estatus;
	private String descEstatus;
	private boolean spei;
	
	public String getIdPago() {
		return idPago;
	}

	public void setIdPago(String idPago) {
		this.idPago = idPago;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public String getFechaHoraMovimiento() {
		return fechaHoraMovimiento;
	}

	public void setFechaHoraMovimiento(String fechaHoraMovimiento) {
		this.fechaHoraMovimiento = fechaHoraMovimiento;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public String getImporte() {
		return importe;
	}

	public void setImporte(String importe) {
		this.importe = importe;
	}

	public String getFolioBanco() {
		return folioBanco;
	}

	public void setFolioBanco(String folioBanco) {
		this.folioBanco = folioBanco;
	}

	public String getRefInterbancaria() {
		return refInterbancaria;
	}

	public void setRefInterbancaria(String refInterbancaria) {
		this.refInterbancaria = refInterbancaria;
	}

	public String getFolioCliente() {
		return folioCliente;
	}

	public void setFolioCliente(String folioCliente) {
		this.folioCliente = folioCliente;
	}

	public String getTipoDivisa() {
		return tipoDivisa;
	}

	public void setTipoDivisa(String tipoDivisa) {
		this.tipoDivisa = tipoDivisa;
	}

	public String getFolioOperacion() {
		return folioOperacion;
	}

	public void setFolioOperacion(String folioOperacion) {
		this.folioOperacion = folioOperacion;
	}

	public String getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(String formaPago) {
		this.formaPago = formaPago;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}

	public String getDescEstatus() {
		return descEstatus;
	}

	public void setDescEstatus(String descEstatus) {
		this.descEstatus = descEstatus;
	}

	public boolean isSpei() {
		return spei;
	}

	public void setSpei(boolean spei) {
		this.spei = spei;
	}

	@Override
	public String toString() {
		return "PagosModel [idPago=" + idPago + ", numeroCuenta=" + numeroCuenta + ", fechaHoraMovimiento="
				+ fechaHoraMovimiento + ", concepto=" + concepto + ", importe=" + importe + ", folioBanco=" + folioBanco
				+ ", refInterbancaria=" + refInterbancaria + ", folioCliente=" + folioCliente + ", tipoDivisa="
				+ tipoDivisa + ", folioOperacion=" + folioOperacion + ", formaPago=" + formaPago + ", estatus="
				+ estatus + ", descEstatus=" + descEstatus + "]";
	}
	
}

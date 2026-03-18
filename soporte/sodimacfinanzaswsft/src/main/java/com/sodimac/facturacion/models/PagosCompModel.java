package com.sodimac.facturacion.models;

public class PagosCompModel {

	private int id;
	private String numeroCuenta;
	private java.util.Date fechaHoraMovimiento;
	private double importe;
	
	private String folioBanco;
	private String refInterbancaria;
	private String folioCliente;
	private String tipoDivisa;
	private String folioOperacion;
	private int formaPago;
	private int folioFactura;
	
	public PagosCompModel() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public java.util.Date getFechaHoraMovimiento() {
		return fechaHoraMovimiento;
	}

	public void setFechaHoraMovimiento(java.util.Date fechaHoraMovimiento) {
		this.fechaHoraMovimiento = fechaHoraMovimiento;
	}

	public double getImporte() {
		return importe;
	}

	public void setImporte(double importe) {
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

	public int getFormaPago() {
		return formaPago;
	}

	public void setFormaPago(int formaPago) {
		this.formaPago = formaPago;
	}

	public int getFolioFactura() {
		return folioFactura;
	}

	public void setFolioFactura(int folioFactura) {
		this.folioFactura = folioFactura;
	}
	
}

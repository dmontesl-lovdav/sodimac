package com.sodimac.cfdi.models;

public class ComplementosModel {
	
	private String id;
	private String numeroCuenta;
	private String fechaHoraMovimiento;
	private String folioBanco;
	private String concepto;
	private String leyenda;
	private String refInterbancaria;
	private String folioCliente;
	private String tipoDivisa;
	private String folioOperacion;
	private String formaPago;
	private String rfc;
	private String transaccion;
	private String granTotal;
	private String totalTransaccion;
	private String saldoAnterior;
	private String importe;
	private String saldoPendiente;
	private String uuidRelacionado;
	private String estatus;
	private String descEstatus;
	private Integer estatusTimbrado;
	private Integer complementoAsignado;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getFolioBanco() {
		return folioBanco;
	}

	public void setFolioBanco(String folioBanco) {
		this.folioBanco = folioBanco;
	}

	public String getConcepto() {
		return concepto;
	}

	public void setConcepto(String concepto) {
		this.concepto = concepto;
	}

	public String getLeyenda() {
		return leyenda;
	}

	public void setLeyenda(String leyenda) {
		this.leyenda = leyenda;
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

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}

	public String getGranTotal() {
		return granTotal;
	}

	public void setGranTotal(String granTotal) {
		this.granTotal = granTotal;
	}

	public String getTotalTransaccion() {
		return totalTransaccion;
	}

	public void setTotalTransaccion(String totalTransaccion) {
		this.totalTransaccion = totalTransaccion;
	}

	public String getSaldoAnterior() {
		return saldoAnterior;
	}

	public void setSaldoAnterior(String saldoAnterior) {
		this.saldoAnterior = saldoAnterior;
	}

	public String getImporte() {
		return importe;
	}

	public void setImporte(String importe) {
		this.importe = importe;
	}

	public String getSaldoPendiente() {
		return saldoPendiente;
	}

	public void setSaldoPendiente(String saldoPendiente) {
		this.saldoPendiente = saldoPendiente;
	}

	public String getUuidRelacionado() {
		return uuidRelacionado;
	}

	public void setUuidRelacionado(String uuidRelacionado) {
		this.uuidRelacionado = uuidRelacionado;
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

	public Integer getEstatusTimbrado() {
		return estatusTimbrado;
	}

	public void setEstatusTimbrado(Integer estatusTimbrado) {
		this.estatusTimbrado = estatusTimbrado;
	}

	public Integer getComplementoAsignado() {
		return complementoAsignado;
	}

	public void setComplementoAsignado(Integer complementoAsignado) {
		this.complementoAsignado = complementoAsignado;
	}

	@Override
	public String toString() {
		return "ComplementosModel [id=" + id + ", numeroCuenta=" + numeroCuenta + ", fechaHoraMovimiento="
				+ fechaHoraMovimiento + ", folioBanco=" + folioBanco + ", concepto=" + concepto + ", leyenda=" + leyenda
				+ ", refInterbancaria=" + refInterbancaria + ", folioCliente=" + folioCliente + ", tipoDivisa="
				+ tipoDivisa + ", folioOperacion=" + folioOperacion + ", formaPago=" + formaPago + ", rfc=" + rfc
				+ ", transaccion=" + transaccion + ", granTotal=" + granTotal + ", totalTransaccion=" + totalTransaccion
				+ ", saldoAnterior=" + saldoAnterior + ", importe=" + importe + ", saldoPendiente=" + saldoPendiente
				+ ", uuidRelacionado=" + uuidRelacionado + ", estatus=" + estatus + ", descEstatus=" + descEstatus
				+ "]";
	}
	
}

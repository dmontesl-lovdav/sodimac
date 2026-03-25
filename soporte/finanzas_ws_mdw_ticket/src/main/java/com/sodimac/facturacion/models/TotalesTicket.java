package com.sodimac.facturacion.models;

public class TotalesTicket {

	String subtotal;
	String descuento;
	String moneda;
	String tipoCambio;
	String total;
	String importeLetra;
	String totalImpuestosTrasladados;

	public String getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(String subtotal) {
		this.subtotal = subtotal;
	}

	public String getDescuento() {
		return descuento;
	}

	public void setDescuento(String descuento) {
		this.descuento = descuento;
	}

	public String getMoneda() {
		return moneda;
	}

	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}

	public String getTipoCambio() {
		return tipoCambio;
	}

	public void setTipoCambio(String tipoCambio) {
		this.tipoCambio = tipoCambio;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getImporteLetra() {
		return importeLetra;
	}

	public void setImporteLetra(String importeLetra) {
		this.importeLetra = importeLetra;
	}

	public String getTotalImpuestosTrasladados() {
		return totalImpuestosTrasladados;
	}

	public void setTotalImpuestosTrasladados(String totalImpuestosTrasladados) {
		this.totalImpuestosTrasladados = totalImpuestosTrasladados;
	}

}

package com.sodimac.bctfacturacion.model;

public class FacturacionGlobalVentaModel {

	private String tienda;
	private String fecha;
	private String tipoTimbrado;
	private Integer total;
	private String orden;

	public String getTienda() {
		return tienda;
	}

	public void setTienda(String tienda) {
		this.tienda = tienda;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getTipoTimbrado() {
		return tipoTimbrado;
	}

	public void setTipoTimbrado(String tipoTimbrado) {
		this.tipoTimbrado = tipoTimbrado;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getOrden() {
		return orden;
	}

	public void setOrden(String orden) {
		this.orden = orden;
	}

	@Override
	public String toString() {
		return "FacturacionGlobalVentaModel [tienda=" + tienda + ", fecha=" + fecha + ", tipoTimbrado=" + tipoTimbrado
				+ ", total=" + total + ", orden=" + orden + "]";
	}
}

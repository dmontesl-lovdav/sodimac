package com.sodimac.cfdi.models;

public class TableroId {

	private String fechaTicket;
	private String tienda;
	
	public String getFechaTicket() {
		return fechaTicket;
	}
	public void setFechaTicket(String fechaTicket) {
		this.fechaTicket = fechaTicket;
	}
	public String getTienda() {
		return tienda;
	}
	public void setTienda(String tienda) {
		this.tienda = tienda;
	}
	
	@Override
	public String toString() {
		return "TableroId [fechaTicket=" + fechaTicket + ", tienda=" + tienda + "]";
	}
	
}

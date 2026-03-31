package com.sodimac.bctfacturacion.model;

import java.util.Date;

public class DevolucionDetalle {

	private String ticket;
	private Date fechaTicket;
	private Integer tienda;
	private Integer caja;
	private String numDocCanal;
	private String canalLinio;
	private Integer totalArticulo;
	private Date fechaCarga;
	private Integer cajaEstatusProceso;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Date getFechaTicket() {
		return fechaTicket;
	}

	public void setFechaTicket(Date fechaTicket) {
		this.fechaTicket = fechaTicket;
	}

	public Integer getTienda() {
		return tienda;
	}

	public void setTienda(Integer tienda) {
		this.tienda = tienda;
	}

	public Integer getCaja() {
		return caja;
	}

	public void setCaja(Integer caja) {
		this.caja = caja;
	}

	public String getNumDocCanal() {
		return numDocCanal;
	}

	public void setNumDocCanal(String numDocCanal) {
		this.numDocCanal = numDocCanal;
	}

	public String getCanalLinio() {
		return canalLinio;
	}

	public void setCanalLinio(String canalLinio) {
		this.canalLinio = canalLinio;
	}

	public Integer getTotalArticulo() {
		return totalArticulo;
	}

	public void setTotalArticulo(Integer totalArticulo) {
		this.totalArticulo = totalArticulo;
	}

	public Date getFechaCarga() {
		return fechaCarga;
	}

	public void setFechaCarga(Date fechaCarga) {
		this.fechaCarga = fechaCarga;
	}

	public Integer getCajaEstatusProceso() {
		return cajaEstatusProceso;
	}

	public void setCajaEstatusProceso(Integer cajaEstatusProceso) {
		this.cajaEstatusProceso = cajaEstatusProceso;
	}

	@Override
	public String toString() {
		return "DevolucionDetalle [ticket=" + ticket + ", fechaTicket=" + fechaTicket + ", tienda=" + tienda + ", caja="
				+ caja + ", numDocCanal=" + numDocCanal + ", canalLinio=" + canalLinio + ", totalArticulo="
				+ totalArticulo + ", fechaCarga=" + fechaCarga + ", cajaEstatusProceso=" + cajaEstatusProceso + "]";
	}

	
}

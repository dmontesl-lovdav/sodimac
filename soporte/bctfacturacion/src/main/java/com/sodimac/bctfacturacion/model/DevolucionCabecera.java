package com.sodimac.bctfacturacion.model;

import java.util.Date;
import java.util.List;

public class DevolucionCabecera {

	private String ticket;
	private Date fechaTicket;
	private Integer tienda;
	private Integer caja;
	private Integer transaccion;
	private Integer tipo;
	private Double total;
	private Double subtotal;
	private Double redondeo;
	private String ticketOrigen;
	private Date fechaEnlace;
	private Date fechaCarga;
	private Integer estatusProceso;
	private List<DevolucionDetalle> listDetalleDevolucion;

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

	public Integer getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(Integer transaccion) {
		this.transaccion = transaccion;
	}

	public Integer getTipo() {
		return tipo;
	}

	public void setTipo(Integer tipo) {
		this.tipo = tipo;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public Double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(Double subtotal) {
		this.subtotal = subtotal;
	}

	public Double getRedondeo() {
		return redondeo;
	}

	public void setRedondeo(Double redondeo) {
		this.redondeo = redondeo;
	}

	public String getTicketOrigen() {
		return ticketOrigen;
	}

	public void setTicketOrigen(String ticketOrigen) {
		this.ticketOrigen = ticketOrigen;
	}

	public Date getFechaEnlace() {
		return fechaEnlace;
	}

	public void setFechaEnlace(Date fechaEnlace) {
		this.fechaEnlace = fechaEnlace;
	}

	public Date getFechaCarga() {
		return fechaCarga;
	}

	public void setFechaCarga(Date fechaCarga) {
		this.fechaCarga = fechaCarga;
	}

	public Integer getEstatusProceso() {
		return estatusProceso;
	}

	public void setEstatusProceso(Integer estatusProceso) {
		this.estatusProceso = estatusProceso;
	}

	public List<DevolucionDetalle> getListDetalleDevolucion() {
		return listDetalleDevolucion;
	}

	public void setListDetalleDevolucion(List<DevolucionDetalle> listDetalleDevolucion) {
		this.listDetalleDevolucion = listDetalleDevolucion;
	}

	@Override
	public String toString() {
		return "DevolucionCabecera [ticket=" + ticket + ", fechaTicket=" + fechaTicket + ", tienda=" + tienda
				+ ", caja=" + caja + ", transaccion=" + transaccion + ", tipo=" + tipo + ", total=" + total
				+ ", subtotal=" + subtotal + ", redondeo=" + redondeo + ", ticketOrigen=" + ticketOrigen
				+ ", fechaEnlace=" + fechaEnlace + ", fechaCarga=" + fechaCarga + ", estatusProceso=" + estatusProceso
				+ ", listDetalleDevolucion=" + listDetalleDevolucion + "]";
	}
}

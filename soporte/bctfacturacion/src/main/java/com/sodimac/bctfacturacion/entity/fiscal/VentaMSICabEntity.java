package com.sodimac.bctfacturacion.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "VENTA_CAB")
public class VentaMSICabEntity {

	@Id
	@Column(name = "TICKET")
	private String ticket;

	@Column(name = "FECHA_TICKET")
	private Date fechaTicket;

	@Column(name = "TIENDA")
	private Integer tienda;

	@Column(name = "CAJA")
	private Integer caja;

	@Column(name = "TRANSACCION")
	private Integer transaccion;

	@Column(name = "TIPO")
	private Integer tipo;

	@Column(name = "TOTAL")
	private Double total;

	@Column(name = "SUBTOTAL")
	private Double subtotal;

	@Column(name = "REDONDEO")
	private Double redondeo;

	@Column(name = "TICKET_ORIGEN")
	private String ticketOrigen;

	@Column(name = "FECHA_ENLACE")
	private Date fechaEnlace;

	@Column(name = "FECHA_CARGA")
	private Date fechaCarga;

	@Column(name = "ESTATUS_PROCESO")
	private Integer estatusProceso;

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

}

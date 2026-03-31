package com.sodimac.bctfacturacion.entity.bct;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TRX_HDR")
public class ViewCinfoTrjBnc {
	
	@Column(name = "NUM_TICKET")
	private String ticket;

	@Column(name = "FECHA_TRX")
	private Date fechaTicket;

	@Column(name = "NUM_TIENDA")
	private Integer tienda;

	@Column(name = "NUM_CAJA")
	private Integer caja;

	@Id
	@Column(name = "NUM_TRX")
	private String transaccion;

	@Column(name = "TIPO_TRX")
	private Integer tipo;

	@Column(name = "MNT_TOTAL_A_PAGAR")
	private Double total;

	@Column(name = "MNT_TOT_SN_IMPTOS")
	private Double subtotal;

	@Column(name = "MNT_REDONDEO")
	private Double redondeo;

	@Column(name = "TRX_ORIGINAL")
	private String ticketOrigen;

	@Column(name = "FECHA_BCT")
	private Date fechaEnlace;

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

	public String getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(String transaccion) {
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
}

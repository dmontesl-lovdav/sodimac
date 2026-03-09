package com.sodimac.cfdi.entity.fiscal;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class TableroControlTimbradoId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "FECHA_TICKET")
	private Date fechaTicket;
	@Column(name = "TIENDA")
	private Integer tienda;
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

	
}

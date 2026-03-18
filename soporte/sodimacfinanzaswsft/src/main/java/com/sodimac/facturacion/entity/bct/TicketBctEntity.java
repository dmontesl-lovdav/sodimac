package com.sodimac.facturacion.entity.bct;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TRX_HDR")
public class TicketBctEntity {

	@Id
	@Column(name = "NUM_TRX")
	private String ticket;

	@Column(name = "NUM_TIENDA")
	private String tienda;
	
	public TicketBctEntity() {

	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getTienda() {
		return tienda;
	}

	public void setTienda(String tienda) {
		this.tienda = tienda;
	}
	
}

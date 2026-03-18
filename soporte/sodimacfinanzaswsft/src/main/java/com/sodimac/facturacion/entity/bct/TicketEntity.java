package com.sodimac.facturacion.entity.bct;

import java.math.BigDecimal;
import java.util.Date;

public class TicketEntity {

	private String ticket;
	private Date fecha;
	private int tienda;
	private int caja;
	private float cajero;
	private int numTicket;
	private int tipoTicket;
	private BigDecimal totalPagar;
	private BigDecimal totalSinImpuestos;
	private String original;
	private String devolucion;

	public TicketEntity() {

	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public int getNumTicket() {
		return numTicket;
	}

	public void setNumTicket(int numTicket) {
		this.numTicket = numTicket;
	}

	public int getTipoTicket() {
		return tipoTicket;
	}

	public void setTipoTicket(int tipoTicket) {
		this.tipoTicket = tipoTicket;
	}

	public BigDecimal getTotalPagar() {
		return totalPagar;
	}

	public void setTotalPagar(BigDecimal totalPagar) {
		this.totalPagar = totalPagar;
	}

	public int getTienda() {
		return tienda;
	}

	public void setTienda(int tienda) {
		this.tienda = tienda;
	}

	public int getCaja() {
		return caja;
	}

	public void setCaja(int caja) {
		this.caja = caja;
	}

	public float getCajero() {
		return cajero;
	}

	public void setCajero(float cajero) {
		this.cajero = cajero;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public BigDecimal getTotalSinImpuestos() {
		return totalSinImpuestos;
	}

	public void setTotalSinImpuestos(BigDecimal totalSinImpuestos) {
		this.totalSinImpuestos = totalSinImpuestos;
	}

	public String getDevolucion() {
		return devolucion;
	}

	public void setDevolucion(String devolucion) {
		this.devolucion = devolucion;
	}

	@Override
	public String toString() {
		return "TicketEntity [ticket=" + ticket + ", fecha=" + fecha + ", tienda=" + tienda + ", caja=" + caja
				+ ", cajero=" + cajero + ", numTicket=" + numTicket + ", tipoTicket=" + tipoTicket + ", totalPagar="
				+ totalPagar + ", totalSinImpuestos=" + totalSinImpuestos + ", original=" + original + ", devolucion="
				+ devolucion + "]";
	}
	
}

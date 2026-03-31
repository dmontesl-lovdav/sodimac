package com.sodimac.bctfacturacion.entity.fiscal;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "FACTURACION_CLIENTE")
public class FacturacionClienteEntity {

	@Id
	@Column(name = "TICKET")
	private String ticket;

	@Column(name = "FECHA_TIMBRADO")
	private Date fechaTimbrado;

	@Column(name = "UUID")
	private String uuid;

	@Column(name = "TRANSACCION")
	private String transaccion;

	@Column(name = "SERIE")
	private String serie;

	@Column(name = "FOLIO")
	private String folio;

	@Column(name = "TIENDA")
	private String tienda;

	@Column(name = "FechaCarga")
	private Date fechaCarga;

	@Column(name = "SUBTOTAL")
	private Double subtotal;

	@Column(name = "TOTAL")
	private Double total;

	@Column(name = "FECHA_TICKET")
	private Date fechaTicket;

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public Date getFechaTimbrado() {
		return fechaTimbrado;
	}

	public void setFechaTimbrado(Date fechaTimbrado) {
		this.fechaTimbrado = fechaTimbrado;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTransaccion() {
		return transaccion;
	}

	public void setTransaccion(String transaccion) {
		this.transaccion = transaccion;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public String getTienda() {
		return tienda;
	}

	public void setTienda(String tienda) {
		this.tienda = tienda;
	}

	public Date getFechaCarga() {
		return fechaCarga;
	}

	public void setFechaCarga(Date fechaCarga) {
		this.fechaCarga = fechaCarga;
	}

	public Double getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(Double subtotal) {
		this.subtotal = subtotal;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public Date getFechaTicket() {
		return fechaTicket;
	}

	public void setFechaTicket(Date fechaTicket) {
		this.fechaTicket = fechaTicket;
	}

	@Override
	public String toString() {
		return "FacturacionClienteEntity [ticket=" + ticket + ", fechaTimbrado=" + fechaTimbrado + ", uuid=" + uuid
				+ ", transaccion=" + transaccion + ", serie=" + serie + ", folio=" + folio + ", tienda=" + tienda
				+ ", fechaCarga=" + fechaCarga + ", subtotal=" + subtotal + ", total=" + total + ", fechaTicket="
				+ fechaTicket + "]";
	}
	
	

}

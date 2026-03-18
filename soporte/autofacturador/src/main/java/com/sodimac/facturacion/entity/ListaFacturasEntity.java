package com.sodimac.facturacion.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "listafacturas")
public class ListaFacturasEntity {

	@Id
	@Column(name = "idListaFacturas")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int idListaFacturas;
	private String ticket;
	@Column(precision=11, scale=2)
	private BigDecimal total;
	private int status;
	private String sessionId;

	@Column(name = "fechaIngreso")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaIngreso;

	public ListaFacturasEntity() {

	}

	public ListaFacturasEntity(String ticket, int status, String sessionId) {
		this.ticket = ticket;
		this.status = status;
		this.sessionId = sessionId;
	}

	public int getIdListaFacturas() {
		return idListaFacturas;
	}

	public void setIdListaFacturas(int idListaFacturas) {
		this.idListaFacturas = idListaFacturas;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public java.util.Date getFechaIngreso() {
		return fechaIngreso;
	}

	public void setFechaIngreso(java.util.Date fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "ListaFacturasEntity [idListaFacturas=" + idListaFacturas + ", ticket=" + ticket + ", total=" + total
				+ ", status=" + status + ", sessionId=" + sessionId + ", fechaIngreso=" + fechaIngreso + "]";
	}

}

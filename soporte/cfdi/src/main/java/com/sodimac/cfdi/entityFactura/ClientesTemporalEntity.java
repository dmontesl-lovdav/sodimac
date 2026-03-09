package com.sodimac.cfdi.entityFactura;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "clientesTemporal")
public class ClientesTemporalEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idClientesTemporal")
	private int id;

	@Column(name = "rfc")
	private String rfc;

	@Column(name = "ticket")
	private String ticket;

	@Column(name = "razonSocial")
	private String razonSocial;

	@Column(name = "idUsoCfdi")
	private int idUsoCfdi;

	@Column(name = "email")
	private String email;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	public ClientesTemporalEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
	}

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public int getIdUsoCfdi() {
		return idUsoCfdi;
	}

	public void setIdUsoCfdi(int idUsoCfdi) {
		this.idUsoCfdi = idUsoCfdi;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	@Override
	public String toString() {
		return "ClientesTemporalEntity [id=" + id + ", rfc=" + rfc + ", ticket=" + ticket + ", razonSocial="
				+ razonSocial + ", idUsoCfdi=" + idUsoCfdi + ", email=" + email + ", activo=" + activo
				+ ", fechaCreacion=" + fechaCreacion + "]";
	}

}

package com.sodimac.cfdi.entity.fiscal;

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
@Table(name = "clientes")
public class ClientesEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idCliente")
	private int idCliente;

	@Column(name = "rfc")
	private String rfc;

	@Column(name = "razonSocial")
	private String razonSocial;

	@Column(name = "idUsoCfdi")
	private int idUsoCfdi;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "idOrigen")
	private int idOrigen;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	
	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	public int getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}

	public String getRfc() {
		return rfc;
	}

	public void setRfc(String rfc) {
		this.rfc = rfc;
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

	public int getIdOrigen() {
		return idOrigen;
	}

	public void setIdOrigen(int idOrigen) {
		this.idOrigen = idOrigen;
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
		return "ClientesEntity [idCliente=" + idCliente + ", rfc=" + rfc + ", razonSocial=" + razonSocial
				+ ", idUsoCfdi=" + idUsoCfdi + ", email=" + email + ", idOrigen=" + idOrigen + ", activo=" + activo
				+ ", fechaCreacion=" + fechaCreacion + "]";
	}

	
}
package com.sodimac.facturacion.entity.fac;

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
@Table(name = "catRfcEmisor")
public class CatRfcEmisorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idRfcEmisor")
	private int idRfcEmisor;

	@Column(name = "rfc")
	private String rfc;

	@Column(name = "razonSocial")
	private String razonSocial;

	@Column(name = "idEstatus")
	private int idEstatus;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	@Column(name = "fechaModificacion")
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaModificacion;

	public CatRfcEmisorEntity() {

	}

	public int getIdRfcEmisor() {
		return idRfcEmisor;
	}

	public void setIdRfcEmisor(int idRfcEmisor) {
		this.idRfcEmisor = idRfcEmisor;
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

	public int getIdEstatus() {
		return idEstatus;
	}

	public void setIdEstatus(int idEstatus) {
		this.idEstatus = idEstatus;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public java.util.Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(java.util.Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	@Override
	public String toString() {
		return "CatRfcEmisorEntity [idRfcEmisor=" + idRfcEmisor + ", rfc=" + rfc + ", razonSocial=" + razonSocial
				+ ", idEstatus=" + idEstatus + ", fechaCreacion=" + fechaCreacion + ", fechaModificacion="
				+ fechaModificacion + "]";
	}

}

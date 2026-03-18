package com.sodimac.facturacion.entity;

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
@Table(name = "confDatosEmisor")
public class ConfDatosEmisorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idConfDatosEmisor")
	private int idConfDatosEmisor;

	@Column(name = "rfc")
	private String rfc;

	@Column(name = "razonSocial")
	private String razonSocial;

	@Column(name = "regimenFiscal")
	private String regimenFiscal;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	public ConfDatosEmisorEntity() {

	}

	public int getIdConfDatosEmisor() {
		return idConfDatosEmisor;
	}

	public void setIdConfDatosEmisor(int idConfDatosEmisor) {
		this.idConfDatosEmisor = idConfDatosEmisor;
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

	public String getRegimenFiscal() {
		return regimenFiscal;
	}

	public void setRegimenFiscal(String regimenFiscal) {
		this.regimenFiscal = regimenFiscal;
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
		return "ConfDatosEmisorEntity [idConfDatosEmisor=" + idConfDatosEmisor + ", rfc=" + rfc + ", razonSocial="
				+ razonSocial + ", regimenFiscal=" + regimenFiscal + ", activo=" + activo + ", fechaCreacion="
				+ fechaCreacion + "]";
	}

}

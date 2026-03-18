package com.sodimac.facturacion.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "catConfiguracion")
public class CatConfiguracionEntity {

	@Id
	@Column(name = "nombreCampo")
	private String nombreCampo;

	@Lob
	@Column(name = "valor")
	private String valor;

	@Column(name = "aplicacion")
	private String aplicacion;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "fechaCreacion")
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date fechaCreacion;

	public CatConfiguracionEntity() {

	}

	public String getNombreCampo() {
		return nombreCampo;
	}

	public void setNombreCampo(String nombreCampo) {
		this.nombreCampo = nombreCampo;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getAplicacion() {
		return aplicacion;
	}

	public void setAplicacion(String aplicacion) {
		this.aplicacion = aplicacion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public java.util.Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(java.util.Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	@Override
	public String toString() {
		return "CatConfiguracionEntity [nombreCampo=" + nombreCampo + ", valor=" + valor + ", aplicacion=" + aplicacion
				+ ", descripcion=" + descripcion + ", fechaCreacion=" + fechaCreacion + "]";
	}
	
}

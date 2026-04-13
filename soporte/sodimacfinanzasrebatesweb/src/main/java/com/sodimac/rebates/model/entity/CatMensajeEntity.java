package com.sodimac.rebates.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CatMensaje")
public class CatMensajeEntity {

	@Id
	@Column(name = "IdMensaje")
	private Integer idMensaje;

	@Column(name = "Clave")
	private String clave;

	@Column(name = "Descripcion")
	private String descripcion;

	@Column(name = "Activo")
	private boolean activo;

	public Integer getIdMensaje() {
		return idMensaje;
	}

	public void setIdMensaje(Integer idMensaje) {
		this.idMensaje = idMensaje;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

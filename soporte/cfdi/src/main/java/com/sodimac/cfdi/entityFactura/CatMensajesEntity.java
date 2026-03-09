package com.sodimac.cfdi.entityFactura;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "catMensajes")
public class CatMensajesEntity {

	@Id
	@Column(name = "idMensaje")
	private int idMensaje;

	@Column(name = "descripcionMensaje")
	private String descripcionMensaje;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	

	public CatMensajesEntity() {

	}


	public int getIdMensaje() {
		return idMensaje;
	}


	public void setIdMensaje(int idMensaje) {
		this.idMensaje = idMensaje;
	}


	public String getDescripcionMensaje() {
		return descripcionMensaje;
	}


	public void setDescripcionMensaje(String descripcionMensaje) {
		this.descripcionMensaje = descripcionMensaje;
	}


	public boolean isActivo() {
		return activo;
	}


	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
	public String toString() {
		return "CatMensajesEntity [idMensaje=" + idMensaje + ", descripcionMensaje=" + descripcionMensaje + ", activo="
				+ activo + "]";
	}
	
}

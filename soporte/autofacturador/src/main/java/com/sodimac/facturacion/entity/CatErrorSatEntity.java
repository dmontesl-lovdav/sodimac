package com.sodimac.facturacion.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "caterrorsat")
public class CatErrorSatEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idCatErrorSat")
	private int idCatErrorSat;

	@Column(name = "claveError")
	private String claveError;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;

	public int getIdCatErrorSat() {
		return idCatErrorSat;
	}

	public void setIdCatErrorSat(int idCatErrorSat) {
		this.idCatErrorSat = idCatErrorSat;
	}

	public String getClaveError() {
		return claveError;
	}

	public void setClaveError(String claveError) {
		this.claveError = claveError;
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

package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "catperiodicidad")
public class CatPeriodicidadEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idPeriodicidad")
	private Integer idPeriodicidad;

	@Column(name = "clave")
	private String clave;

	@Column(name = "descripcion")
	private String descripcion;
	
	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;

	public Integer getIdPeriodicidad() {
		return idPeriodicidad;
	}

	public void setIdPeriodicidad(Integer idPeriodicidad) {
		this.idPeriodicidad = idPeriodicidad;
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

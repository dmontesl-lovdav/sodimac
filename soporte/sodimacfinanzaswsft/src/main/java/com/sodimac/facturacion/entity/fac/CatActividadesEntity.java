package com.sodimac.facturacion.entity.fac;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "catActividades")
public class CatActividadesEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idActividad")
	private int idActividad;

	@Column(name = "actividad")
	private String actividad;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "activo", nullable = false, columnDefinition = "BIT", length = 1)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	private boolean activo = true;
	

	public CatActividadesEntity() {

	}


	public int getIdActividad() {
		return idActividad;
	}


	public void setIdActividad(int idActividad) {
		this.idActividad = idActividad;
	}


	public String getActividad() {
		return actividad;
	}


	public void setActividad(String actividad) {
		this.actividad = actividad;
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

	@Override
	public String toString() {
		return "CatActividadesEntity [idActividad=" + idActividad + ", actividad=" + actividad + ", descripcion="
				+ descripcion + ", activo=" + activo + "]";
	}

}

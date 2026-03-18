package com.sodimac.wsconfiguracion.dto;

public class PeriodicidadDto {

	private Integer idPeriodicidad;
	private String clave;
	private String descripcion;
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

	@Override
	public String toString() {
		return "PeriodicidadDto [idPeriodicidad=" + idPeriodicidad + ", clave=" + clave + ", descripcion=" + descripcion
				+ ", activo=" + activo + "]";
	}
}

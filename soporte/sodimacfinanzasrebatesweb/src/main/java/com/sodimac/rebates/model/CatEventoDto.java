package com.sodimac.rebates.model;

public class CatEventoDto {

	private Integer idCatEvento;
	private String clave;
	private String descripcion;
	private boolean activo;

	public Integer getIdCatEvento() {
		return idCatEvento;
	}

	public void setIdCatEvento(Integer idCatEvento) {
		this.idCatEvento = idCatEvento;
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

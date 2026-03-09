package com.sodimac.cfdi.models;

public class CatTiendaModel {

	private Integer id;
	private String nombre;
	private Integer descripcion;
	private boolean activo;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(Integer descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

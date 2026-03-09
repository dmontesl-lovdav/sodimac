package com.sodimac.cfdi.models.documento;

public class CatDocumentoCabeceraDetModel {

	private Integer idDocumentoCabeceraDet;
	private Integer idDocumentoCabecera;
	private String nombre;
	private Integer posicion;
	private Integer estatus;

	public Integer getIdDocumentoCabeceraDet() {
		return idDocumentoCabeceraDet;
	}

	public void setIdDocumentoCabeceraDet(Integer idDocumentoCabeceraDet) {
		this.idDocumentoCabeceraDet = idDocumentoCabeceraDet;
	}

	public Integer getIdDocumentoCabecera() {
		return idDocumentoCabecera;
	}

	public void setIdDocumentoCabecera(Integer idDocumentoCabecera) {
		this.idDocumentoCabecera = idDocumentoCabecera;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getPosicion() {
		return posicion;
	}

	public void setPosicion(Integer posicion) {
		this.posicion = posicion;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

}

package com.sodimac.cfdi.entity.fiscal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catdocumentocabeceradet")
public class CatDocumentoCabeceraDetEntity {

	@Id
	@Column(name = "idDocumentoCabeceraDet")
	private Integer idDocumentoCabeceraDet;

	@Column(name = "idDocumentoCabecera")
	private Integer idDocumentoCabecera;

	@Column(name = "nombre")
	private String nombre;

	@Column(name = "posicion")
	private Integer posicion;

	@Column(name = "estatus")
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

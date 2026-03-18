package com.sodimac.facturacion.entity.fac.catalogospdf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catCfdi")
public class CatCfdiEntity {

	@Id
	@Column(name = "idCfdi")
	private int idCfdi;

	@Column(name = "descripcion")
	private String descripcion;

	public CatCfdiEntity() {

	}

	public int getIdCfdi() {
		return idCfdi;
	}

	public void setIdCfdi(int idCfdi) {
		this.idCfdi = idCfdi;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "CatCfdiEntity [idCfdi=" + idCfdi + ", descripcion=" + descripcion + "]";
	}
	
}

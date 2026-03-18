package com.sodimac.facturacion.entity.fac.catalogospdf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catMoneda")
public class CatMonedaEntity {

	@Id
	@Column(name = "idMoneda")
	private String idMoneda;

	@Column(name = "descripcion")
	private String descripcion;

	public CatMonedaEntity() {

	}

	public String getIdMoneda() {
		return idMoneda;
	}


	public void setIdMoneda(String idMoneda) {
		this.idMoneda = idMoneda;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "CatMonedaEntity [idMoneda=" + idMoneda + ", descripcion=" + descripcion + "]";
	}
	
}

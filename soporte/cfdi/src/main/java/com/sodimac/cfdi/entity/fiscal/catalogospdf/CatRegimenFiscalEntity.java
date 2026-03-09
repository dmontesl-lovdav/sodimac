package com.sodimac.cfdi.entity.fiscal.catalogospdf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catregimenfiscal")
public class CatRegimenFiscalEntity {

	@Id
	@Column(name = "idRegimenFiscal")
	private int idRegimenFiscal;

	@Column(name = "descripcion")
	private String descripcion;

	public CatRegimenFiscalEntity() {

	}

	public int getIdRegimenFiscal() {
		return idRegimenFiscal;
	}


	public void setIdRegimenFiscal(int idRegimenFiscal) {
		this.idRegimenFiscal = idRegimenFiscal;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "CatRegimenFiscalEntity [idRegimenFiscal=" + idRegimenFiscal + ", descripcion=" + descripcion + "]";
	}
	
}

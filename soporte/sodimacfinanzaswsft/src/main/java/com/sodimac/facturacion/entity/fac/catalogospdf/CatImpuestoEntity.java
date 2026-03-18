package com.sodimac.facturacion.entity.fac.catalogospdf;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catImpuesto")
public class CatImpuestoEntity {

	@Id
	public String idImpuesto;
	
	public String descripcion;
	
	public String tipoImpuesto;

	
	public String getTipoImpuesto() {
		return tipoImpuesto;
	}

	public void setTipoImpuesto(String tipoImpuesto) {
		this.tipoImpuesto = tipoImpuesto;
	}

	public String getIdImpuesto() {
		return idImpuesto;
	}

	public void setIdImpuesto(String idImpuesto) {
		this.idImpuesto = idImpuesto;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "CatImpuestoEntity [idImpuesto=" + idImpuesto + ", descripcion=" + descripcion + ", tipoImpuesto="
				+ tipoImpuesto + "]";
	}
	
	
}

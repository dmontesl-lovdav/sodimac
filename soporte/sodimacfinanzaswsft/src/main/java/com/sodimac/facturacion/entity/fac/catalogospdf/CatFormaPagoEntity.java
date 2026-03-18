package com.sodimac.facturacion.entity.fac.catalogospdf;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catFormaPago")
public class CatFormaPagoEntity {

	
	@Id
	public String idFormaPago;
	
	public String descripcion;

	public String getIdFormaPago() {
		return idFormaPago;
	}

	public void setIdFormaPago(String idFormaPago) {
		this.idFormaPago = idFormaPago;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "CatFormaPagoEntity [idFormaPago=" + idFormaPago + ", descripcion=" + descripcion + "]";
	}
	
	
	
}

package com.sodimac.facturacion.entity.fac.catalogospdf;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catMetodoPago")
public class CatMetodoPagoEntity {

	@Id
	public String idMetodoPago;
	
	public String descripcion;
	

	public String getIdMetodoPago() {
		return idMetodoPago;
	}
	public void setIdMetodoPago(String idMetodoPago) {
		this.idMetodoPago = idMetodoPago;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "CatMetodoPagoEntity [idMetodoPago=" + idMetodoPago + ", descripcion=" + descripcion + "]";
	}


	
}

package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catmetodopago")
public class CatMetodoPagoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "idMetodoPago")
	private String idMetodoPago;
	
	@Column(name = "descripcion")
	private String descripcion;

		
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


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
		return "CatMetodoPagoEntity [id=" + id + ", idMetodoPago=" + idMetodoPago + ", descripcion=" + descripcion
				+ "]";
	}
	
}

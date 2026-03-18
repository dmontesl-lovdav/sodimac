package com.sodimac.wsconfiguracion.entity.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "catformapago")
public class CatFormaPagoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "idFormaPago")
	private String idFormaPago;
	
	@Column(name = "descripcion")
	private String descripcion;

	
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


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
		return "CatFormaPagoEntity [id=" + id + ", idFormaPago=" + idFormaPago + ", descripcion=" + descripcion + "]";
	}
	
	
}

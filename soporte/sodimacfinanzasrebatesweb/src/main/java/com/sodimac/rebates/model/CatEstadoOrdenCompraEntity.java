package com.sodimac.rebates.model;

import javax.persistence.*;

@Entity
@Table(name = "CatEstadoOrdenCompra")
public class CatEstadoOrdenCompraEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idEstadoOrdenCompra")
	private Integer idEstadoOrdenCompra;
	
	@Column(name = "nombre")
	private String nombre;
	
	@Column(name = "descripcion")
	private String descripcion;
	
	@Column(name = "estatus")
	private Integer estatus;

	public Integer getIdEstadoOrdenCompra() {
		return idEstadoOrdenCompra;
	}

	public void setIdEstadoOrdenCompra(Integer idEstadoOrdenCompra) {
		this.idEstadoOrdenCompra = idEstadoOrdenCompra;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

}

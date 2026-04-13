package com.sodimac.rebates.model;

import javax.persistence.*;

@Entity
@Table(name = "CatTipoOrdenCompra")
public class CatTipoOrdenCompraEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idTipoOrdenCompra")
	private Integer idTipoOrdenCompra;
	
	@Column(name = "nombre")
	private String nombre;
	
	@Column(name = "descripcion")
	private String descripcion;
	
	@Column(name = "estatus")
	private Integer estatus;
	
	public Integer getIdTipoOrdenCompra() {
		return idTipoOrdenCompra;
	}

	public void setIdTipoOrdenCompra(Integer idTipoOrdenCompra) {
		this.idTipoOrdenCompra = idTipoOrdenCompra;
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

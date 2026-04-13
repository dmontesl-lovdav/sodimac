package com.sodimac.rebates.model;

import javax.persistence.*;

@Entity
@Table(name = "CatTienda")
public class CatTiendaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdCatTienda")
	private Integer idCatTienda;
	
	@Column(name = "Nombre")
	private String nombre;
	
	@Column(name = "NumeroTienda")
	private Integer numeroTienda;
	
	@Column(name = "IdCatTipoTienda")
	private Integer idCatTipoTienda;
	
	@Column(name = "Activo")
	private boolean activo;

	public Integer getIdCatTienda() {
		return idCatTienda;
	}

	public void setIdCatTienda(Integer idCatTienda) {
		this.idCatTienda = idCatTienda;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getNumeroTienda() {
		return numeroTienda;
	}

	public void setNumeroTienda(Integer numeroTienda) {
		this.numeroTienda = numeroTienda;
	}

	public Integer getIdCatTipoTienda() {
		return idCatTipoTienda;
	}

	public void setIdCatTipoTienda(Integer idCatTipoTienda) {
		this.idCatTipoTienda = idCatTipoTienda;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
}

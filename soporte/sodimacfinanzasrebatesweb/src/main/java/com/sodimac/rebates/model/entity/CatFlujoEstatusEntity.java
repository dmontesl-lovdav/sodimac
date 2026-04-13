package com.sodimac.rebates.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "CatFlujoEstatus")
public class CatFlujoEstatusEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "IdCatFlujoEstatus")
	private Integer IdCatFlujoEstatus;

	@Column(name = "EstatusOrigen")
	private Integer estatusOrigen;

	@Column(name = "EstatusDestino")
	private Integer estatusDestino;

	@JoinColumn(name = "IdCatRol", referencedColumnName = "id")
	@ManyToOne(optional = false)
	private CatRolEntity rol;

	@JoinColumn(name = "IdCatEvento", referencedColumnName = "IdCatEvento")
	@ManyToOne(optional = false)
	private CatEventoEntity evento;

	@Column(name = "Activo")
	private boolean activo;

	public Integer getIdCatFlujoEstatus() {
		return IdCatFlujoEstatus;
	}

	public void setIdCatFlujoEstatus(Integer idCatFlujoEstatus) {
		IdCatFlujoEstatus = idCatFlujoEstatus;
	}

	public Integer getEstatusOrigen() {
		return estatusOrigen;
	}

	public void setEstatusOrigen(Integer estatusOrigen) {
		this.estatusOrigen = estatusOrigen;
	}

	public Integer getEstatusDestino() {
		return estatusDestino;
	}

	public void setEstatusDestino(Integer estatusDestino) {
		this.estatusDestino = estatusDestino;
	}

	public CatRolEntity getRol() {
		return rol;
	}

	public void setRol(CatRolEntity rol) {
		this.rol = rol;
	}

	public CatEventoEntity getEvento() {
		return evento;
	}

	public void setEvento(CatEventoEntity evento) {
		this.evento = evento;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

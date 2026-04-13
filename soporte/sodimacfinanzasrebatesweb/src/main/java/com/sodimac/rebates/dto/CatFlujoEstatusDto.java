package com.sodimac.rebates.dto;

import com.sodimac.rebates.model.CatEventoDto;

public class CatFlujoEstatusDto {

	private Integer idCatFlujoEstatus;
	private Integer estatusOrigen;
	private Integer estatusDestino;
	private CatRolDto rol;
	private CatEventoDto evento;
	private boolean activo;

	public Integer getidCatFlujoEstatus() {
		return idCatFlujoEstatus;
	}

	public void setIdCatFlujoEstatus(Integer idCatFlujoEstatus) {
		this.idCatFlujoEstatus = idCatFlujoEstatus;
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

	public CatRolDto getRol() {
		return rol;
	}

	public void setRol(CatRolDto rol) {
		this.rol = rol;
	}

	public CatEventoDto getEvento() {
		return evento;
	}

	public void setEvento(CatEventoDto evento) {
		this.evento = evento;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

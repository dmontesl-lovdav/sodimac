package com.sodimac.rebates.dto;

import java.util.Date;

public class ExclusionDetDto {

	private Integer idExclusionDet;
	private Integer idExclusion;
	private Long idCarga;
	private String motivo;
	private Date fechaRegistro;
	private boolean activo;

	public Integer getIdExclusionDet() {
		return idExclusionDet;
	}

	public void setIdExclusionDet(Integer idExclusionDet) {
		this.idExclusionDet = idExclusionDet;
	}

	public Integer getIdExclusion() {
		return idExclusion;
	}

	public void setIdExclusion(Integer idExclusion) {
		this.idExclusion = idExclusion;
	}

	public Long getIdCarga() {
		return idCarga;
	}

	public void setIdCarga(Long idCarga) {
		this.idCarga = idCarga;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public Date getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(Date fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

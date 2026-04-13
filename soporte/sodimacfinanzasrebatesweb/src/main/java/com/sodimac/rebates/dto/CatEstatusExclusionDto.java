package com.sodimac.rebates.dto;

public class CatEstatusExclusionDto {

	private Integer idCatEstatusExclusion;
	private String clave;
	private String descripcion;
	private boolean activo;

	public CatEstatusExclusionDto() {
		super();
	}

	public CatEstatusExclusionDto(Integer idCatEstatusExclusion) {
		super();
		this.idCatEstatusExclusion = idCatEstatusExclusion;
	}

	public Integer getIdCatEstatusExclusion() {
		return idCatEstatusExclusion;
	}

	public void setIdCatEstatusExclusion(Integer idCatEstatusExclusion) {
		this.idCatEstatusExclusion = idCatEstatusExclusion;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

package com.sodimac.rebates.dto;

public class CatTipoExclusionDto {

	private Integer idCatTipoExclusion;
	private String clave;
	private String descripcion;
	private boolean activo;

	public Integer getIdCatTipoExclusion() {
		return idCatTipoExclusion;
	}

	public void setIdCatTipoExclusion(Integer idCatTipoExclusion) {
		this.idCatTipoExclusion = idCatTipoExclusion;
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

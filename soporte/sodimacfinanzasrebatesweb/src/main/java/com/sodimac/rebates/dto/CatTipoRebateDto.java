package com.sodimac.rebates.dto;

public class CatTipoRebateDto {

	private Integer idCatTipoRebate;
	private String tipoRebate;
	private boolean activo;
	private String nomenclatura;

	public Integer getIdCatTipoRebate() {
		return idCatTipoRebate;
	}

	public void setIdCatTipoRebate(Integer idCatTipoRebate) {
		this.idCatTipoRebate = idCatTipoRebate;
	}

	public String getTipoRebate() {
		return tipoRebate;
	}

	public void setTipoRebate(String tipoRebate) {
		this.tipoRebate = tipoRebate;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public String getNomenclatura() {
		return nomenclatura;
	}

	public void setNomenclatura(String nomenclatura) {
		this.nomenclatura = nomenclatura;
	}

	@Override
	public String toString() {
		return "CatTipoRebateDto [idCatTipoRebate=" + idCatTipoRebate + ", tipoRebate=" + tipoRebate + ", activo="
				+ activo + ", nomenclatura=" + nomenclatura + "]";
	}

}

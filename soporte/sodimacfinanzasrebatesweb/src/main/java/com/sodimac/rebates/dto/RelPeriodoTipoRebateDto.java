package com.sodimac.rebates.dto;

public class RelPeriodoTipoRebateDto {

	private Integer id;
	private PeriodoDto periodo;
	private CatTipoRebateDto catTipoRebate;
	private boolean activo;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public PeriodoDto getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PeriodoDto periodo) {
		this.periodo = periodo;
	}

	public CatTipoRebateDto getCatTipoRebate() {
		return catTipoRebate;
	}

	public void setCatTipoRebate(CatTipoRebateDto catTipoRebate) {
		this.catTipoRebate = catTipoRebate;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

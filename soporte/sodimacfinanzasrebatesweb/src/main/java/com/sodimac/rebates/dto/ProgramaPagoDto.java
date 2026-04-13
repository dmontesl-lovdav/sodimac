package com.sodimac.rebates.dto;

public class ProgramaPagoDto {

	private Integer idCatProgramaPago;
	private String programaPago;
	private Integer numeroMeses;
	private String nomenclatura;
	private boolean activo;

	public Integer getIdCatProgramaPago() {
		return idCatProgramaPago;
	}

	public void setIdCatProgramaPago(Integer idCatProgramaPago) {
		this.idCatProgramaPago = idCatProgramaPago;
	}

	public String getProgramaPago() {
		return programaPago;
	}

	public void setProgramaPago(String programaPago) {
		this.programaPago = programaPago;
	}

	public Integer getNumeroMeses() {
		return numeroMeses;
	}

	public void setNumeroMeses(Integer numeroMeses) {
		this.numeroMeses = numeroMeses;
	}

	public String getNomenclatura() {
		return nomenclatura;
	}

	public void setNomenclatura(String nomenclatura) {
		this.nomenclatura = nomenclatura;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

}

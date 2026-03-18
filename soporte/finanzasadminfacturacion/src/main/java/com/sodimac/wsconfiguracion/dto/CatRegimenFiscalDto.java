package com.sodimac.wsconfiguracion.dto;

public class CatRegimenFiscalDto {

	private String regimenfiscal;
	private String descripcion;
	private String tipoPersona;
	private int idTipoPersona;
	
	
	public CatRegimenFiscalDto() {}
	
	public CatRegimenFiscalDto(String regimenfiscal, String descripcion, String tipoPersona, int idTipoPersona ) {
		this.regimenfiscal = regimenfiscal;
		this.descripcion = descripcion;
		this.tipoPersona = tipoPersona;
		this.idTipoPersona = idTipoPersona;
	}
	
	public String getRegimenfiscal() {
		return regimenfiscal;
	}
	public void setRegimenfiscal(String regimenfiscal) {
		this.regimenfiscal = regimenfiscal;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getTipoPersona() {
		return tipoPersona;
	}
	public void setTipoPersona(String tipoPersona) {
		this.tipoPersona = tipoPersona;
	}
	public int getIdTipoPersona() {
		return idTipoPersona;
	}
	public void setIdTipoPersona(int idTipoPersona) {
		this.idTipoPersona = idTipoPersona;
	}
	
}

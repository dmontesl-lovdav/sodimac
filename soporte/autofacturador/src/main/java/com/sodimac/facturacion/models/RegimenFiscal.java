package com.sodimac.facturacion.models;

public class RegimenFiscal {
	private Integer idTipoPersona;
	private String descripcion;
	private String regimenfiscal;
	private String tipoPersona;

	public Integer getIdTipoPersona() {
		return idTipoPersona;
	}

	public void setIdTipoPersona(Integer idTipoPersona) {
		this.idTipoPersona = idTipoPersona;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getRegimenfiscal() {
		return regimenfiscal;
	}

	public void setRegimenfiscal(String regimenfiscal) {
		this.regimenfiscal = regimenfiscal;
	}

	public String getTipoPersona() {
		return tipoPersona;
	}

	public void setTipoPersona(String tipoPersona) {
		this.tipoPersona = tipoPersona;
	}

}

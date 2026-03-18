package com.sodimac.facturacion.models;

public class UsoDeCfdi {

	private int idUsoCfdi;
	private String clave;
	private String descripcionUso;
	private int activo;

	public int getIdUsoCfdi() {
		return idUsoCfdi;
	}

	public void setIdUsoCfdi(int idUsoCfdi) {
		this.idUsoCfdi = idUsoCfdi;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getDescripcionUso() {
		return descripcionUso;
	}

	public void setDescripcionUso(String descripcionUso) {
		this.descripcionUso = descripcionUso;
	}

	public int getActivo() {
		return activo;
	}

	public void setActivo(int activo) {
		this.activo = activo;
	}

}

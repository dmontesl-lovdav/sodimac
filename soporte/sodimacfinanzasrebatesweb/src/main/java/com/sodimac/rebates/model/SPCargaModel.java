package com.sodimac.rebates.model;

public class SPCargaModel {
	
	private Integer codigo;
	private String descripcion;
	
	public Integer getCodigo() {
		return codigo;
	}
	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	@Override
	public String toString() {
		return "SPCargaModel [codigo=" + codigo + ", descripcion=" + descripcion + "]";
	}
	
	
}
package com.sodimac.rebates.enums;

public enum EEstadoOrdenCompra {

	CANCELADA ("Cancelada"),
	RECEIVED  ("Received");
	
	private String descripcion;
	
	EEstadoOrdenCompra(String descripcion){
		this.descripcion = descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
}

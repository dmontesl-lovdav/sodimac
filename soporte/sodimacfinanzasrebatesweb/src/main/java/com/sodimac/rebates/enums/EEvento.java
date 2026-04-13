package com.sodimac.rebates.enums;

public enum EEvento {

	EXC_AUTORIZAR				(1,"Botón para autorizar"),
	EXC_INACTIVAR_RECURRENCIA	(2,"Botón para inactivar recurrencia"),
	FILLRATE_CALCULO			(3,"Botón para correr cálculo");
	
	private int id;
	private String descripcion;
	
	EEvento(int id, String descripcion) {
		this.id = id;
		this.descripcion = descripcion;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}

package com.sodimac.bctfacturacion.enums;

public enum EEstatus {

	ACTIVO 		(1, true),
	INACTIVO	(0, false);
	
	private int id;
	private boolean activo;
	
	EEstatus(int id, boolean activo) {
		this.id = id;
		this.activo = activo;
	}

	public int getId() {
		return id;
	}

	public boolean isActivo() {
		return activo;
	}
	
}

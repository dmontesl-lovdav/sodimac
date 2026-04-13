package com.sodimac.rebates.enums;

public enum EEstatus {
	ACTIVO 		(1,true),
	INACTIV0	(0,false);
	
	private int id;
	private boolean activo;
	
	EEstatus(int id, boolean activo) {
		this.id = id;
		this.activo = activo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
}

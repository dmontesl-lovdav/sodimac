package com.sodimac.facturacion.util.enums;

public enum EFacturaMailEstatus {

	EN_PROCESO	(1),
	ENVIADO   	(2),
	ERROR		(3);
	
	private int id;
	
	EFacturaMailEstatus(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

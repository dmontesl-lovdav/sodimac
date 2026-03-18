package com.sodimac.facturacion.util.enums;

public enum ECatalogoMensajes {

	CODIGO_POSTAL_INVALIDO 		(128),
	REGIMEN_CAPITAL_INVALIDO 	(129);

	private int id;
	
	ECatalogoMensajes(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}

package com.sodimac.facturacion.util.enums;

public enum ETipoPersonaEnum {

	FISICA	(1), 
	MORAL	(2);

	private int id;

	ETipoPersonaEnum(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}

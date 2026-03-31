package com.sodimac.bctfacturacion.enums;

public enum EEstatusContable {

	CONTABILIZADO 		(1),
	NO_CONTABILIZADO	(0);
	
	private int id;
	
	EEstatusContable(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

}

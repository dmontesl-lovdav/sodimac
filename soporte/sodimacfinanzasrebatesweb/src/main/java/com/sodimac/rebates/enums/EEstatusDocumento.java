package com.sodimac.rebates.enums;

public enum EEstatusDocumento {

	PENDIENTE_CALCULO 			(0),
	SOLICITUD_INICIO_CALCULO 	(1),
	EN_PROCESO_CALCULO 			(2),
	CALCULADO					(3);
	
	private int id;
	
	EEstatusDocumento(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

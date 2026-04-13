package com.sodimac.rebates.enums;

public enum EEstatusPeriodo {
	PENDIENTE_CALCULAR 			(0),
	SOLICITUD_CALCULO 			(1),
	EN_PROCESO_CALCULO			(2),
	TERMINO_CALCULO				(3),
	SOLICITUD_CONTABILIDAD		(4),
	AUTORIZACION_CONTABILIDAD	(5),
	REVISION_CONTABILIDAD		(6),
	PROCESO_CONTABILIDAD		(7),
	CONTABILIZADO				(8),
	ERROR_CONTABILIZAR			(9);
	
	private int id;
	
	EEstatusPeriodo(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
}

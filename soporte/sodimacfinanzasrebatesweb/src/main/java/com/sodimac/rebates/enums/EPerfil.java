package com.sodimac.rebates.enums;

public enum EPerfil {

	GESTOR_DE_CUENTAS_POR_PAGAR	(1),
	GERENTE_DE_LOGISTICA		(2),
	DIRECTOR_COMERCIAL			(3),
	GERENTE_COMERCIAL			(4),
	DIRECTOR_GENERAL			(5),
	GERENTE_FINANZAS			(6),
	SEGURIDAD					(7),
	DIRECCION_LOGISTICA			(8),
	DIRECCION_FINANZAS			(9),
	SUB_GERENTE_DE_COSTOS		(10);
	
	private int id;
	
	EPerfil(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}

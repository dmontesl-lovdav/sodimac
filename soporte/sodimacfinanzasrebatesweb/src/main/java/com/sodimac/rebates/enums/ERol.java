package com.sodimac.rebates.enums;

public enum ERol {

	ADMINISTRADOR					(1),
	ESPECIALISTA_CUENTAS_POR_PAGAR	(2),
	GESTION_PROVEEDORES				(3),
	GESTION_CADENA_SUMINISTRO		(4),
	SOPORTE							(5),
	AUDITORIA						(6),
	ADMINISTRACION_SEGURIDAD		(7),
	DIRECCION						(8),
	GESTION_CONTRATOS				(9),
	GESTION_FINANCIERA				(10),
	SUPERVISION_PROCESOS_PAGO		(11);
	
	private int id;
	
	ERol(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

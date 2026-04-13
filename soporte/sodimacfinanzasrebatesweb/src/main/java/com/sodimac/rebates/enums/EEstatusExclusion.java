package com.sodimac.rebates.enums;

public enum EEstatusExclusion {

	SOLICITUD 				(1),
	PENDIENTE_AUTORIZACION	(2),
	RECURRENCIA_CANCELADA	(3),
	CALCULADO				(4),
	CONTABILIZADO			(5),
	INACTIVA				(6);
	
	private Integer idEstatus;
	
	EEstatusExclusion(Integer idEstatus) {
		this.idEstatus = idEstatus;
	}

	public Integer getIdEstatus() {
		return idEstatus;
	}

	public void setIdEstatus(Integer idEstatus) {
		this.idEstatus = idEstatus;
	}
	
	
}

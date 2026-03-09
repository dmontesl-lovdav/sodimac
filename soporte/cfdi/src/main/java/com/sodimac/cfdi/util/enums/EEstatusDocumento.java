package com.sodimac.cfdi.util.enums;

public enum EEstatusDocumento {

	PENDIENTE_PROCESO 	(1),
	EN_PROCESO			(2),
	PUBLICADO			(3),
	RECHAZADO			(4);
	
	EEstatusDocumento(Integer idEstatus) {
		this.idEstatus = idEstatus;
	}
	
	private Integer idEstatus;

	public Integer getIdEstatus() {
		return idEstatus;
	}

	public void setIdEstatus(Integer idEstatus) {
		this.idEstatus = idEstatus;
	}
}

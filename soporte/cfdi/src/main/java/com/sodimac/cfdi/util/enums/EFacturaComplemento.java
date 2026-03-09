package com.sodimac.cfdi.util.enums;

public enum EFacturaComplemento {
	COMPLETO (1),
	PARTES (2),
	LIBRE (3);
	
	private int idEstatus;
	
	EFacturaComplemento(int idEstatus) {
		this.idEstatus = idEstatus;
	}

	public int getIdEstatus() {
		return idEstatus;
	}

	public void setIdEstatus(int idEstatus) {
		this.idEstatus = idEstatus;
	}

	public static EFacturaComplemento getFacturaComplemento(int idEstatus) {
		for (EFacturaComplemento eFacturaCom : EFacturaComplemento.values()) {
			if (idEstatus == eFacturaCom.getIdEstatus()) {
				return eFacturaCom;
			}
		}
		return null;
	}
	
	
}

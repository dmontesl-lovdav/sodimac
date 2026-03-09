package com.sodimac.cfdi.util.enums;

public enum EComplementoPago {

	PENDIENTE_POR_TIMBRAR 	("PR"),
	COMPLEMENTO_EN_PROCESO	("CP"),
	TIMBRADO				("T"),
	TIMBRADO_PARCIAL		("TP"),
	CANCELADO				("C");
	
	private String estatus;
	
	EComplementoPago(String estatus) {
		this.estatus = estatus;
	}

	public String getEstatus() {
		return estatus;
	}

	public void setEstatus(String estatus) {
		this.estatus = estatus;
	}
	
	public static EComplementoPago getComplementoByEstatus(String estatus) {
		for (EComplementoPago eComplemento : EComplementoPago.values()) {
			if (estatus.equals(eComplemento.getEstatus())) {
				return eComplemento;
			}
		}
		return null;
	}
}

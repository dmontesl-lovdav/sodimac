package com.sodimac.rebates.enums;

public enum ETipoDocumento {

	CATALOGO_MONEDA 					(1),
	CATALOGO_ACUERDO_COMERCIAL 			(2),
	CATALOGO_ARTICULO					(3),
	CATALOGO_PROVEEDORES				(4),
	EXCLUSION_OC						(5),
	EXCLUSION_PROVEEDORES				(6),
	EXCLUSION_PROVEEDORES_SKU   		(7),
	AJUSTE_CALCULO_REBATE				(8),
	EXCLUSION_OC_FILL_RATE				(9),
	EXCLUSION_PROVEEDORES_FILL_RATE		(10),
	EXCLUSION_PROVEEDORES_SKU_FILL_RATE (11),
	CATALOGO_IMPUESTOS					(12),
	AJUSTE_CALCULO_FILL_RATE			(13),
	DEVOLUCIONES_PROVEEDORES_AP 		(14);
	
	private int id;
	
	ETipoDocumento(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}

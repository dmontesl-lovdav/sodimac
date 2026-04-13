package com.sodimac.rebates.enums;

public enum ETipoExclusion {
	ORDEN_COMPRA	(1),
	SKU				(2),
	FAMILIA			(3),
	PROVEEDORES		(4);
	
	private int id;
	
	ETipoExclusion(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
}

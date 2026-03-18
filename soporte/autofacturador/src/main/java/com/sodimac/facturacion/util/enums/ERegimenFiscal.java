package com.sodimac.facturacion.util.enums;

public enum ERegimenFiscal {
	FISICA (1,"fisica"),
	MORAL (2,"moral");
	
	private int id;
	private String descripcion;
	
	ERegimenFiscal(int id, String descripcion) {
		this.id = id;
		this.descripcion = descripcion;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public static ERegimenFiscal getRegimenFiscalByDescripcion(String desc) {
		for (ERegimenFiscal reg : ERegimenFiscal.values()) {
			if (reg.getDescripcion().equalsIgnoreCase(desc)) {
				return reg;
			}
		}
		return null;
	}
}

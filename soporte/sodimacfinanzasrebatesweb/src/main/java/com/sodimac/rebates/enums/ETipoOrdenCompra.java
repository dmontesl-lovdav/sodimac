package com.sodimac.rebates.enums;

public enum ETipoOrdenCompra {

	COMPRA_DIRECTA 			("Compra Directa"),
	PARADIGM_MK_PE 			("Paradigm MK PE"),
	CROSS_DOCKING 			("Cross Docking"),
	REP_TIENDA_ACEPTA_UN	("Rep Tienda Acepta un"),
	DAD_CONTRA_PEDIDO_PE	("DAD Contra pedido PE"),
	REP_TIENDA_MULTIPLE		("Rep Tienda Multiple"), 
	SERVICIOS_ESPECIALES	("Servicios Especiales"),
	PRODUCCIÓN_INTERNA		("Producción Interna"),
	REP_CENTRAL				("Rep Central"),
	APERTURA_TIENDA			("Apertura Tienda"),
	REP_CENTRAL_VOLUMEN		("Rep Central Volumen"),
	MANUAL_LOCAL			("Manual Local"),
	IMPORTADA				("Importada"),
	COMPRA_CONTRA_PEDIDO	("Compra contra Pedido");
	
	private String descripcion;
	
	ETipoOrdenCompra(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}

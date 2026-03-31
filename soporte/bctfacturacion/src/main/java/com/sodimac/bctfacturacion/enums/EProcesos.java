package com.sodimac.bctfacturacion.enums;

public enum EProcesos {

	FACTURAS_TO_BCT 	(1, "Portal In House [77] a EST_FACTURAS [bct]"),					//prod			
	VENTA_CAB			(2, "Venta de Bct TRX_HDR y TRX_DET a VENTA_CAB y VENTA_DET [83]"), //prod
	FACTURAS_TO_FISCAL	(3, "Portal In House [77] a FACTURACION_CLIENTE [83]"),				//prod
	FACTURA_GLOBAL		(4, "Facturación Global Venta"),									//PROD
	ADMIN_PUNTOS_CES	(5, "Admin puntos CES [BCT a DEV_SAP]");							//PROD
	
	private int idProceso;
	private String descripcion;
	
	EProcesos(int idProceso, String descripcion) {
		this.idProceso = idProceso;
		this.descripcion = descripcion;
	}
	
	public static EProcesos getProceso(int idProceso) {
		for (EProcesos proc : EProcesos.values()) {
			if (proc.getIdProceso() == idProceso) {
				return proc;
			}
		}
		return null;
	}

	public int getIdProceso() {
		return idProceso;
	}

	public void setIdProceso(int idProceso) {
		this.idProceso = idProceso;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}

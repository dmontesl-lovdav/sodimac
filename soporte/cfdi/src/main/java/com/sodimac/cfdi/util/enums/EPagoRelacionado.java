package com.sodimac.cfdi.util.enums;

public enum EPagoRelacionado {

	PENDIENTE_FOLIO(1,"Pendiente de folio"),
	PENDIENTE_ASIGNACION(2,"Pendiente de Pago"),
	ASIGNADO(3,"Pago Asignado");
	
	private int idPagoRelacionado;
	private String descripcion;
	
	EPagoRelacionado(int idPagoRelacionado, String descripcion) {
		this.idPagoRelacionado = idPagoRelacionado;
		this.descripcion = descripcion;
	}

	public int getIdPagoRelacionado() {
		return idPagoRelacionado;
	}

	public void setIdPagoRelacionado(int idPagoRelacionado) {
		this.idPagoRelacionado = idPagoRelacionado;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public static EPagoRelacionado getPagoRelacionado(int intValue) {
		for (EPagoRelacionado pago : EPagoRelacionado.values()) {
			if (pago.getIdPagoRelacionado() == intValue) {
				return pago;
			}
		}
		return null;
	}
}

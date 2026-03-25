package com.sodimac.facturacion.models;

public class ComprobanteBct {

	private ErrorBase errorBase;
	private String comprobante;
	
	public ErrorBase getError() {
		return errorBase;
	}

	public void setError(ErrorBase errorBase) {
		this.errorBase = errorBase;
	}

	public String getComprobante() {
		return comprobante;
	}

	public void setComprobante(String comprobante) {
		this.comprobante = comprobante;
	}
	
}

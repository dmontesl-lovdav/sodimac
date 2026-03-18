package com.sodimac.facturacion.util.enums;

public enum EStatusConsultarFacturaId {
	NoExisteFactura(0),
	TimbradoOk(1),
	TimbradoError(2);
	
	int valor;
	EStatusConsultarFacturaId (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

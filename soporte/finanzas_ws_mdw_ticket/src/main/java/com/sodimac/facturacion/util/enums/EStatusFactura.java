package com.sodimac.facturacion.util.enums;

public enum EStatusFactura {
	Pendiente(1),
	Facturado(2);
	
	int valor;
	EStatusFactura (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

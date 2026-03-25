package com.sodimac.facturacion.util.enums;

public enum EQuery {
	SingleResult(0),
	ResultList(1);
	
	int valor;
	EQuery (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

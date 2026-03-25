package com.sodimac.facturacion.util.enums;

public enum EExiste {
	NoExiste(0),
	SiExiste(1);
	
	int valor;
	EExiste (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

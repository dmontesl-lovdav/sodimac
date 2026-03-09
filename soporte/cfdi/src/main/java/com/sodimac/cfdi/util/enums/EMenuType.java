package com.sodimac.cfdi.util.enums;

public enum EMenuType {
	Modulo(0),
	Item(1);
	
	int valor;
	EMenuType (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
	
}

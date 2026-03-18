package com.sodimac.wsconfiguracion.util.enums;

public enum EProceso {
	TimbradoNormal(0),
	Autofacturador(1),
	PendientePac(2),
	NoBct(3),
	Refacturacion(4),
	Rebates(5),
	Sincronizacion(6);
	
	int valor;
	EProceso (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

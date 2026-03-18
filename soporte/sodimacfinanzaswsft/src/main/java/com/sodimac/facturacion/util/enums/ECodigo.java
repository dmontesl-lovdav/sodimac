package com.sodimac.facturacion.util.enums;

public enum ECodigo {
	Error(0),
	Ok(1),
	AccesoDenegado(5),
	DocumentoInvalido(105),
	FechaInvalida(112);
	
	int valor;
	ECodigo (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

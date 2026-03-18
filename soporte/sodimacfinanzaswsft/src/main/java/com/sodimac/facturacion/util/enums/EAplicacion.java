package com.sodimac.facturacion.util.enums;

public enum EAplicacion {
	PortalInHouse(1),
	ProcesoBatch(2),
	NotasCredito(3),
	FacturacionMasivaVVEE(4),
	FacturacionGlobal(5),
	Rebates(6),
	Refacturacion(7),
	TimbradoInmuebles(8),
	Wsft(9),
	Detecno(10);
	
	int valor;
	EAplicacion (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

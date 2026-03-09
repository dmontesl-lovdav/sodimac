package com.sodimac.cfdi.util.enums;

public enum ETipoDocumento {
	POS(1),
	DAD(2),
	Mascara(3),
	Lepton(4),
	Arrendamiento(5),
	OC(6);
	
	int valor;
	ETipoDocumento (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

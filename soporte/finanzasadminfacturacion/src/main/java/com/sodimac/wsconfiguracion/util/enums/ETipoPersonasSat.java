package com.sodimac.wsconfiguracion.util.enums;

public enum ETipoPersonasSat {
	PERSONA_FISICA(1),
	PERSONA_MORAL(2),
	TODAS_LAS_PERSONAS(3);
	
	private Integer valor;
	
	ETipoPersonasSat(Integer v) {
	        this.valor = v;
	}

	public Integer getValor() {
		return valor;
	}
	
	 
}

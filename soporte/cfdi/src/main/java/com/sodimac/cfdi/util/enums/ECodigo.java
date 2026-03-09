package com.sodimac.cfdi.util.enums;

public enum ECodigo {
	Error(0),
	Ok(1),
	ErrorConexionBct(2),
	ErrorConexionWSDAD(3),
	ErrorConexionMascara(4),
	AccesoDenegado(5),
	
	NoExisteTicketBct(101),
	NoExisteOCBct(102),
	NoExisteGuiaDAD(103),
	NoExisteGuiaMascara(104),
	DocumentoInvalido(105),
	OCDADSinTicket(106),
	OCMascaraSinTicket(107),
	TicketIncompletoSinDetalle(108),
	TicketIncompletoSinCabecera(109),
	FolioLepton(110),
	FolioArrendamiento(111),
	FechaInvalida(112);
	
	int valor;
	ECodigo (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

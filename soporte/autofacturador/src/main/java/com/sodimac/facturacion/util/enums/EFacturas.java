package com.sodimac.facturacion.util.enums;

public enum EFacturas {
	ConsultaTicket(1),
	ObtieneTicket(2),
	NoExisteTicket(3),
	ReintentoConsultaTicket(4),
	ReintentoNoExisteTicket(5),
	RfcValido(6),
	RfcInvalidoNoApto(7),
	RfcInvalidoInhabilitado(8),
	EnProcesoFacturacion(9),
	Facturado(10),
	PendienteEnviar(11),
	FacturaEnviada(12),
	EnEspera(13);

	int valor;
	EFacturas (int v){
		valor = v;
	}
	public int getValor() {
		return valor;
   } 	
};

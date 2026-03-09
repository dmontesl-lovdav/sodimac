package com.sodimac.cfdi.model.puntosces;

public class ResponsePoliza {

	PolizaContableDto data;
	RespuestaDto respuesta;

	public PolizaContableDto getData() {
		return data;
	}

	public void setData(PolizaContableDto data) {
		this.data = data;
	}

	public RespuestaDto getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(RespuestaDto respuesta) {
		this.respuesta = respuesta;
	}

}

package com.sodimac.cfdi.model.puntosces;

import java.util.List;

public class ResponseListPolizas {

	List<PolizaContableDto> data;
	RespuestaDto respuesta;

	public List<PolizaContableDto> getData() {
		return data;
	}

	public void setData(List<PolizaContableDto> data) {
		this.data = data;
	}

	public RespuestaDto getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(RespuestaDto respuesta) {
		this.respuesta = respuesta;
	}

}

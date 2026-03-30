package com.sodimac.oc.batch.dto.sodimac;

public class ParametroResponse {

	private DataContent data;
	private RespuestaContent respuesta;

	public ParametroResponse() {
	}

	public DataContent getData() {
		return data;
	}

	public void setData(DataContent data) {
		this.data = data;
	}

	public RespuestaContent getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(RespuestaContent respuesta) {
		this.respuesta = respuesta;
	}

}

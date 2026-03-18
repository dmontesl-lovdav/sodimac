package com.sodimac.facturacion.clientews.model;

public class ClientResponseTYPE<T> {

	private T data;
	private RespuestaClient respuesta;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public RespuestaClient getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(RespuestaClient respuesta) {
		this.respuesta = respuesta;
	}

}

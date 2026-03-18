package com.sodimac.facturacion.clientews.wsprmpar;

public class WsResponse <T> {
	private T data;
	private Respuesta Respuesta;
	
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	public Respuesta getRespuesta() {
		return Respuesta;
	}
	public void setRespuesta(Respuesta respuesta) {
		Respuesta = respuesta;
	}
	
}

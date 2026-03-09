package com.sodimac.cfdi.cliente.wsadministracion;

public class ClientResponseTYPE<T> {
	
	private T data;
	private ResponseBaseDto respuesta = new ResponseBaseDto();
	
	public ClientResponseTYPE() {
		
	}
	
	public ClientResponseTYPE(T data) {
		this.data = data;
	}
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}


	public ResponseBaseDto getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(ResponseBaseDto respuesta) {
		this.respuesta = respuesta;
	}


}

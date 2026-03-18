package com.sodimac.facturacion.clientews.configuracion;

public class VersionTimbradoRes {

	private VersionTimbradoDto data;
	private ResponseBaseDto respuesta = new ResponseBaseDto();

	public VersionTimbradoRes () {}
	public VersionTimbradoRes (VersionTimbradoDto data) {
		this.data = data;
		
	}
	
	public VersionTimbradoDto getData() {
		return data;
	}
	public void setData(VersionTimbradoDto data) {
		this.data = data;
	}
	public ResponseBaseDto getRespuesta() {
		return respuesta;
	}
	public void setRespuesta(ResponseBaseDto respuesta) {
		this.respuesta = respuesta;
	}
	
}

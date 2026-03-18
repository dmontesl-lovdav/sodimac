package com.sodimac.facturacion.clientews.configuracion;

public class EmisorYLugarExpedicionDto {
	

	private ComprobanteDto data;
	private ResponseBaseDto respuesta = new ResponseBaseDto();
	
	
	public EmisorYLugarExpedicionDto () {}
	
	public EmisorYLugarExpedicionDto (ComprobanteDto comprobante) {
		this.data = comprobante;
		
	}

	public ComprobanteDto getData() {
		return data;
	}

	public void setData(ComprobanteDto data) {
		this.data = data;
	}

	public ResponseBaseDto getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(ResponseBaseDto respuesta) {
		this.respuesta = respuesta;
	}

}

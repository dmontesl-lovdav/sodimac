package com.sodimac.cfdi.model.puntosces;

import java.util.List;

public class ResponseListCatalogos {

	List<CatalogoDto> data;
	RespuestaDto respuesta;

	public List<CatalogoDto> getData() {
		return data;
	}

	public void setData(List<CatalogoDto> data) {
		this.data = data;
	}

	public RespuestaDto getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(RespuestaDto respuesta) {
		this.respuesta = respuesta;
	}

}

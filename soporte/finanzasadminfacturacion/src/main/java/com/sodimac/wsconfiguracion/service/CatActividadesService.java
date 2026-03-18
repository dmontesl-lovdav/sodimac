package com.sodimac.wsconfiguracion.service;


public interface CatActividadesService {
	

	public int registrarActividad(int idActividad, String actividadDesc
			, String ticket
			, int usuario
			, String longitud
			, String latitud
			, String pagina
			, String explorador
			, String sistemaOper
			, String ip
			, String rfc
			, String sessionId
			);
	

	
}

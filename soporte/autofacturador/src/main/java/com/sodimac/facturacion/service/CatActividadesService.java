package com.sodimac.facturacion.service;

import com.sodimac.facturacion.entity.CatActividadesEntity;

public interface CatActividadesService {
	
	public CatActividadesEntity getActividad(int idActividad);
	
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

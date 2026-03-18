package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.MesDto;
import com.sodimac.wsconfiguracion.dto.PeriodicidadDto;

public interface CatalogosService {
	
	public ClientResponseTYPE<MesDto> getMesByClave(String clave);
	public ClientResponseTYPE<PeriodicidadDto> getPeriodicidadByClave(String clave);
}

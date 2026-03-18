package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.VersionTimbradoDto;

public interface VersiontimbradoAplicacionService {
	
	public ClientResponseTYPE<VersionTimbradoDto> ObtieneVersionTimbrado(Integer idAplicacion);

}

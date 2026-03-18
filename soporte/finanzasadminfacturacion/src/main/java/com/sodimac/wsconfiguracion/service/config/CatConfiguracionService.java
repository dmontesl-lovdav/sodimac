package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.InformacionGlobal;

public interface CatConfiguracionService {

	
	public String findParameterByKey(String NombreCampo);

	public ClientResponseTYPE<InformacionGlobal> obtieneParamsFG();
}

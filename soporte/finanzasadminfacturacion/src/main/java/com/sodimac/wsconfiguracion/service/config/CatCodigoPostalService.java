package com.sodimac.wsconfiguracion.service.config;

import com.sodimac.wsconfiguracion.dto.ClientResponseTYPE;
import com.sodimac.wsconfiguracion.dto.CodigoPostal;

public interface CatCodigoPostalService {

	
	public ClientResponseTYPE<CodigoPostal> verificaCodigoPostal(Integer codigoPostal);
	
}

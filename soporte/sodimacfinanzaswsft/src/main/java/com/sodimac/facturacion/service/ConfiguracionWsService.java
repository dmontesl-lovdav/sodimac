package com.sodimac.facturacion.service;

import java.util.List;

import com.sodimac.facturacion.entity.ws.ConfiguracionWsEntity;

public interface ConfiguracionWsService {

	public List<ConfiguracionWsEntity> getAll();
	public String findParameterByKey(String NombreCampo);
	
}

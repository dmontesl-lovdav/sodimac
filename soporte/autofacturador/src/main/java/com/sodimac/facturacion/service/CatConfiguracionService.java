package com.sodimac.facturacion.service;

import java.util.List;

import com.sodimac.facturacion.entity.CatConfiguracionEntity;

public interface CatConfiguracionService {

	public List<CatConfiguracionEntity> getAll();
	public String findParameterByKey(String NombreCampo);


	
}

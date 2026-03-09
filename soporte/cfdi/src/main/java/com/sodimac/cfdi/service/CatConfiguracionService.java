package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.entity.fiscal.CatConfiguracionEntity;

public interface CatConfiguracionService {

	public List<CatConfiguracionEntity> getAll();
	public String findParameterByKey(String NombreCampo);


	
}

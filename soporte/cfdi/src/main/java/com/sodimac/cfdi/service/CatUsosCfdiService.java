package com.sodimac.cfdi.service;

import java.util.List;

import com.sodimac.cfdi.models.UsoDeCfdi;

public interface CatUsosCfdiService {

	public List<UsoDeCfdi> getAll(String tipo, Integer idVersionCfdi);
	
	public UsoDeCfdi getUsoCfdi(int id, Integer idVersionCfdi);
	
	public UsoDeCfdi getUsoCfdi(String clave, Integer idVersionCfdi);
	
}

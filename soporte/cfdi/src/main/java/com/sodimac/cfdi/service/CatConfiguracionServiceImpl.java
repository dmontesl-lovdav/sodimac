package com.sodimac.cfdi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.cfdi.entity.fiscal.CatConfiguracionEntity;
import com.sodimac.cfdi.repository.fiscal.CatConfiguracionRepository;

@Service
public class CatConfiguracionServiceImpl implements CatConfiguracionService {

	@Autowired
	private CatConfiguracionRepository catConfiguracionRepository;
	
	@Override
	@Transactional
	public List<CatConfiguracionEntity> getAll() {
		return catConfiguracionRepository.findAll();
	}
	
	@Override
	@Transactional
	public String findParameterByKey(String NombreCampo) {
		return catConfiguracionRepository.findParameterByKey(NombreCampo);
	}
	
}

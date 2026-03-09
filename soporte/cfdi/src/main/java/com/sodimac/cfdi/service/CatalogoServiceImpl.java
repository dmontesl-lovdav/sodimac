package com.sodimac.cfdi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.menu.CatPerfilEntity;
import com.sodimac.cfdi.repository.fiscal.menu.CatPerfilRepository;

@Service
public class CatalogoServiceImpl implements CatalogoService {

	@Autowired
	private CatPerfilRepository catPerfilRepository;
	
	@Override
	public List<CatPerfilEntity> getPerfiles() {	
		return catPerfilRepository.findAll();
	}

}

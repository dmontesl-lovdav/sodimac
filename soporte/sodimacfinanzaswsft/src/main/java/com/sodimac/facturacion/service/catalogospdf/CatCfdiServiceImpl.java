package com.sodimac.facturacion.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.fac.catalogospdf.CatCfdiEntity;
import com.sodimac.facturacion.repository.fac.catalogospdf.CatCfdiRepository;

@Service
public class CatCfdiServiceImpl implements CatCfdiService {

	@Autowired
	private CatCfdiRepository catCfdiRepository;
	
	@Override
	@Transactional
	public CatCfdiEntity get(int id) {
		return catCfdiRepository.findById(id).orElse(null);

	}	
}
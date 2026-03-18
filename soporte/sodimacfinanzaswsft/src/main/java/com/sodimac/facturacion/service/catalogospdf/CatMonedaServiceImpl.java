package com.sodimac.facturacion.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.fac.catalogospdf.CatMonedaEntity;
import com.sodimac.facturacion.repository.fac.catalogospdf.CatMonedaRepository;

@Service
public class CatMonedaServiceImpl implements CatMonedaService {
	
	@Autowired
	private CatMonedaRepository catMonedaRepository;
	
	@Override
	@Transactional
	public CatMonedaEntity get(String id) {
		return catMonedaRepository.findById(id).orElse(null);

	}	
}

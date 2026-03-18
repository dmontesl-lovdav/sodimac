package com.sodimac.facturacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.fac.CatMensajesEntity;
import com.sodimac.facturacion.repository.fac.CatMensajesRepository;

@Service
public class CatMensajesServiceImpl implements CatMensajesService {

	@Autowired
	private CatMensajesRepository catMensajesRepository;
	
	@Override
	@Transactional
	public CatMensajesEntity get(int id) {
		return catMensajesRepository.findById(id).orElse(null);
	}
	
}

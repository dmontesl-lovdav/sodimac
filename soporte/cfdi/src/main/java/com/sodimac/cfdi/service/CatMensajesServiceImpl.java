package com.sodimac.cfdi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.cfdi.entity.fiscal.CatMensajesEntity;
import com.sodimac.cfdi.repository.fiscal.CatMensajesRepository;

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

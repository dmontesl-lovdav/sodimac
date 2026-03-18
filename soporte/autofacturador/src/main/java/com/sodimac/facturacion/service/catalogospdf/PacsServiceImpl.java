package com.sodimac.facturacion.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.catalogospdf.PacsEntity;
import com.sodimac.facturacion.repository.catalogospdf.PacsRepository;

@Service
public class PacsServiceImpl implements PacsService{

	@Autowired
	private PacsRepository pacsRepository;
	
	@Transactional
	public int getIdDefault() {
		return pacsRepository.getIdDefault();
	}

	@Override
	@Transactional
	public PacsEntity getById(int id) {
		return pacsRepository.findById(id).orElse(null);
	}

}

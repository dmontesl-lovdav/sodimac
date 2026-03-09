package com.sodimac.cfdi.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.CatRegimenFiscalEntity;
import com.sodimac.cfdi.repository.fiscal.catalogospdf.CatRegimenFiscalRepository;

@Service
public class CatRegimenFiscalServiceImpl implements CatRegimenFiscalService {

	@Autowired
	private CatRegimenFiscalRepository catRegimenFiscalRepository;
	
	@Override
	@Transactional
	public CatRegimenFiscalEntity get(int id) {
		return catRegimenFiscalRepository.findById(id).orElse(null);

	}	
}
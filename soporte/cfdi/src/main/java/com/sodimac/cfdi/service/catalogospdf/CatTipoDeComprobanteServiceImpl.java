package com.sodimac.cfdi.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.CatTipoDeComprobanteEntity;
import com.sodimac.cfdi.repository.fiscal.catalogospdf.CatTipoDeComprobanteRepository;

@Service
public class CatTipoDeComprobanteServiceImpl implements CatTipoDeComprobanteService {
	
	@Autowired
	private CatTipoDeComprobanteRepository catTipoDeComprobanteRepository;
	
	@Override
	@Transactional
	public CatTipoDeComprobanteEntity get(String idComprobante) {
		return catTipoDeComprobanteRepository.findByIdComprobante(idComprobante);

	}	
}

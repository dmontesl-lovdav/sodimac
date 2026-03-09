package com.sodimac.cfdi.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.CatFormaPagoEntity;
import com.sodimac.cfdi.repository.fiscal.catalogospdf.CatFormaPagoRepository;

@Service
public class CatFormaPagoServiceImpl implements CatFormaPagoService {

	@Autowired
	private CatFormaPagoRepository catFormaPagoRepository;
	
	@Override
	public CatFormaPagoEntity getByIdFormaPago(String idFormaPago) {
		return catFormaPagoRepository.findById(idFormaPago).orElse(null);

	}

}

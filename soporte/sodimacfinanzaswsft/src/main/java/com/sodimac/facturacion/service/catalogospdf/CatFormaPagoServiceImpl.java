package com.sodimac.facturacion.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.entity.fac.catalogospdf.CatFormaPagoEntity;
import com.sodimac.facturacion.repository.fac.catalogospdf.CatFormaPagoRepository;

@Service
public class CatFormaPagoServiceImpl implements CatFormaPagoService {

	@Autowired
	private CatFormaPagoRepository catFormaPagoRepository;
	
	@Override
	public CatFormaPagoEntity getByIdFormaPago(String idFormaPago) {
		return catFormaPagoRepository.findById(idFormaPago).orElse(null);

	}

}

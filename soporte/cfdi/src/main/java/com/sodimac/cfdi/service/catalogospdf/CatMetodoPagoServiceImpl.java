package com.sodimac.cfdi.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.cfdi.entity.fiscal.catalogospdf.CatMetodoPagoEntity;
import com.sodimac.cfdi.repository.fiscal.catalogospdf.CatMetodoPagoRepository;

@Service
public class CatMetodoPagoServiceImpl implements CatMetodoPagoService {

	@Autowired
	private CatMetodoPagoRepository catMetodoPagoRepository;
	
	@Override
	public CatMetodoPagoEntity getByIdFormaPago(String idMetodoPago) {
		return catMetodoPagoRepository.findById(idMetodoPago).orElse(null);
	}

}

package com.sodimac.facturacion.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.entity.fac.catalogospdf.CatMetodoPagoEntity;
import com.sodimac.facturacion.repository.fac.catalogospdf.CatMetodoPagoRepository;

@Service
public class CatMetodoPagoServiceImpl implements CatMetodoPagoService {

	@Autowired
	private CatMetodoPagoRepository catMetodoPagoRepository;
	
	@Override
	public CatMetodoPagoEntity getByIdFormaPago(String idMetodoPago) {
		return catMetodoPagoRepository.findById(idMetodoPago).orElse(null);
	}

}

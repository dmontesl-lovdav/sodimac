package com.sodimac.facturacion.service.catalogospdf;

import org.springframework.beans.factory.annotation.Autowired;

import com.sodimac.facturacion.entity.fac.catalogospdf.CatImpuestoEntity;
import com.sodimac.facturacion.repository.fac.catalogospdf.CatImpuestoRepository;

public class CatImpuestoServiceImpl implements CatImpuestoService {

	@Autowired
	private CatImpuestoRepository catImpuestoRepository;
	
	@Override
	public CatImpuestoEntity getByIdImpuesto(String idImpuesto) {
		return catImpuestoRepository.findById(idImpuesto).orElse(null);
	}

}

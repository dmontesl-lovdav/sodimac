package com.sodimac.rebates.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.model.entity.CatCompradorProveedorEntity;
import com.sodimac.rebates.repository.CatCompradorProveedorRepository;
import com.sodimac.rebates.service.ICatCompradorProveedorService;

@Service
public class CatCompradorProveedorServiceImpl implements ICatCompradorProveedorService {

	@Autowired
	private CatCompradorProveedorRepository repository;
	
	@Override
	public boolean tienePermiso(Integer idcomprador, String numeroProveedor) {
		boolean result = true;
		CatCompradorProveedorEntity ent = this.repository.findByIdcompradorAndNumeroProveedor(idcomprador, numeroProveedor);
		if (ent == null) {
			result = false;
		}
		return result;
	}

}

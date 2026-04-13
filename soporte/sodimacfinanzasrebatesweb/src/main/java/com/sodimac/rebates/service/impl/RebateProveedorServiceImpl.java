package com.sodimac.rebates.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.rebates.dto.RebateProveedorDto;
import com.sodimac.rebates.mapper.RebateProveedorMapper;
import com.sodimac.rebates.model.entity.RebateProveedorEntity;
import com.sodimac.rebates.repository.RebateProveedorRepository;
import com.sodimac.rebates.service.IRebateProveedorService;

@Service
public class RebateProveedorServiceImpl implements IRebateProveedorService {

	@Autowired
	private RebateProveedorRepository proveedorRepository;
	
	@Override
	public RebateProveedorDto getProveedor(String codigoProveedor) {
		RebateProveedorEntity byIdRebateProveedor = this.proveedorRepository.findByCodigoProveedor(codigoProveedor);
		RebateProveedorDto dto = RebateProveedorMapper.convertDto(byIdRebateProveedor);
		return dto;
	}

}

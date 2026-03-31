package com.sodimac.bctfacturacion.service.impl;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.sodimac.bctfacturacion.entity.ces.VentaCabEntity;
import com.sodimac.bctfacturacion.mapper.VentaCabMapper;
import com.sodimac.bctfacturacion.model.VentaCabModel;
import com.sodimac.bctfacturacion.model.VentaDetImpuestoModel;
import com.sodimac.bctfacturacion.repository.ces.VentaCabRepository;
import com.sodimac.bctfacturacion.service.IVentaCabService;
import com.sodimac.bctfacturacion.service.IVentaDetImpuestoService;

@Repository
public class VentaCabServiceImpl implements IVentaCabService {

	@Autowired
	private VentaCabRepository ventaCabRepository;
	
	@Autowired
	private IVentaDetImpuestoService detImpuestoService;
	
	@Override
	public Integer obtenerIdVentaCab() {
		return this.ventaCabRepository.getIdVentaCab();
	}
	
	@Override
	public boolean existeTicket(String ticket) {
		long count = this.ventaCabRepository.countByTicket(ticket);
		if (count > 0) {
			return true;
		}
		return false;
	}

	@Override
	public VentaCabModel getTicket(String ticket) {
		VentaCabModel dto = null;
		VentaCabEntity entity = this.ventaCabRepository.findByTicket(ticket);
		
		if (entity != null) {
			dto = VentaCabMapper.convertToDto(entity);
			List<VentaDetImpuestoModel> ventasDet = this.detImpuestoService.getVentasDet(ticket);
			dto.setDetImpuestos(ventasDet);
		}
		return dto;
	}

	@Override
	public void guardar(VentaCabModel dto) throws ParseException {
		VentaCabEntity entity = VentaCabMapper.convertToEntity(dto);
		this.ventaCabRepository.save(entity);
		//this.detImpuestoService.guardar( dto.getDetImpuestos() );
	}	

}

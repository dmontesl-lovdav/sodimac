package com.sodimac.bctfacturacion.service.impl;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.bctfacturacion.entity.ces.VentaDetImpuestoEntity;
import com.sodimac.bctfacturacion.mapper.VentaDetImpuestoMapper;
import com.sodimac.bctfacturacion.model.VentaDetImpuestoModel;
import com.sodimac.bctfacturacion.repository.ces.VentaDetImpuestoRepository;
import com.sodimac.bctfacturacion.service.IVentaDetImpuestoService;

@Service
public class VentaDetImpuestoServiceImpl implements IVentaDetImpuestoService {

	@Autowired
	private VentaDetImpuestoRepository ventaDetImpuestoRepository;
	
	@Override
	public Integer obtnerIdVentaDetImpuesto() {
		return this.ventaDetImpuestoRepository.getIdVentaDetImpuesto();
	}
	
	@Override
	public boolean existeVentaDetImpuesto(String ticket, int linea) {
		long count = this.ventaDetImpuestoRepository.countByTicketAndNumLinea(ticket, linea) ;
		if (count > 0) {
			return true;
		}
		return false;
	}

	@Override
	public List<VentaDetImpuestoModel> getVentasDet(String ticket) {
		List<VentaDetImpuestoEntity> entities = this.ventaDetImpuestoRepository.findByTicket(ticket);
		return VentaDetImpuestoMapper.converToDtos(entities);
	}

	@Override
	public void guardar(List<VentaDetImpuestoModel> dtos) throws ParseException {
		if (dtos != null) {
			for (VentaDetImpuestoModel dto : dtos) {
				VentaDetImpuestoEntity entity = VentaDetImpuestoMapper.converToEntity(dto);
				this.ventaDetImpuestoRepository.save(entity);
			}
		}
		
	}

	@Override
	public void guardar(VentaDetImpuestoModel dto) throws ParseException {
		VentaDetImpuestoEntity entity = VentaDetImpuestoMapper.converToEntity(dto);
		this.ventaDetImpuestoRepository.save(entity);
	}
}

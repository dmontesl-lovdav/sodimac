package com.sodimac.bctfacturacion.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.bctfacturacion.entity.facturacion.FacturasEntity;
import com.sodimac.bctfacturacion.repository.facturacion.FacturasRepository;
import com.sodimac.bctfacturacion.service.FacturasService;

@Service
public class FacturasServiceImpl implements FacturasService {

	@Autowired
	private FacturasRepository facturasRepository;
	
	@Override
	public FacturasEntity getTicket(String ticket) {
		return this.facturasRepository.findByTicket(ticket);
	}

	@Override
	public List<FacturasEntity> getTicketFecha(String pFecha, String nextDay) {
		return this.facturasRepository.findByTicketByDate(pFecha, nextDay);
	}

}

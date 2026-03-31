package com.sodimac.bctfacturacion.service;

import java.util.List;

import com.sodimac.bctfacturacion.entity.facturacion.FacturasEntity;

public interface FacturasService {

	public FacturasEntity getTicket(String ticket);
	
	public List<FacturasEntity> getTicketFecha(String pFecha, String nextDay);
}

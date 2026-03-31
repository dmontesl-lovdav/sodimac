package com.sodimac.bctfacturacion.service;

import java.util.List;

import com.sodimac.bctfacturacion.model.FacturaModel;

public interface EstFacturasService {

	public Integer existeTicket(String pUuid);
	
	public void insertar(FacturaModel pFactura);

	public List<String> getTickets(List<String> pUuid);
}

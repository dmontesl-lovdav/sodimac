package com.sodimac.bctfacturacion.service;

import java.util.Date;

import com.sodimac.bctfacturacion.model.DevolucionCabecera;
import com.sodimac.bctfacturacion.model.DevolucionDetalle;

public interface VentaMSICabService {
	
	public Integer totalTickets(String fechaTicket, Integer tienda);
	
	public Integer totalTicketsDet(String fechaTicket, Integer tienda);

	public void eliminaVentaCab(Date fechaTicket, Integer tienda);
	
	void eliminaVentaDet(Date fechaTicket, Integer tienda);
	
	public void registraVentaCab(DevolucionCabecera devolucionCabecera);
	
	public void registraVentaCabDet(DevolucionDetalle detalle);

}

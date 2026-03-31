package com.sodimac.bctfacturacion.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.bctfacturacion.model.DevolucionCabecera;
import com.sodimac.bctfacturacion.model.DevolucionDetalle;
import com.sodimac.bctfacturacion.repository.fiscal.VentaMSICabRepository;
import com.sodimac.bctfacturacion.service.VentaMSICabService;

@Service
public class VentaMSIServiceImpl implements VentaMSICabService {

	@Autowired
	private VentaMSICabRepository ventaMSICabRepository;
	
	@Override
	public Integer totalTickets(String fechaTicket, Integer tienda) {
		return this.ventaMSICabRepository.totalTickets(fechaTicket, tienda);
	}
	
	@Override
	public Integer totalTicketsDet(String fechaTicket, Integer tienda) {
		return this.ventaMSICabRepository.totalTicketsDet(fechaTicket, tienda);
	}
	
	@Override
	@Transactional("transactionManagerFiscal")
	public void eliminaVentaCab(Date fechaTicket, Integer tienda) {
		this.ventaMSICabRepository.eliminaVentaCab(fechaTicket, tienda);
	}
	
	@Override
	@Transactional("transactionManagerFiscal")
	public void eliminaVentaDet(Date fechaTicket, Integer tienda) {
		this.ventaMSICabRepository.eliminaVentaDet(fechaTicket, tienda);
	}

	@Override
	@Transactional("transactionManagerFiscal")
	public void registraVentaCab(DevolucionCabecera devolucionCabecera) {
		this.ventaMSICabRepository.registraVentaCab(devolucionCabecera.getTicket()
													, devolucionCabecera.getFechaTicket()
													, devolucionCabecera.getTienda()
													, devolucionCabecera.getCaja()
													, devolucionCabecera.getTransaccion()
													, devolucionCabecera.getTipo()
													, devolucionCabecera.getTotal()
													, devolucionCabecera.getSubtotal()
													, devolucionCabecera.getRedondeo()
													, devolucionCabecera.getTicketOrigen()
													, devolucionCabecera.getFechaEnlace()
													, devolucionCabecera.getFechaCarga()
													, devolucionCabecera.getEstatusProceso());
	}

	@Override
	@Transactional("transactionManagerFiscal")
	public void registraVentaCabDet(DevolucionDetalle detalle) {
		this.ventaMSICabRepository.registraVentaCabDet( detalle.getTicket()
										, detalle.getFechaTicket()
										, detalle.getTienda()
										, detalle.getCaja()
										, detalle.getNumDocCanal()
										, detalle.getCanalLinio()
										, detalle.getTotalArticulo()
										, detalle.getFechaCarga()
										, detalle.getCajaEstatusProceso()
										);
	}
}

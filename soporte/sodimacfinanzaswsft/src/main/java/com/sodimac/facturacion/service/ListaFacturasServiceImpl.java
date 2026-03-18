package com.sodimac.facturacion.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.fac.ListaFacturasEntity;
import com.sodimac.facturacion.repository.fac.ListaFacturasRepository;

@Service
public class ListaFacturasServiceImpl implements ListaFacturasService {

	@Autowired
	private ListaFacturasRepository listaFacturasRepository;
	
	@Override
	@Transactional
	public List<ListaFacturasEntity> getAllListaFacturas() {

		return listaFacturasRepository.findAll();
	}

	@Override
	public ListaFacturasEntity getListaFacturaById(int idListaFacturas) {
		// TODO Auto-generated method stub
		return listaFacturasRepository.findById(idListaFacturas).orElse(null);
	}

	@Override
	@Transactional
	public ListaFacturasEntity addListaFacturas(String ticket, BigDecimal total, String sessionId) {
		
		listaFacturasRepository.saveEntity(ticket, total, 1, sessionId);

		return null;
	}

	@Override
	@Transactional
	public void updateListasFacturas(ListaFacturasEntity listaFactura) {
		listaFacturasRepository.save(listaFactura);
		
	}

	@Override
	@Transactional
	public void deleteItem(String ticket, String sessionId) {
		ListaFacturasEntity listaFacturasEntity= listaFacturasRepository.getListaFacturaByTicket(ticket, sessionId);
		
		if (listaFacturasEntity != null) {
			listaFacturasRepository.delete(listaFacturasEntity);
		}
		
		
	}

	@Override
	public ListaFacturasEntity getListaFacturaByTicket(String ticket, String sessionId) {
		return listaFacturasRepository.getListaFacturaByTicket(ticket, sessionId);
	}

	@Override
	public List<ListaFacturasEntity> getListaFacturasBySessionId(String SessionId) {
		return listaFacturasRepository.getListaFacturasBySessionId(SessionId);
	}
	
	@Override
	public ListaFacturasEntity findByTicketAndTotal (String ticket, BigDecimal total) {
		return listaFacturasRepository.findTop1ByTicketAndTotalOrderByFechaIngresoDesc(ticket, total);
	}

	@Override
	public ListaFacturasEntity findByTicket (String ticket) {
		return listaFacturasRepository.findTop1ByTicketOrderByFechaIngresoDesc(ticket);
	}
}

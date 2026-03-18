package com.sodimac.facturacion.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sodimac.facturacion.entity.ListaFacturasEntity;
import com.sodimac.facturacion.repository.ListaFacturasRepository;

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
		
		
		ListaFacturasEntity listaFacturaExist	= getListaFacturaByTicket(ticket, sessionId);
		if (listaFacturaExist == null) {
			ListaFacturasEntity listaFacturasEntity = new ListaFacturasEntity();
			listaFacturasEntity.setTicket(ticket);
			listaFacturasEntity.setTotal(total);
			listaFacturasEntity.setStatus(1);
			
			listaFacturasEntity.setSessionId(sessionId);
			ListaFacturasEntity response = listaFacturasRepository.save(listaFacturasEntity);
			return response;
		}
		
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

}

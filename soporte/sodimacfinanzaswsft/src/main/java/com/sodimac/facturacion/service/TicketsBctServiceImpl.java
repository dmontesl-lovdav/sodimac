package com.sodimac.facturacion.service;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sodimac.facturacion.entity.bct.TicketEntity;
import com.sodimac.facturacion.repository.bct.TicketBctRepository;

@Service
public class TicketsBctServiceImpl implements TicketsBctService {

	@Autowired
	private TicketBctRepository ticketBctRepository;

	public TicketEntity findByTicket(String ticket) {
		TicketEntity result = new TicketEntity();
		Object[] ent = (Object[]) ticketBctRepository.findByTicket(ticket);
		if (ent == null) {
			return null;
		}

		result.setTicket((String) ent[0]);
		result.setFecha((Date) ent[1]);
		result.setTienda(Integer.parseInt(ent[2].toString()));
		result.setCaja(Integer.parseInt(ent[3].toString()));
		result.setCajero(Float.parseFloat(ent[4].toString()));
		result.setNumTicket(Integer.parseInt(ent[5].toString()));
		result.setTipoTicket(Integer.parseInt(ent[6].toString()));
		result.setTotalPagar(new BigDecimal(ent[7].toString()));
		result.setTotalSinImpuestos(new BigDecimal(ent[8].toString()));
		result.setOriginal((String) ent[9]);
		result.setDevolucion(String.valueOf((char)ent[10]));						

		return result;
	}
	
}

package com.sodimac.facturacion.service;

import com.sodimac.facturacion.entity.bct.TicketEntity;

public interface TicketsBctService {

	public TicketEntity findByTicket(String ticket);
	
}
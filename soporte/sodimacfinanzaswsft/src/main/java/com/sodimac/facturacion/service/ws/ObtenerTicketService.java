package com.sodimac.facturacion.service.ws;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;

public interface ObtenerTicketService {

	public ClienteTicketObtenerExpRespTYPE getTicket(String ticket);
	
}

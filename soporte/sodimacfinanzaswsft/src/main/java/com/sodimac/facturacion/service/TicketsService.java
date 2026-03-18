package com.sodimac.facturacion.service;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.sodimac.facturacion.models.ClientesTemporalModel;

public interface TicketsService {

	public int getEstatusTicket(String ticket);
	public String validarTicketExpresionRegular(String ticket);
	public boolean validarMontoExpresionRegular(String monto);
	public String validarTicketWS(String ticket, String monto);
	public ClienteTicketObtenerExpRespTYPE getResultTicketWS(ClientesTemporalModel model) throws NumberFormatException;
	public int eliminarTicket(String ticket);
	public int validarTicket(String ticket);
	
}
package com.sodimac.facturacion.service;

import java.text.ParseException;

import com.sodimac.facturacion.entity.bct.TicketEntity;

public interface TicketsService {

	public int getEstatusTicket(String ticket);
	public String validarTicketExpresionRegular(String ticket);
	public boolean validarMontoExpresionRegular(String monto);
	public String validarTicketWS(String ticket, String monto) throws ParseException;
	
	public TicketEntity getTicket(String ticket);
	
}
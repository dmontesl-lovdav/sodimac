package com.sodimac.facturacion.service;

import java.math.BigDecimal;
import java.util.List;

import com.sodimac.facturacion.entity.fac.ListaFacturasEntity;

public interface ListaFacturasService {

	public List<ListaFacturasEntity> getAllListaFacturas();
	public ListaFacturasEntity getListaFacturaById(int idListaFacturas);
	public ListaFacturasEntity addListaFacturas(String ticket, BigDecimal total, String sessionid);
	public void updateListasFacturas(ListaFacturasEntity listaFactura);
	public void deleteItem(String ticket, String sessionId);
	public ListaFacturasEntity getListaFacturaByTicket(String ticket, String sessionId);
	public List<ListaFacturasEntity> getListaFacturasBySessionId(String SessionId);
	public ListaFacturasEntity findByTicketAndTotal(String ticket, BigDecimal total);
	public ListaFacturasEntity findByTicket (String ticket);

}

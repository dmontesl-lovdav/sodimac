package com.sodimac.facturacion.service;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;
import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.ticket.obtener.resp_v2018.ClienteTicketObtenerExpRespTYPE;
import com.sodimac.facturacion.exception.DataBaseException;

public interface FacturasService {
	public ClienteTicketObtenerExpRespTYPE obtenerTicket (String numeroOrden, String transaccion, String tienda, String caja, String fecha) throws DataBaseException;
	public ClienteFacturaConfirmarExpRespTYPE confirmaFactura(String ticket, String codigo, String uuid) throws DataBaseException;
}

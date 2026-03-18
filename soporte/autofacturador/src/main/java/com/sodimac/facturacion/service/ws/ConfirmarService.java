package com.sodimac.facturacion.service.ws;

import com.falabella.mdwcorp.sod.corp.osb.schema.cliente.factura.confirmar.resp_v2018.ClienteFacturaConfirmarExpRespTYPE;

public interface ConfirmarService {

	public ClienteFacturaConfirmarExpRespTYPE confirmar(String numeroTicket, String comprobanteFiscal, String codigo);
	
}
